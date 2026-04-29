package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a single price for a product variant.
 * Includes price type, customer type, amount, and validity period.
 */
@Type
public class VariantPriceDto {
    @Description("Price ID (UUID as string)")
    public String id;

    @Description("Price type: RETAIL_PRICE, RETAIL_SALE_PRICE, WHOLESALE_PRICE, WHOLESALE_SALE_PRICE")
    public String priceType;

    @Description("Price amount")
    public BigDecimal price;

    @Description("When this price becomes active (null = always active)")
    public LocalDateTime priceStartDate;

    @Description("When this price expires (null = never expires)")
    public LocalDateTime priceEndDate;

    @Description("Whether this price is currently active based on date range")
    public Boolean isActive;

    @Description("Number of days remaining for sale prices (RETAIL_SALE_PRICE / WHOLESALE_SALE_PRICE); null for non-sale or no end date")
    public Long saleDaysRemaining;

    public VariantPriceDto() {}

}
