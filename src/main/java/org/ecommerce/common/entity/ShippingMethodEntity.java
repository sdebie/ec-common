package org.ecommerce.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shipping_methods")
public class ShippingMethodEntity
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
    
    @Column(name = "requires_address", nullable = false)
    private boolean requiresAddress = true;
}
