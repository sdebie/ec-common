package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

// Structured Shipping Methods
@Getter
@Setter
@Entity
@Table(name = "shipping_methods")
public class ShippingMethodEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String name;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "base_fee")
    private BigDecimal baseFee;

    @Column(name = "estimated_days")
    private String estimatedDays;

    /**
     * Whether choosing this method requires a delivery address. Collection methods do
     * not — and this must be stated, never inferred: fee and lead time describe what a
     * method costs and how fast it is, not whether anything is shipped anywhere.
     * <p>
     * Defaults true so a method created without an opinion asks for an address rather
     * than silently dropping one a courier needs.
     */
    @Column(name = "requires_address", nullable = false)
    private boolean requiresAddress = true;
}
