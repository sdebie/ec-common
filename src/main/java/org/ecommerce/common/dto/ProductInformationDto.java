package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * DTO that represents a product with multiple variants and multiple images.
 */
@Type
public class ProductInformationDto {
    @Description("Product information")
    public ProductDto product;

    @Description("List of product images")
    public List<ProductImageDto> productImages;

    @Description("List of variants for this product")
    public List<ProductVariantDto> variants;

    public ProductInformationDto() {}

    public ProductInformationDto(ProductDto product,
                                 List<ProductImageDto> productImages, List<ProductVariantDto> variants) {
        this.product = product;
        this.productImages = productImages;
        this.variants = variants;
    }
}
