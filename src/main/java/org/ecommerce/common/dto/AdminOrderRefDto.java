package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for a single order reference in a customer detail view.
 */
@Getter
@Setter
@Type
public class AdminOrderRefDto
{

    private String id;
    private String reference;
    private String placedAt;
    private double total;
    private String status;

    public AdminOrderRefDto()
    {
    }
}
