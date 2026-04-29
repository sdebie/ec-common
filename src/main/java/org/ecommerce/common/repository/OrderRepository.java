package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.entity.ProductVariantEntity;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.SortRequest;
import org.ecommerce.common.query.enums.SortDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class OrderRepository extends BaseRepository<OrderEntity, UUID>
{
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
        if (order == null || order.items == null || order.items.isEmpty()) {
            return;
        }

        Set<UUID> variantIds = new HashSet<>();
        for (OrderItemEntity item : order.items) {
            if (item != null && item.variant != null && item.variant.id != null) {
                variantIds.add(item.variant.id);
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
            if (order == null || order.id == null) {
                continue;
            }
            OrderEntity fullOrder = findOrderInfoById(order.id);
            if (fullOrder != null) {
                hydratedOrders.add(fullOrder);
            }
        }

        return hydratedOrders;
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
