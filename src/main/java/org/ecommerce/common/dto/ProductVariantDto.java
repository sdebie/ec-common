package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Type
public class ProductVariantDto {

    @Description("Product Variant ID (UUID as string)")
    public String id;

    public String sku;
    public Integer stockQuantity;
    public String attributesJson;
    public BigDecimal weightKg;

    @Description("Parent product info")
    public ProductDto product;

    @Description("All prices for this variant")
    public List<VariantPriceDto> prices = new ArrayList<>();

    @Description("Images attached to this variant")
    public List<ProductImageDto> images = new ArrayList<>();

    public ProductVariantDto() {}
}
