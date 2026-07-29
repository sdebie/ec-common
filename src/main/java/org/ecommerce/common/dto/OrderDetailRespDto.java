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

@Getter
@Setter
@Type
public class OrderDetailRespDto
{
    // OrderEntity fields
    private UUID id;
    private CustomerDto customerEntity;
    private BigDecimal totalAmount;
    private UUID sessionId;
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
        private String changedBy;
        private LocalDateTime createdAt;
    }
}