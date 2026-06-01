package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.UUID;

@Type
public class OrderItemDetailDto {
    public UUID id;
    public BigDecimal unitPrice;
    public Integer quantity;
    public ProductVariantDetailDto variant;
}