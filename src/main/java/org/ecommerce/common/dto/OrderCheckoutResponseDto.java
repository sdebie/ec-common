package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class OrderCheckoutResponseDto
{
    private String orderId;
    private String sessionId;
    private List<OrderCheckoutLineDto> lines;
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal shippingEstimate;
    private BigDecimal grandTotal;

}
