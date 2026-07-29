package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Type
public class OrderResponseDto
{
    private String id;
    private String sessionId;
    private String status;
    private String createDate;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private CustomerDto customer;
    private List<OrderItemDetailDto> items = new ArrayList<>();
}
