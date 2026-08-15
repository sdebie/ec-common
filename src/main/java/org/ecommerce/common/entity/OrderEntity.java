package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.OrderStatusEn;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity extends PanacheEntityBase
{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = true)
    private CustomerEntity customerEntity;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "session_id")
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private OrderStatusEn status = OrderStatusEn.PENDING;

    // Contact details
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_last_name")
    private String contactLastName;

    // Shipping method
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_method_id")
    private ShippingMethodEntity shippingMethod;

    // Shipping address
    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    /**
     * Courier tracking, recorded when the order is marked IN_TRANSIT. Null until then,
     * and always null for a collection order. The in-transit notification exists to hand
     * the shopper this reference — without it that email can only say "on its way".
     */
    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "tracking_carrier")
    private String trackingCarrier;

    @OneToMany(mappedBy = "orderEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The human-facing order reference shown to staff and customers. Derived
     * from the id rather than stored, so it needs no sequence, no extra column
     * and no backfill, and is stable for the life of the order.
     */
    public String getReference()
    {
        return id == null ? null : "ORD-" + id.toString().substring(0, 8).toUpperCase();
    }

    /**
     * Display name for whoever placed the order. A guest checkout has no
     * customer record, so the contact details captured at checkout are the
     * only name that exists — the same fallback order the confirmation mailer
     * uses.
     */
    public String getPlacedByName()
    {
        if (customerEntity != null) {
            String name = join(customerEntity.getFirstName(), customerEntity.getLastName());
            if (name != null) {
                return name;
            }
        }
        return join(contactFirstName, contactLastName);
    }

    private static String join(String first, String last)
    {
        String joined = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return joined.isEmpty() ? null : joined;
    }

    // Finder methods to return fully-hydrated orders (customer + order_items)
    public static OrderEntity findOrderInfoById(UUID id)
    {
        if (id == null)
            throw new IllegalArgumentException("id must not be null");
        return find("select distinct o from OrderEntity o left join fetch o.customerEntity left join fetch o.items where o.id = ?1", id)
                .firstResult();
    }

    public static OrderEntity findLatestOrderInfoBySessionId(UUID sessionId)
    {
        if (sessionId == null)
            throw new IllegalArgumentException("sessionId must not be null");
        return find("select distinct o from OrderEntity o left join fetch o.customerEntity left join fetch o.items where o.sessionId = ?1 order by o.createdAt desc", sessionId)
                .firstResult();
    }


    /**
     * Total units on the order, not the number of distinct lines. Staff read this as
     * "how many things am I picking", so a line of quantity 3 counts as 3.
     */
    public int totalUnits()
    {
        if (items == null) {
            return 0;
        }
        int count = 0;
        for (OrderItemEntity item : items) {
            if (item != null && item.getQuantity() != null) {
                count += item.getQuantity();
            }
        }
        return count;
    }

    /**
     * An address that reaches whoever placed this order: the account email when the order
     * belongs to a signed-in customer, otherwise the checkout contact a guest supplied.
     */
    public String reachableEmail()
    {
        if (customerEntity != null && customerEntity.getUser() != null) {
            String email = customerEntity.getUser().getEmail();
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return contactEmail;
    }
}
