package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The admin order detail view: the list row plus everything staff need to
 * fulfil or query a single order.
 * <p>
 * The money breakdown is derived server-side from the order's own persisted
 * lines and its selected delivery method, through the same
 * {@code OrderService.computeTotals} path that produced the amount charged —
 * the client never recomputes it.
 */
@Getter
@Setter
@Type
public class AdminOrderDetailDto
{
    private String id;
    private String reference;
    private String customerName;
    private String customerEmail;
    private String placedAt;
    private int itemCount;
    private BigDecimal total;
    private String status;

    private AdminOrderAddressDto shippingAddress;
    private List<AdminOrderLineItemDto> lineItems = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal vatAmount;
    private BigDecimal grandTotal;

    private List<AdminOrderStatusHistoryDto> statusHistory = new ArrayList<>();
}
