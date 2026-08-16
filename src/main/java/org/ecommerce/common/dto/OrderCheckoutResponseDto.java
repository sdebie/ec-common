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
    /**
     * The order capability token (guest-order-authorization Requirement 2.1) — minted
     * fresh on every response that carries this DTO, including a checkout-idempotency
     * replay (Requirement 3.2). There is no stored token to return, by design (no
     * migration, design.md §2), so a replay at hour 20 of the 24-hour window still gets
     * a token usable for a full 60 minutes from that moment, not one already expired.
     */
    private String orderToken;

}
