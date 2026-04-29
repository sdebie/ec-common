package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;

@Type
public class OrderItemResponseDto
{
    public String id;
    public BigDecimal unitPrice;
    public Integer quantity;
    public ProductVariantDto variant;
}

