package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for a single customer row in the customer list table.
 * Used by the allCustomers GraphQL query.
 */
@Type
public class AdminCustomerListItemDto {

    public String id;
    public String firstName;
    public String lastName;
    public String email;
    public String status;
    public String shopperType;
    public String registeredAt;
    public String wholesaleApplicationStatus;

    public AdminCustomerListItemDto() {
    }
}
