package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * Storefront DTO for shopping product cards with consolidated active pricing.
 */
@Type
public class ProductShoppingListItemDto {
    @Description("Product ID (UUID as string)")
    public String id;

    @Description("Product name")
    public String name;

    @Description("Product short description")
    public String shortDescription;

    @Description("Total number of variants for this product")
    public Integer variantCount;

    @Description("Product images including featured flag")
    public List<ProductImageDto> images;

    @Description("Lowest active retail price in the current window with oldest start date")
    public VariantPriceDto retailPrice;

    @Description("Lowest active wholesale price in the current window with oldest start date")
    public VariantPriceDto wholesalePrice;

    @Description("Lowest active retail sale price in the current window with oldest start date")
    public VariantPriceDto retailSalePrice;

    @Description("Lowest active wholesale sale price in the current window with oldest start date")
    public VariantPriceDto wholesaleSalePrice;

    public ProductShoppingListItemDto() {
    }
}

