package org.ecommerce.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Write shape for a testimonial, shared by create and update.
 * <p>
 * Both operations accept every field and replace the record wholesale — there is no
 * partial-update semantic and no create-only or update-only field, so a single type
 * carries both. Splitting it again would reintroduce two identical declarations that
 * can drift.
 */
public record TestimonialRequest(
        @NotBlank String quote,
        @NotBlank String authorName,
        String authorTitle,
        int sortOrder,
        boolean published
) {}
