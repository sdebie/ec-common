package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for a single customer row in the customer list table.
 * Used by the allCustomers GraphQL query.
 */
@Getter
@Setter
@Type
public class AdminCustomerListItemDto {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String status;
    private String shopperType;
    private String registeredAt;
    private String wholesaleApplicationStatus;

    public AdminCustomerListItemDto() {
    }
}
