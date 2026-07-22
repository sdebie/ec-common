package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;

@Getter
@Setter
@Type
public class OrderItemDetailDto
{
    private String id;
    private BigDecimal unitPrice;
    private Integer quantity;
    private ProductVariantDetailDto variant;
}
