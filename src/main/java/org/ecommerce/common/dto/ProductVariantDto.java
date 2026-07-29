package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Type
public class ProductVariantDto {

    @Description("Product Variant ID (UUID as string)")
    private String id;

    private String sku;
    private Integer stockQuantity;
    private String attributesJson;
    private BigDecimal weightKg;

    @Description("Variant status")
    private String status;

    @Description("Parent product info")
    private ProductDto product;

    @Description("All prices for this variant")
    private List<VariantPriceDto> prices = new ArrayList<>();

    @Description("Images attached to this variant")
    private List<ProductImageDto> images = new ArrayList<>();

    public ProductVariantDto() {}
}
