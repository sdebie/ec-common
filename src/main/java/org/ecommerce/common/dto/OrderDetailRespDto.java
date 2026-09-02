package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;
import org.ecommerce.common.enums.OrderStatusEn;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The shopper-facing order-detail shape ({@code getOrderDetail} — guest-order-authorization
 * S1). Deliberately carries no {@code sessionId}: that field is {@code orderBySessionId}'s
 * credential, and exposing it here would let anyone who can read one order read every
 * order through it — closing S1 alone, not S2 (Requirement 4.1). Its {@code statusHistory}
 * rows carry no {@code changedBy} either — staff members' real names — because this surface
 * admits no staff caller, which is what closes S2 (Requirement 1.6/4.4).
 */
@Getter
@Setter
@Type
public class OrderDetailRespDto
{
    // OrderEntity fields
    private UUID id;
    private CustomerDto customerEntity;
    private BigDecimal totalAmount;
    private OrderStatusEn status;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingProvince;
    private String shippingPostalCode;
    private List<OrderItemDetailDto> items = new ArrayList<>(); // Changed to OrderItemDetailDto
    private LocalDateTime createdAt;

    // OrderStatusHistoryEntity fields wrapped as detail rows
    private List<OrderStatusHistoryDetailRespDto> statusHistory = new ArrayList<>();

    @Getter
    @Setter
    @Type
    public static class OrderStatusHistoryDetailRespDto
    {
        private UUID id;
        private OrderStatusEn status;
        private String comment;
        private LocalDateTime createdAt;
    }
}