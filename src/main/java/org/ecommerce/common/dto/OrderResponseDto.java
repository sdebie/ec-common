package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Type
public class OrderResponseDto
{
    public String id;
    public String sessionId;
    public String status;
    public BigDecimal totalAmount;
    public CustomerDto customer;
    public List<OrderItemResponseDto> items = new ArrayList<>();
}

