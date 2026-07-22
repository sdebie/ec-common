package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Input;

import java.math.BigDecimal;

@Getter
@Setter
@Input("OrderItemDtoInput")
@Description("Input item for an order line. Name is the product name sent by client.")
public class OrderItemDto
{
    private BigDecimal unitPrice; // price per unit
    private Integer quantity;     // quantity of units
    private String name;          // product name (informational)
    @Description("Selected product variant ID (UUID string)")
    private String variant;
}
