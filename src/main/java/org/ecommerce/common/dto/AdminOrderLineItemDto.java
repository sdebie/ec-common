package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;

/**
 * One purchased line on the admin order detail view.
 * <p>
 * Prices are the ones persisted on the order line, not current catalogue
 * prices — an order shows what was charged, not what the product costs today.
 */
@Getter
@Setter
@Type
public class AdminOrderLineItemDto
{
    private String id;
    private String productName;
    private String variantSku;
    private String thumbnailUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
}
