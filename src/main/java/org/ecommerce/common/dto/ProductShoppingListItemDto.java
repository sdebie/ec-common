package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * Storefront DTO for shopping product cards with consolidated active pricing.
 */
@Getter
@Setter
@Type
public class ProductShoppingListItemDto
{
    @Description("Product ID (UUID as string)")
    private String id;

    @Description("Product name")
    private String name;

    @Description("URL slug for routing to the product detail page")
    private String slug;

    @Description("Product short description")
    private String shortDescription;

    @Description("Product type (e.g. SIMPLE or VARIABLE)")
    private String productType;

    @Description("Product status")
    private String status;

    @Description("Total number of variants for this product")
    private Integer variantCount;

    @Description("Variant ID for SIMPLE products; null for non-SIMPLE products")
    private String variantId;

    @Description("Product images including featured flag")
    private List<ProductImageDto> images;

    @Description("Lowest active retail price in the current window with oldest start date")
    private VariantPriceDto retailPrice;

    @Description("Lowest active wholesale price in the current window with oldest start date")
    private VariantPriceDto wholesalePrice;

    @Description("Lowest active retail sale price in the current window with oldest start date")
    private VariantPriceDto retailSalePrice;

    @Description("Lowest active wholesale sale price in the current window with oldest start date")
    private VariantPriceDto wholesaleSalePrice;

    public ProductShoppingListItemDto()
    {
    }
}
