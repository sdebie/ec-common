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
}
