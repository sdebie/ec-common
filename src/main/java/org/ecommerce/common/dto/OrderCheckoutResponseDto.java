package org.ecommerce.common.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderCheckoutResponseDto {

    private String orderId;
    private String sessionId;
    private List<OrderCheckoutLineDto> lines;
    private BigDecimal subtotal;
    private BigDecimal vatAmount;
    private BigDecimal shippingEstimate;
    private BigDecimal grandTotal;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<OrderCheckoutLineDto> getLines() {
        return lines;
    }

    public void setLines(List<OrderCheckoutLineDto> lines) {
        this.lines = lines;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getShippingEstimate() {
        return shippingEstimate;
    }

    public void setShippingEstimate(BigDecimal shippingEstimate) {
        this.shippingEstimate = shippingEstimate;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }
}
