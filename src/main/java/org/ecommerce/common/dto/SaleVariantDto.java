package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * DTO representing a product variant on sale, enriched with its parent product info and images.
 */
@Type
public class SaleVariantDto {

    @Description("Variant info including prices, SKU, stock, and attributes")
    public ProductVariantDto variant;

    @Description("Parent product info")
    public ProductDto product;

    @Description("Product images")
    public List<ProductImageDto> productImages;

    public SaleVariantDto() {}

    public SaleVariantDto(ProductVariantDto variant,
                          ProductDto product,
                          List<ProductImageDto> productImages) {
        this.variant = variant;
        this.product = product;
        this.productImages = productImages;
    }
}

