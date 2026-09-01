package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderStatusHistoryEntity;
import org.ecommerce.common.enums.OrderStatusEn;

import java.util.UUID;

@ApplicationScoped
public class OrderStatusHistoryRepository extends BaseRepository<OrderStatusHistoryEntity, UUID>
{
    @Override
    protected Class<OrderStatusHistoryEntity> getEntityClass()
    {
        return OrderStatusHistoryEntity.class;
    }

    /**
     * Records a status change against an order. Every writer of
     * {@code OrderEntity.status} goes through here so the admin timeline shows
     * the whole life of an order and not just the staff-driven part of it.
     * <p>
     * Caller must already hold a transaction; the row is persisted immediately.
     *
     * @param changedBy the staff member's display name, or "SYSTEM" for an
     *                  automated transition
     */
    public OrderStatusHistoryEntity record(OrderEntity order, OrderStatusEn status, String comment, String changedBy)
    {
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrder(order);
        history.setStatus(status);
        history.setComment(comment);
        history.setChangedBy(changedBy);
        persist(history);
        return history;
    }
}
