package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerProfileDto
{
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private AddressDto physicalAddress;
    private AddressDto postalAddress;
    private String shopperType;
    private String status;
    private String additionalInfo;
    private boolean hasPassword;

}
