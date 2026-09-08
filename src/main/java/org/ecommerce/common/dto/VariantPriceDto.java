package org.ecommerce.common.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Ignore;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a single price for a product variant.
 * Includes price type, customer type, amount, and validity period.
 */
@Type
@Getter
@Setter
public class VariantPriceDto
{
    @Description("Price ID (UUID as string)")
    private String id;

    @Description("Price type: RETAIL_PRICE, RETAIL_SALE_PRICE, WHOLESALE_PRICE, WHOLESALE_SALE_PRICE")
    private String priceType;

    @Description("Price amount")
    private BigDecimal price;

    @Description("When this price becomes active (null = always active)")
    private Instant priceStartDate;

    @Description("When this price expires (null = never expires)")
    private Instant priceEndDate;

    @Description("Whether this price is currently active based on date range")
    private Boolean isActive;

    /** Computed; GraphQL output only (CVE-2026-76763 — do not map this to BigInteger input). */
    @Setter(AccessLevel.NONE)
    @Description("Number of days remaining for sale prices (RETAIL_SALE_PRICE / WHOLESALE_SALE_PRICE); null for non-sale or no end date")
    private Integer saleDaysRemaining;

    public VariantPriceDto() {
    }

    @Ignore
    public void setSaleDaysRemaining(Integer saleDaysRemaining) {
        this.saleDaysRemaining = saleDaysRemaining;
    }

}
