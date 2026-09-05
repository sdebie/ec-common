package org.ecommerce.common.dto;

import java.time.Instant;
import java.util.UUID;

public record TestimonialDto(
        UUID id,
        String quote,
        String authorName,
        String authorTitle,
        boolean published,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt
)
{
}
