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
 * The shopper-facing order shape: order creation/status-change ({@code updateOrderStatus},
 * {@code allOrders}), order-detail lookup ({@code getOrderDetail} — guest-order-authorization
 * S1), and the guest checkout success-page poll ({@code orderStatus} — S2′) all return this one
 * type via {@code OrderMapper}'s {@code toOrderDto}/{@code toDetailDto}/{@code toStatusDto},
 * each populating only the fields its caller needs. Two exclusions are security-load-bearing,
 * not incidental:
 * <ul>
 *   <li>{@code sessionId} is the guest-checkout credential. {@code toDetailDto} and
 *   {@code toStatusDto} must never populate it — exposing it on either read path would let
 *   anyone who can read one order read every order through it (Requirement 4.1).</li>
 *   <li>{@code customerEmail}, {@code items}, the {@code shipping*} fields, and
 *   {@code statusHistory} are excluded from {@code toStatusDto} — the success page never
 *   reads them (Requirement 4.3), so the guest poll stays minimal by construction.</li>
 * </ul>
 */
@Getter
@Setter
@Type
public class OrderDetailDto
{
    private UUID id;
    private String sessionId;
    private String customerEmail;
    private OrderStatusEn status;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCity;
    private String shippingProvince;
    private String shippingPostalCode;
    private List<OrderItemDetailDto> items = new ArrayList<>();
    private List<OrderStatusHistoryDto> statusHistory = new ArrayList<>();

    @Getter
    @Setter
    @Type
    public static class OrderStatusHistoryDto
    {
        private UUID id;
        private OrderStatusEn status;
        private String comment;
        private LocalDateTime createdAt;
    }
}
