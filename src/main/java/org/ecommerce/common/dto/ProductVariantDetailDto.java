package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Type
public class ProductVariantDetailDto {
    public UUID id;
    public Integer stockQuantity;
    public String attributesJson;
    public BigDecimal weightKg;
    public ProductDetailDto product;
    public List<ImageDetailDto> images;
}