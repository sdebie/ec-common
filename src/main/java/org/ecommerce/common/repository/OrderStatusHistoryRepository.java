package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderStatusHistoryEntity;
import org.ecommerce.common.enums.OrderStatusEn;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OrderStatusHistoryRepository extends BaseRepository<OrderStatusHistoryEntity, UUID>
{
    @Override
    protected Class<OrderStatusHistoryEntity> getEntityClass()
    {
        return OrderStatusHistoryEntity.class;
    }

    /** An order's status timeline, newest first. */
    public List<OrderStatusHistoryEntity> findByOrderId(UUID orderId)
    {
        return find("select h from OrderStatusHistoryEntity h where h.order.id = ?1 order by h.createdAt desc", orderId).list();
    }

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
