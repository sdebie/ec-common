package org.ecommerce.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.OrderStatusEn;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class OrderEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerEntity customerEntity;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    /**
     * VAT and shipping fee as they were computed when this order was priced —
     * at creation, or at {@link #shippingMethod} repricing. Null only for an
     * order placed before this column existed. The admin detail breakdown
     * reads these rather than recomputing against today's settings, so a
     * later VAT-rate or shipping-fee change can never move what an
     * already-charged order appears to have cost.
     */
    @Column(name = "vat_amount")
    private BigDecimal vatAmount;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    @Column(name = "session_id")
    private UUID sessionId;

    /**
     * Identifies one checkout intent, stable across a client's retries of it.
     * A bearer capability of the same entropy as {@link #sessionId} — treat it
     * with the same care, since holding it is equivalent to holding the order.
     * Nullable: every order predating this feature, and any created during the
     * deployment window before the header became required, carries no key.
     */
    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    /**
     * A hash of the cart the {@link #idempotencyKey} was minted for, aggregated
     * and sorted by variant id. Detects a key reused with different contents;
     * never recomputed from {@link #items}, since it is a property of the
     * originating request, not of the order's current state.
     */
    @Column(name = "cart_fingerprint")
    private String cartFingerprint;

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
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    public String getReference() {
        return getId() == null ? null : "ORD-" + getId().toString().substring(0, 8).toUpperCase();
    }

    public String getPlacedByName() {
        if (customerEntity != null) {
            String name = join(customerEntity.getFirstName(), customerEntity.getLastName());
            if (name != null) {
                return name;
            }
        }
        return join(contactFirstName, contactLastName);
    }

    private static String join(String first, String last) {
        String joined = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return joined.isEmpty() ? null : joined;
    }

    public int totalUnits() {
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

    public String reachableEmail() {
        if (customerEntity != null && customerEntity.getUser() != null) {
            String email = customerEntity.getUser().getEmail();
            if (email != null && !email.isBlank()) {
                return email;
            }
        }
        return contactEmail;
    }
}
