package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO that represents a single product variant (variant-specific information only).
 */
@Type
public class ProductVariantDto {
    @Description("Variant ID (UUID as string)")
    public String id;

    @Description("SKU of the variant")
    public String sku;

    @Description("Prices for this variant (organized by price type and customer type)")
    public List<VariantPriceDto> prices;

    @Description("Stock quantity available")
    public Integer stockQuantity;

    @Description("Attributes as JSON string")
    public String attributesJson;

    @Description("Weight in kilograms")
    public BigDecimal weightKg;

    public ProductVariantDto() {}

    public ProductVariantDto(String id, String sku, List<VariantPriceDto> prices, Integer stockQuantity,
                             String attributesJson, BigDecimal weightKg) {
        this.id = id;
        this.sku = sku;
        this.prices = prices;
        this.stockQuantity = stockQuantity;
        this.attributesJson = attributesJson;
        this.weightKg = weightKg;
    }
}
