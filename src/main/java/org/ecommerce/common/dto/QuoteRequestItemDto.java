package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class QuoteRequestItemDto
{
    private UUID id;
    private UUID variantId;
    private String productNameSnapshot;
    private String variantSkuSnapshot;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
