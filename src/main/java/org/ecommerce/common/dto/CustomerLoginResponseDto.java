package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerLoginResponseDto
{
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String shopperType;  // "GUEST" | "RETAILER" | "WHOLESALER"
    private String status;       // "PENDING" | "ACTIVE" | "DISABLED"
}
