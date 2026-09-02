package org.ecommerce.common.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single requested item's staff-quoted unit price, submitted when previewing or
 * generating a quote. itemId identifies the QuoteRequestItemEntity row, not the variant —
 * a variant can be null (deleted product), but every request item still has its own id.
 */
public record QuoteItemPriceInput(@NotNull UUID itemId, @NotNull @DecimalMin("0.0") BigDecimal unitPrice)
{
}
