package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * Admin DTO for a single customer detail view.
 * Used by the adminCustomer GraphQL query.
 */
@Type
public class AdminCustomerDetailDto {

    public String id;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String status;
    public String shopperType;
    public String registeredAt;
    public WholesaleApplicationDetailsDto wholesaleApplication;
    public List<AdminOrderRefDto> recentOrders;

    public AdminCustomerDetailDto() {
    }
}
