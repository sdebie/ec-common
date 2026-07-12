package org.ecommerce.common.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PageContentSummaryDto(
        UUID id,
        String slug,
        String title,
        String category,
        OffsetDateTime publishedAt,
        OffsetDateTime updatedAt,
        boolean hasUnpublishedChanges
) {}
