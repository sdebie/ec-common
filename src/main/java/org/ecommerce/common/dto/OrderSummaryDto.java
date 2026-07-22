package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

@Getter
@Setter
@Type
public class OrderSummaryDto
{
    private String id;
    private String orderDate;        // ISO-8601 string
    private String status;           // OrderStatusEn.name()
    private int itemCount;           // sum of quantities across all line items
    private double totalAmount;      // order total as double
}
