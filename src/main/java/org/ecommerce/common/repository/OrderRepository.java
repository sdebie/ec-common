package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.entity.ProductImageEntity;
import org.ecommerce.common.entity.ProductVariantEntity;
import org.ecommerce.common.enums.OrderStatusEn;
import org.ecommerce.common.query.FieldNameValidator;
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
    /**
     * {@code findAllOrderInfo} (behind the VIEWER-reachable {@code allOrders} GraphQL query)
     * routes through the inherited, generic {@link BaseRepository#findAll}, so this is a real
     * gate, not documentation. Excludes {@code idempotencyKey} and {@code cartFingerprint} —
     * both explicitly "a bearer capability" per this entity's own javadoc, never returned in
     * any DTO — and every {@code customerEntity.user.*} credential/security-posture field
     * (password hash, the {@code passwordResetCode*} family, {@code roles}, {@code
     * mfaEnabled}, {@code lastLogin}), the same class of column already found exploitable via
     * {@code CustomerRepository}, just reached one hop further through {@code customerEntity}.
     * {@code customerEntity.user.email} is deliberately left out too even though it isn't
     * secret (it's already bulk-returned by this endpoint's own {@code OrderMapper}) — nothing
     * here demonstrates a need to filter by it, and {@code contactEmail} already covers guest
     * order lookup without a relation traversal.
     */
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

    public OrderEntity findOrderInfoById(UUID id)
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

        attachItems(List.of(order));
        hydrateVariantImages(order);
        return order;
    }

    /**
     * Resolves an order by its checkout idempotency key, joining every
     * association {@code replayOrder}/{@code computeTotals} touch —
     * deliberately more than {@link #findOrderInfoById}'s set. This runs
     * outside a transaction (design §3.3), so a lazy association left
     * unjoined here is a hard failure, not a slow one: {@code items.variant}
     * and {@code variant.product} for each line's name and id (via
     * {@link #attachItems}), and {@code shippingMethod} for the totals a
     * replay recomputes.
     */
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

        attachItems(List.of(order));
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

    /**
     * Loads {@code items} (with each line's variant and product to-one chains) for the
     * given orders in one query and attaches them, instead of a to-many JOIN FETCH from
     * the order side — which would multiply each order's row once per item and need
     * DISTINCT to collapse back down.
     * <p>
     * Mutates each order's existing {@code items} collection in place ({@code clear()} +
     * {@code addAll()}) rather than assigning a new {@code List} to the field: {@code items}
     * cascades with {@code orphanRemoval = true}, and Hibernate tracks that collection by
     * instance identity — swapping in a different {@code List} instance leaves the original,
     * still-tracked collection instance orphaned from its owner, which Hibernate refuses to
     * flush ("A collection with orphan deletion was no longer referenced by the owning
     * entity instance"). This surfaces for real whenever the same managed order is fetched
     * more than once in one persistence context — e.g. a test (or service) that persists an
     * order, then calls a service method that re-fetches it by id before the transaction
     * commits. Mutating in place never changes which collection instance Hibernate has
     * registered, so this holds regardless of how many times it runs against the same order
     * in one session.
     */
    private void attachItems(List<OrderEntity> orders)
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

    /**
     * Batch-loads and attaches {@code variant.images} for every variant among an order's
     * items, in one query grouped by variant id — instead of a to-many JOIN FETCH (which
     * would multiply rows and need DISTINCT). Every variant here is already a managed
     * entity from {@link #attachItems}'s fetch-joined {@code i.variant}, so
     * {@code getEntityManager().find} returns the same instance rather than issuing
     * another query. Mutates {@code images} in place ({@code clear()} + {@code addAll()})
     * rather than assigning a new {@code List} — see {@link #attachItems}'s javadoc for
     * why: {@code images} also cascades with {@code orphanRemoval = true}.
     */
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

        List<ProductImageEntity> images = getEntityManager()
                .createQuery("select img from ProductImageEntity img " +
                        "where img.productVariant.id in :variantIds " +
                        "order by img.productVariant.id, img.sortOrder asc", ProductImageEntity.class)
                .setParameter("variantIds", variantIds)
                .getResultList();

        Map<UUID, List<ProductImageEntity>> imagesByVariantId = new HashMap<>();
        for (ProductImageEntity image : images) {
            imagesByVariantId.computeIfAbsent(image.getProductVariant().getId(), k -> new ArrayList<>()).add(image);
        }

        for (UUID variantId : variantIds) {
            ProductVariantEntity variant = getEntityManager().find(ProductVariantEntity.class, variantId);
            if (variant != null) {
                variant.getImages().clear();
                variant.getImages().addAll(imagesByVariantId.getOrDefault(variantId, List.of()));
            }
        }
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
                .createQuery("select o.id from OrderEntity o" + where + adminOrderByClause(sort, ADMIN_SORTABLE_FIELDS, "o", "createdAt"), UUID.class)
                .setFirstResult(page.getOffset())
                .setMaxResults(page.getPageSize());
        params.forEach(idQuery::setParameter);

        List<UUID> ids = idQuery.getResultList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrderEntity> hydrated = getEntityManager()
                .createQuery("select o from OrderEntity o "
                        + "left join fetch o.customerEntity "
                        + "where o.id in :ids", OrderEntity.class)
                .setParameter("ids", ids)
                .getResultList();
        attachItems(hydrated);

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
