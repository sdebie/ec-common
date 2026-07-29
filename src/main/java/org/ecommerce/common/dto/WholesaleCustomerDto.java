package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.WholesaleCustomerStatusEn;

import java.util.UUID;

@Setter
@Getter
public class WholesaleCustomerDto
{
    private UUID id;
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
    private String companyName;
    private String vatNumber;
    private String regNumber;
    private String notes;
    private WholesaleCustomerStatusEn status;
    private String applicantEmail;
    private String tradingName;
    private String companyPhone;
    private String companyEmail;
    private String financeContactName;
    private String financeContactEmail;
    private String financeContactPhone;
    private Boolean purchaseOrderRequired;

}

