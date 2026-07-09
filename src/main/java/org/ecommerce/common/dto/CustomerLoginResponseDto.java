package org.ecommerce.common.dto;

public class CustomerLoginResponseDto {
    public String token;
    public String email;
    public String firstName;
    public String lastName;
    public String shopperType;  // "GUEST" | "RETAILER" | "WHOLESALER"
    public String status;       // "PENDING" | "ACTIVE" | "DISABLED"
}
