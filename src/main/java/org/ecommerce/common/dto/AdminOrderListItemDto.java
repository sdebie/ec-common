package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;

/**
 * One row of the admin order list.
 */
@Getter
@Setter
@Type
public class AdminOrderListItemDto
{
    private String id;
    private String reference;
    private String customerName;
    private String placedAt;
    private int itemCount;
    private BigDecimal total;
    private String status;
}
