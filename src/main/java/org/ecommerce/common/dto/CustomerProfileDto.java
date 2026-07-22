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
    private String physicalAddressLine1;
    private String physicalAddressLine2;
    private String physicalSuburb;
    private String physicalCity;
    private String physicalProvince;
    private String physicalPostalCode;
    private String postalAddressLine1;
    private String postalAddressLine2;
    private String postalSuburb;
    private String postalCity;
    private String postalProvince;
    private String postalPostalCode;
    private String shopperType;
    private String status;
    private String additionalInfo;
    private boolean hasPassword;

}
