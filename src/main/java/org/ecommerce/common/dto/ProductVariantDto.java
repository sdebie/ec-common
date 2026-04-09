package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;
import org.ecommerce.common.entity.ProductEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public LocalDateTime price_start_date;
    public LocalDateTime price_end_date;

    public ProductEntity product;
    public String sku;
    public List<VariantPriceDto> variantPrices = Collections.emptyList();
    public Integer stockQuantity;
    public String attributesJson;
    public BigDecimal weightKg;

    public ProductVariantDto() {}

}
