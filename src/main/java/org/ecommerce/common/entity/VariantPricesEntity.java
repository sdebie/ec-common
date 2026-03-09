package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.ecommerce.common.enums.PriceTypeEn;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing different prices for a product variant.
 * Each variant can have multiple prices for different scenarios:
 * - RETAIL_PRICE and RETAIL_SALE_PRICE for retail customers
 * - WHOLESALE_PRICE and WHOLESALE_SALE_PRICE for wholesale customers
 *
 * The price_start_date and price_end_date allow for time-limited pricing (e.g., promotions).
 */
@Entity
@Table(name = "variant_prices")
public class VariantPricesEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    public ProductVariantEntity variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricetype", nullable = false)
    public PriceTypeEn priceType;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal price;

    @Column(name = "price_start_date")
    public LocalDateTime priceStartDate;

    @Column(name = "price_end_date")
    public LocalDateTime priceEndDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "created_by")
    public UUID createdBy;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @Column(name = "updated_by")
    public UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Helper Methods ---

    /**
     * Checks if the price is currently active (within its date range).
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        if (priceStartDate != null && now.isBefore(priceStartDate)) {
            return false;
        }
        if (priceEndDate != null && now.isAfter(priceEndDate)) {
            return false;
        }
        return true;
    }

    /**
     * Find all prices for a specific variant.
     */
    public static List<VariantPricesEntity> findByVariantId(UUID variantId) {
        if (variantId == null) return List.of();
        return list("variant.id = ?1 order by priceType asc, createdAt desc", variantId);
    }

    /**
     * Find the active price for a variant by type.
     */
    public static VariantPricesEntity findActiveByVariantAndType(UUID variantId, PriceTypeEn priceType) {
        if (variantId == null || priceType == null) return null;

        LocalDateTime now = LocalDateTime.now();
        return find(
            "variant.id = ?1 and priceType = ?2 and (priceStartDate is null or priceStartDate <= ?3) " +
            "and (priceEndDate is null or priceEndDate >= ?3) " +
            "order by updatedAt desc",
            variantId, priceType, now
        ).firstResult();
    }

    /**
     * Find the latest price record for a variant by type (regardless of date range).
     */
    public static VariantPricesEntity findLatestByVariantAndType(UUID variantId, PriceTypeEn priceType) {
        if (variantId == null || priceType == null) return null;

        return find(
            "variant.id = ?1 and priceType = ?2 order by updatedAt desc",
            variantId, priceType
        ).firstResult();
    }

    /**
     * Find all active prices for a variant.
     */
    public static List<VariantPricesEntity> findActiveByVariantId(UUID variantId) {
        if (variantId == null) return List.of();

        LocalDateTime now = LocalDateTime.now();
        return list(
            "variant.id = ?1 and (priceStartDate is null or priceStartDate <= ?2) " +
            "and (priceEndDate is null or priceEndDate >= ?2) " +
            "order by priceType asc",
            variantId, now
        );
    }

    /**
     * Find all active prices for a variant and list of price types.
     */
    public static List<VariantPricesEntity> findActiveByVariantAndTypes(UUID variantId, List<PriceTypeEn> priceTypes) {
        if (variantId == null || priceTypes == null || priceTypes.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();
        return list(
            "variant.id = ?1 and priceType in ?2 and (priceStartDate is null or priceStartDate <= ?3) " +
            "and (priceEndDate is null or priceEndDate >= ?3) " +
            "order by priceType asc",
            variantId, priceTypes, now
        );
    }
}
