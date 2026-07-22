package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderContactRequestDto
{
    private String email;
    private String firstName;
    private String lastName;
    private String shippingMethodId;  // optional
    private String streetAddress;     // optional
    private String city;              // optional
    private String province;          // optional
    private String postalCode;        // optional
}
