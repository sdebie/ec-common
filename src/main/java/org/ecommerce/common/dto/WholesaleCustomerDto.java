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
    private AddressDto physicalAddress;
    private AddressDto postalAddress;
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

