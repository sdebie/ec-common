package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.entity.ProductVariantEntity;

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
}
