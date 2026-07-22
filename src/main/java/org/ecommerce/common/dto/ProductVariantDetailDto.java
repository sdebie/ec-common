package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Type
public class ProductVariantDetailDto
{
    private UUID id;
    private Integer stockQuantity;
    private String attributesJson;
    private BigDecimal weightKg;
    private ProductDetailDto product;
    private List<ProductImageDto> images;
}