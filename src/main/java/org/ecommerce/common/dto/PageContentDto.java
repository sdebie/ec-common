package org.ecommerce.common.dto;

import java.time.Instant;
import java.util.UUID;

public record PageContentDto(
        UUID id,
        String slug,
        String title,
        String category,
        String draftContent,
        String publishedContent,
        Instant publishedAt,
        Instant updatedAt
) {}
