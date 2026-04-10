package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * DTO for sale product listing: product details and only variants currently on sale.
 */
@Type
public class OnSaleProductListDto {
    @Description("Product information")
    public ProductDto product;

    @Description("Variants for this product with active sale prices only")
    public List<ProductVariantDto> variants;

    public OnSaleProductListDto() {}

    public OnSaleProductListDto(ProductDto product, List<ProductVariantDto> variants) {
        this.product = product;
        this.variants = variants;
    }
}

