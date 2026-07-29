package org.ecommerce.common.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTestimonialRequest(
        @NotBlank String quote,
        @NotBlank String authorName,
        String authorTitle,
        int sortOrder,
        boolean published
) {}
