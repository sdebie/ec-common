package org.ecommerce.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * A single line item in a quote request submission.
 */
public record QuoteRequestLineDto(
        @NotNull UUID variantId,
        @Min(1) int quantity
) {
}
