package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

@Type
public class OrderSummaryDto {
    public String id;
    public String orderDate;        // ISO-8601 string
    public String status;           // OrderStatusEn.name()
    public int itemCount;           // sum of quantities across all line items
    public double totalAmount;      // order total as double
}
