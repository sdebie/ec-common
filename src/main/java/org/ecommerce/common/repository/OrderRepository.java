package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.enums.OrderStatusEn;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.SortRequest;
import org.ecommerce.common.query.enums.SortDirection;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderRepository extends BaseRepository<OrderEntity, UUID>
{
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "id", "status", "createdAt", "totalAmount", "vatAmount", "shippingCost",
            "contactEmail", "contactFirstName", "contactLastName",
            "city", "province", "postalCode", "trackingNumber", "trackingCarrier",
            "customerEntity.id", "customerEntity.firstName", "customerEntity.lastName",
            "customerEntity.status", "customerEntity.shopperType",
            "shippingMethod.id", "shippingMethod.name");

    @Override
    protected Class<OrderEntity> getEntityClass()
    {
        return OrderEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }

    public OrderEntity findByIdWithCustomerAndItems(UUID id)
    {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        OrderEntity order = find("select o from OrderEntity o " +
                "left join fetch o.customerEntity " +
                "where o.id = ?1", id)
                .firstResult();

        if (order == null) {
            return null;
        }

        fetchAndAttachItems(List.of(order));
        return order;
    }

    public OrderEntity findByIdempotencyKey(UUID key)
    {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }

        OrderEntity order = find("""
                select o from OrderEntity o
                  left join fetch o.customerEntity
                  left join fetch o.shippingMethod
                where o.idempotencyKey = ?1
                """, key).firstResult();

        if (order == null) {
            return null;
        }

        fetchAndAttachItems(List.of(order));
        return order;
    }

    /**
     * Ids of orders in {@code statuses} created before {@code cutoff}, oldest first and capped
     * at {@code limit} — the abandoned-checkout candidates the stock-recovery sweep releases.
     */
    public List<UUID> findAbandonedIds(Collection<OrderStatusEn> statuses, LocalDateTime cutoff, int limit)
    {
        return getEntityManager()
                .createQuery("select o.id from OrderEntity o where o.status in :statuses and o.createdAt < :cutoff "
                        + "order by o.createdAt", UUID.class)
                .setParameter("statuses", statuses)
                .setParameter("cutoff", cutoff)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Both {@code items} and its {@code variant} are LAZY, and the variant ids are exactly what
     * the stock-recovery sweep's stock update needs — fetched with the order rather than a
     * query per line. Safe to fetch a collection here: this loads a single order by id.
     */
    public OrderEntity findWithItemsAndVariant(UUID id)
    {
        List<OrderEntity> found = getEntityManager()
                .createQuery("select distinct o from OrderEntity o "
                        + "left join fetch o.items i "
                        + "left join fetch i.variant "
                        + "where o.id = :id", OrderEntity.class)
                .setParameter("id", id)
                .getResultList();

        return found.isEmpty() ? null : found.get(0);
    }

    private void fetchAndAttachItems(List<OrderEntity> orders)
    {
        if (orders == null || orders.isEmpty()) return;

        List<UUID> orderIds = orders.stream().map(OrderEntity::getId).collect(Collectors.toList());

        List<OrderItemEntity> items = getEntityManager()
                .createQuery("select i from OrderItemEntity i " +
                        "left join fetch i.variant v " +
                        "left join fetch v.product " +
                        "where i.orderEntity.id in :orderIds", OrderItemEntity.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<UUID, List<OrderItemEntity>> itemsByOrderId = new HashMap<>();
        for (OrderItemEntity item : items) {
            itemsByOrderId.computeIfAbsent(item.getOrderEntity().getId(), k -> new ArrayList<>()).add(item);
        }

        for (OrderEntity order : orders) {
            order.getItems().clear();
            order.getItems().addAll(itemsByOrderId.getOrDefault(order.getId(), List.of()));
        }
    }

    public List<OrderEntity> findAllWithCustomerAndItems(PageRequest pageRequest, FilterRequest filterRequest)
    {
        PageRequest effectivePageRequest = pageRequest == null ? new PageRequest() : pageRequest;
        FilterRequest effectiveFilterRequest = withDefaultCreatedAtSort(filterRequest);

        List<OrderEntity> pagedOrders = findAll(effectivePageRequest, effectiveFilterRequest);
        if (pagedOrders == null || pagedOrders.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> ids = pagedOrders.stream()
                .map(OrderEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return findByIdsWithCustomerAndItems(ids);
    }

    private static final Set<String> ADMIN_SORTABLE_FIELDS = Set.of("createdAt", "totalAmount", "status");

    public List<OrderEntity> findForAdmin(Collection<OrderStatusEn> statuses, LocalDateTime from, LocalDateTime to, SortRequest sort, PageRequest pageRequest)
    {
        PageRequest page = pageRequest == null ? new PageRequest() : pageRequest;
        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(statuses, from, to, params);

        TypedQuery<UUID> idQuery = getEntityManager()
                .createQuery("select o.id from OrderEntity o" + where + adminOrderByClause(sort, ADMIN_SORTABLE_FIELDS, "o", "createdAt"), UUID.class)
                .setFirstResult(page.getOffset())
                .setMaxResults(page.getPageSize());
        params.forEach(idQuery::setParameter);

        return findByIdsWithCustomerAndItems(idQuery.getResultList());
    }

    /**
     * Batch-loads orders with their customer and items in three queries total, however many
     * ids are given — the shape both {@link #findAllWithCustomerAndItems} and {@link #findForAdmin}
     * need, kept in one place so neither drifts into re-fetching a page one row at a time.
     * Returns orders in the same order as {@code ids}; an id with no matching row is skipped.
     */
    private List<OrderEntity> findByIdsWithCustomerAndItems(List<UUID> ids)
    {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderEntity> orders = getEntityManager()
                .createQuery("select o from OrderEntity o "
                        + "left join fetch o.customerEntity "
                        + "where o.id in :ids", OrderEntity.class)
                .setParameter("ids", ids)
                .getResultList();
        fetchAndAttachItems(orders);

        Map<UUID, OrderEntity> byId = new LinkedHashMap<>();
        for (OrderEntity order : orders) {
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

    public long countForAdmin(Collection<OrderStatusEn> statuses, LocalDateTime from, LocalDateTime to)
    {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(statuses, from, to, params);

        TypedQuery<Long> query = getEntityManager()
                .createQuery("select count(o.id) from OrderEntity o" + where, Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

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
