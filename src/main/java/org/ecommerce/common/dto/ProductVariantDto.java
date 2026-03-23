package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;
import org.ecommerce.common.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Type
public class ProductVariantDto {

    @Description("Product Variant ID (UUID as string)")
    public String id;

    public BigDecimal retailPrice;
    public BigDecimal retailSalesPrice;
    public BigDecimal wholesalePrice;
    public BigDecimal wholesaleSalesPrice;

    public ProductEntity product;
    public String sku;
    public List<VariantPriceDto> variantPrices = Collections.emptyList();
    public Integer stockQuantity;
    public String attributesJson;
    public BigDecimal weightKg;

    public ProductVariantDto() {}

    public ProductVariantDto(String id, BigDecimal retailPrice, BigDecimal retailSalesPrice, BigDecimal wholesalePrice, BigDecimal wholesaleSalesPrice, ProductEntity product, String sku, List<VariantPriceDto> variantPrices, Integer stockQuantity, String attributesJson, BigDecimal weightKg) {
        this.id = id;
        this.retailPrice = retailPrice;
        this.retailSalesPrice = retailSalesPrice;
        this.wholesalePrice = wholesalePrice;
        this.wholesaleSalesPrice = wholesaleSalesPrice;
        this.product = product;
        this.sku = sku;
        this.variantPrices = variantPrices;
        this.stockQuantity = stockQuantity;
        this.attributesJson = attributesJson;
        this.weightKg = weightKg;
    }

    public ProductVariantDto(String id, String sku, List<VariantPriceDto> variantPrices, Integer stockQuantity, String attributesJson, BigDecimal weightKg) {
        this.id = id;
        this.sku = sku;
        this.variantPrices = variantPrices;
        this.stockQuantity = stockQuantity;
        this.attributesJson = attributesJson;
        this.weightKg = weightKg;
    }
}
