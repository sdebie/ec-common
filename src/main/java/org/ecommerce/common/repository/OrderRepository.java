package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.entity.ProductImageEntity;
import org.ecommerce.common.entity.ProductVariantEntity;
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
