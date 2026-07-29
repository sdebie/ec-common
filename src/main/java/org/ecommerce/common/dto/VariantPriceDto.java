package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime priceStartDate;

    @Description("When this price expires (null = never expires)")
    private LocalDateTime priceEndDate;

    @Description("Whether this price is currently active based on date range")
    private Boolean isActive;

    @Description("Number of days remaining for sale prices (RETAIL_SALE_PRICE / WHOLESALE_SALE_PRICE); null for non-sale or no end date")
    private Long saleDaysRemaining;

    public VariantPriceDto()
    {
    }

}
