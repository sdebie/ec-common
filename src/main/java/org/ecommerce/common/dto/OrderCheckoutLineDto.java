package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderCheckoutLineDto
{
    private String variantId;
    private String name;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
