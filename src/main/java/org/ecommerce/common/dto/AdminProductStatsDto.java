package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for product count breakdown by status.
 * Used by the adminProductStats GraphQL query to populate stat cards.
 */
@Getter
@Setter
@Type
public class AdminProductStatsDto
{

    private long total;
    private long active;
    private long pending;
    private long disabled;

    public AdminProductStatsDto()
    {
    }
}
