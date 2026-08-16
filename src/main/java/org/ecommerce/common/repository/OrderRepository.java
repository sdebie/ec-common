package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.entity.ProductVariantEntity;
import org.ecommerce.common.enums.OrderStatusEn;
import org.ecommerce.common.query.FieldNameValidator;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.SortRequest;
import org.ecommerce.common.query.enums.SortDirection;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class OrderRepository extends BaseRepository<OrderEntity, UUID>
{
    @Override
    protected Class<OrderEntity> getEntityClass()
    {
        return OrderEntity.class;
    }

    public OrderEntity findOrderInfoById(UUID id)
    {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        List<OrderEntity> results = find("select distinct o from OrderEntity o " +
                "left join fetch o.customerEntity " +
                "left join fetch o.items i " +
                "left join fetch i.variant v " +
                "left join fetch v.product " +
                "where o.id = ?1", id)
                .list();

        OrderEntity order = results.isEmpty() ? null : results.get(0);
        hydrateVariantImages(order);
        return order;
    }

    public OrderEntity findLatestOrderInfoBySessionId(UUID sessionId)
    {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId must not be null");
        }

        UUID latestOrderId = getEntityManager()
                .createQuery("select o.id from OrderEntity o where o.sessionId = :sessionId order by o.createdAt desc", UUID.class)
                .setParameter("sessionId", sessionId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (latestOrderId == null) {
            return null;
        }

        return findOrderInfoById(latestOrderId);
    }

    private void hydrateVariantImages(OrderEntity order)
    {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        Set<UUID> variantIds = new HashSet<>();
        for (OrderItemEntity item : order.getItems()) {
            if (item != null && item.getVariant() != null && item.getVariant().getId() != null) {
                variantIds.add(item.getVariant().getId());
            }
        }

        if (variantIds.isEmpty()) {
            return;
        }

        // Fetch only variant->images in a dedicated query to avoid multiple bag fetch in one select.
        getEntityManager().createQuery(
                        "select distinct v from ProductVariantEntity v left join fetch v.images where v.id in :variantIds",
                        ProductVariantEntity.class)
                .setParameter("variantIds", variantIds)
                .getResultList();
    }

    public List<OrderEntity> findAllOrderInfo(PageRequest pageRequest, FilterRequest filterRequest)
    {
        PageRequest effectivePageRequest = pageRequest == null ? new PageRequest() : pageRequest;
        FilterRequest effectiveFilterRequest = withDefaultCreatedAtSort(filterRequest);

        List<OrderEntity> pagedOrders = findAll(effectivePageRequest, effectiveFilterRequest);
        if (pagedOrders == null || pagedOrders.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderEntity> hydratedOrders = new ArrayList<>(pagedOrders.size());
        for (OrderEntity order : pagedOrders) {
            if (order == null || order.getId() == null) {
                continue;
            }
            OrderEntity fullOrder = findOrderInfoById(order.getId());
            if (fullOrder != null) {
                hydratedOrders.add(fullOrder);
            }
        }

        return hydratedOrders;
    }

    /**
     * The columns an admin can sort the order list by. Every other row field — reference,
     * the placed-by name, item count — is derived rather than stored ({@link OrderEntity}'s
     * own {@code getReference()}/{@code getPlacedByName()}/{@code totalUnits()}), so there is
     * no single JPQL property an ORDER BY could name for them.
     */
    private static final Set<String> ADMIN_SORTABLE_FIELDS = Set.of("createdAt", "totalAmount", "status");

    /**
     * One page of orders for the admin list, hydrated with the customer and line items the
     * list row needs, ordered per {@code sort} — or newest-first when {@code sort} is null,
     * blank, or names a field outside {@link #ADMIN_SORTABLE_FIELDS}. A silent fallback
     * rather than a thrown error, matching {@code PanacheQueryBuilder}'s own default-sort
     * behaviour: an unresolvable sort request is not a malformed query, it is a request for
     * "however you'd normally order these".
     * <p>
     * Hand-written rather than routed through {@link FilterRequest}: that path
     * coerces a filter value to boolean/UUID/Long/Double/String only, so a
     * {@code createdAt} bound would bind a String against a timestamp column
     * and fail. The date range is the whole point of this query. The field name is still
     * validated through the shared {@link FieldNameValidator} PanacheQueryBuilder itself
     * uses, and further narrowed to a fixed whitelist — a raw-JPQL ORDER BY has no query
     * planner to fail closed for it the way a Hibernate-managed one does, so a wrong name
     * here fails at execution as a raw SQL error rather than a controlled one.
     *
     * @param from inclusive lower bound, or null for no lower bound
     * @param to   exclusive upper bound, or null for no upper bound
     */
    public List<OrderEntity> findForAdmin(Collection<OrderStatusEn> statuses, LocalDateTime from, LocalDateTime to, SortRequest sort, PageRequest pageRequest)
    {
        PageRequest page = pageRequest == null ? new PageRequest() : pageRequest;
        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(statuses, from, to, params);

        // Page over ids alone. A bag fetch of o.items cannot be paged in SQL, so
        // combining the fetch with setMaxResults would make Hibernate page in
        // memory over the entire result set.
        TypedQuery<UUID> idQuery = getEntityManager()
                .createQuery("select o.id from OrderEntity o" + where + adminOrderByClause(sort), UUID.class)
                .setFirstResult(page.getOffset())
                .setMaxResults(page.getPageSize());
        params.forEach(idQuery::setParameter);

        List<UUID> ids = idQuery.getResultList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderEntity> hydrated = getEntityManager()
                .createQuery("select distinct o from OrderEntity o "
                        + "left join fetch o.customerEntity "
                        + "left join fetch o.items "
                        + "where o.id in :ids", OrderEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        // An IN-clause does not preserve the id order, so re-impose the page's.
        Map<UUID, OrderEntity> byId = new LinkedHashMap<>();
        for (OrderEntity order : hydrated) {
            byId.put(order.getId(), order);
        }

        List<OrderEntity> ordered = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            OrderEntity order = byId.get(id);
            if (order != null) {
                ordered.add(order);
            }
        }
        return ordered;
    }

    /**
     * Total matching {@link #findForAdmin} under the same filters, for paging.
     */
    public long countForAdmin(Collection<OrderStatusEn> statuses, LocalDateTime from, LocalDateTime to)
    {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(statuses, from, to, params);

        TypedQuery<Long> query = getEntityManager()
                .createQuery("select count(o.id) from OrderEntity o" + where, Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    /**
     * Builds the shared WHERE clause and fills {@code params} with its bindings.
     *
     * @param statuses the statuses to match, or null for every status. An <em>empty</em>
     *                 collection is not the same thing: it means the caller's filters
     *                 admit nothing, and callers short-circuit on it rather than passing
     *                 it here, since {@code in ()} is not valid SQL.
     */
    private String adminWhereClause(Collection<OrderStatusEn> statuses, LocalDateTime from, LocalDateTime to, Map<String, Object> params)
    {
        List<String> clauses = new ArrayList<>();

        if (statuses != null && !statuses.isEmpty()) {
            clauses.add("o.status in :statuses");
            params.put("statuses", statuses);
        }
        if (from != null) {
            clauses.add("o.createdAt >= :from");
            params.put("from", from);
        }
        if (to != null) {
            clauses.add("o.createdAt < :to");
            params.put("to", to);
        }

        return clauses.isEmpty() ? "" : " where " + String.join(" and ", clauses);
    }

    private String adminOrderByClause(SortRequest sort)
    {
        String field = "createdAt";
        boolean descending = true;

        if (sort != null && sort.getField() != null && ADMIN_SORTABLE_FIELDS.contains(sort.getField())) {
            field = sort.getField();
            descending = sort.getDirection() != SortDirection.ASC;
        }

        return " order by o." + FieldNameValidator.validate(field) + (descending ? " desc" : " asc");
    }

    private FilterRequest withDefaultCreatedAtSort(FilterRequest filterRequest)
    {
        if (filterRequest != null && filterRequest.getSort() != null && !filterRequest.getSort().isEmpty()) {
            return filterRequest;
        }

        FilterRequest effective = filterRequest == null ? new FilterRequest() : filterRequest;
        SortRequest sortRequest = new SortRequest();
        sortRequest.setField("createdAt");
        sortRequest.setDirection(SortDirection.DESC);
        effective.setSort(List.of(sortRequest));
        return effective;
    }
}
