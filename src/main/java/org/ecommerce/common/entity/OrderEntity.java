package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;
import org.ecommerce.common.enums.OrderStatusEn;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = true)
    public CustomerEntity customerEntity;

    @Column(name = "total_amount", nullable = false)
    public BigDecimal totalAmount;

    @Column(name = "session_id")
    public UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    public OrderStatusEn status = OrderStatusEn.PENDING;

    // Contact details
    @Column(name = "contact_email")
    public String contactEmail;

    @Column(name = "contact_first_name")
    public String contactFirstName;

    @Column(name = "contact_last_name")
    public String contactLastName;

    // Shipping method
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_method_id")
    public ShippingMethodEntity shippingMethod;

    // Shipping address
    @Column(name = "street_address")
    public String streetAddress;

    @Column(name = "city")
    public String city;

    @Column(name = "province")
    public String province;

    @Column(name = "postal_code")
    public String postalCode;

    @OneToMany(mappedBy = "orderEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItemEntity> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    // Finder methods to return fully-hydrated orders (customer + order_items)
    public static OrderEntity findOrderInfoById(UUID id) {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        return find("select distinct o from OrderEntity o left join fetch o.customerEntity left join fetch o.items where o.id = ?1", id)
                .firstResult();
    }

    public static OrderEntity findLatestOrderInfoBySessionId(UUID sessionId) {
        if (sessionId == null)
            throw new IllegalArgumentException("sessionId must not be null");
        return find("select distinct o from OrderEntity o left join fetch o.customerEntity left join fetch o.items where o.sessionId = ?1 order by o.createdAt desc", sessionId)
                .firstResult();
    }

}
