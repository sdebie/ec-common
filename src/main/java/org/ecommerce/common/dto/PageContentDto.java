package org.ecommerce.common.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageContentDto(
        UUID id,
        String slug,
        String title,
        String category,
        String draftContent,
        String publishedContent,
        OffsetDateTime publishedAt,
        OffsetDateTime updatedAt
) {}
