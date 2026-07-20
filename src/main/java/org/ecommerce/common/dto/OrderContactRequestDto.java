package org.ecommerce.common.dto;

public class OrderContactRequestDto {
    public String email;
    public String firstName;
    public String lastName;
    public String shippingMethodId;  // optional
    public String streetAddress;     // optional
    public String city;              // optional
    public String province;          // optional
    public String postalCode;        // optional
}
