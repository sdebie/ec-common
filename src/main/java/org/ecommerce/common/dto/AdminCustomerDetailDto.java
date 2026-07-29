package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * Admin DTO for a single customer detail view.
 * Used by the adminCustomer GraphQL query.
 */
@Getter
@Setter
@Type
public class AdminCustomerDetailDto
{

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String status;
    private String shopperType;
    private String registeredAt;
    private WholesaleApplicationDetailsDto wholesaleApplication;
    private List<AdminOrderRefDto> recentOrders;

    public AdminCustomerDetailDto()
    {
    }
}
