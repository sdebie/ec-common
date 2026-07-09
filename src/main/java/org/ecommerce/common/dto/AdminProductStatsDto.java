package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for product count breakdown by status.
 * Used by the adminProductStats GraphQL query to populate stat cards.
 */
@Type
public class AdminProductStatsDto {

    public long total;
    public long active;
    public long pending;
    public long disabled;

    public AdminProductStatsDto() {
    }
}
