package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Input;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Input("OrderDtoInput")
@Description("Input type for creating/updating an order")
public class OrderDto
{
    // Use wrapper type Long so GraphQL input can be nullable during create
    private Long orderId;
    // Frontend cart session identifier (UUID string)
    private String sessionId;
    // Prefer camelCase for GraphQL schema
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
}
