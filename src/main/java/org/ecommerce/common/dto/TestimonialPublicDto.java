package org.ecommerce.common.dto;

import java.util.UUID;

public record TestimonialPublicDto(
        UUID id,
        String quote,
        String authorName,
        String authorTitle
) {}
