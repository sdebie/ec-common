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
