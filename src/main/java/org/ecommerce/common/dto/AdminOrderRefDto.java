package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for a single order reference in a customer detail view.
 */
@Type
public class AdminOrderRefDto {

    public String id;
    public String reference;
    public String placedAt;
    public double total;
    public String status;

    public AdminOrderRefDto() {
    }
}
