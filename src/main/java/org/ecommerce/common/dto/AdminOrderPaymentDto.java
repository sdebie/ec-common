package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;

/**
 * The most recent payment-gateway callback recorded against this order.
 */
@Getter
@Setter
@Type
public class AdminOrderPaymentDto
{
    private String gateway;
    private String externalReference;
    private BigDecimal amountGross;
    private String status;
    private String receivedAt;
}
