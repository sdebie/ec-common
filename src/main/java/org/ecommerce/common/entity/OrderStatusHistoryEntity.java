package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.OrderStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    @UuidGenerator
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public OrderStatusEn status; // PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(columnDefinition = "TEXT")
    public String comment; // e.g., "Customer requested delay"

    @Column(name = "changed_by")
    public String changedBy; // ID of the staff member or "SYSTEM"

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}