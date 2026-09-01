package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.PriceTypeEn;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing different prices for a product variant.
 * Each variant can have multiple prices for different scenarios:
 * - RETAIL_PRICE and RETAIL_SALE_PRICE for retail customers
 * - WHOLESALE_PRICE and WHOLESALE_SALE_PRICE for wholesale customers
 * <p>
 * The price_start_date and price_end_date allow for time-limited pricing (e.g., promotions).
 */
@Getter
@Setter
@Entity
@Table(name = "variant_prices")
public class VariantPricesEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariantEntity variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceTypeEn priceType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "price_start_date")
    private LocalDateTime priceStartDate;

    @Column(name = "price_end_date")
    private LocalDateTime priceEndDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate()
    {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        updatedAt = LocalDateTime.now();
    }

    // --- Helper Methods ---

    /**
     * Checks if the price is currently active (within its date range).
     */
    public boolean isActive()
    {
        LocalDateTime now = LocalDateTime.now();
        if (priceStartDate != null && now.isBefore(priceStartDate)) {
            return false;
        }
        if (priceEndDate != null && now.isAfter(priceEndDate)) {
            return false;
        }
        return true;
    }

}
