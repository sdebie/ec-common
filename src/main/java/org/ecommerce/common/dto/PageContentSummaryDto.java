package org.ecommerce.common.dto;

import java.time.Instant;
import java.util.UUID;

public record PageContentSummaryDto(
        UUID id,
        String slug,
        String title,
        String category,
        Instant publishedAt,
        Instant updatedAt,
        boolean hasUnpublishedChanges
) {}
