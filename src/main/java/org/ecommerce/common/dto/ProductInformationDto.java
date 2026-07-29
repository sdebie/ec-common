package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * DTO that represents a product with its variants.
 * Variant images are exposed on each ProductVariantDto.
 */
@Getter
@Setter
@Type
public class ProductInformationDto
{
    @Description("Product information")
    private ProductDto product;

    @Description("List of variants for this product")
    private List<ProductVariantDto> variants;

    public ProductInformationDto()
    {
    }

    public ProductInformationDto(ProductDto product,
                                 List<ProductVariantDto> variants)
    {
        this.product = product;
        this.variants = variants;
    }
}
