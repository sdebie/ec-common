package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;
import org.ecommerce.common.entity.CustomerEntity;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.OrderItemEntity;
import org.ecommerce.common.enums.OrderStatusEn;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Type
public class OrderDetailRespDto {
    // OrderEntity fields
    public UUID id;
    public CustomerEntity customerEntity;
    public BigDecimal totalAmount;
    public UUID sessionId;
    public OrderStatusEn status;
    public String shippingPhone;
    public String shippingAddressLine1;
    public String shippingAddressLine2;
    public String shippingCity;
    public String shippingProvince;
    public String shippingPostalCode;
    public List<OrderItemEntity> items = new ArrayList<>();
    public LocalDateTime createdAt;

    // OrderStatusHistoryEntity fields wrapped as detail rows
    public List<OrderStatusHistoryDetailRespDto> statusHistory = new ArrayList<>();

    @Type
    public static class OrderStatusHistoryDetailRespDto {
        public UUID id;
        public OrderEntity order;
        public OrderStatusEn status;
        public String comment;
        public String changedBy;
        public LocalDateTime createdAt;
    }
}
