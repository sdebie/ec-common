package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;
import org.ecommerce.common.enums.OrderStatusEn;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The guest checkout success-page poll (S2′, guest-order-authorization Requirement 4.3)
 * — replaces {@code orderBySessionId}. Returns only what the page renders: status, total
 * and creation time. Deliberately does not carry the customer's email or line items,
 * both of which {@code orderBySessionId} returned and the page never read.
 */
@Getter
@Setter
@Type
public class OrderStatusRespDto
{
    private UUID id;
    private OrderStatusEn status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
