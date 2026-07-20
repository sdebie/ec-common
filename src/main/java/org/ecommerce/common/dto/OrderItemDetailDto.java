package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;

@Type
public class OrderItemDetailDto {
    public String id;
    public BigDecimal unitPrice;
    public Integer quantity;
    public ProductVariantDetailDto variant;
}
