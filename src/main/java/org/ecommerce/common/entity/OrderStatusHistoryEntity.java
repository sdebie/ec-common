package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.OrderStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatusEn status; // PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(columnDefinition = "TEXT")
    private String comment; // e.g., "Customer requested delay"

    @Column(name = "changed_by")
    private String changedBy; // ID of the staff member or "SYSTEM"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

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
    public static OrderStatusHistoryEntity record(OrderEntity order, OrderStatusEn status, String comment, String changedBy)
    {
        OrderStatusHistoryEntity history = new OrderStatusHistoryEntity();
        history.setOrder(order);
        history.setStatus(status);
        history.setComment(comment);
        history.setChangedBy(changedBy);
        history.persist();
        return history;
    }
}