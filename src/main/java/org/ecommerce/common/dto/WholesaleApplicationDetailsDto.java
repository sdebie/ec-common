package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.WholesaleApplicationStatusEn;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
public class WholesaleApplicationDetailsDto
{
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;

    private AddressDto physicalAddress;
    private AddressDto postalAddress;

    private String companyName;
    private String tradingName;
    private String companyPhone;
    private String companyEmail;
    private String vatNumber;
    private String regNumber;
    private String financeContactName;
    private String financeContactEmail;
    private String financeContactPhone;
    private Boolean purchaseOrderRequired;
    private String notes;
    private String applicantEmail;

    private String rejectionReason;

    private WholesaleApplicationStatusEn status;
    private OffsetDateTime createdAt;
    private OffsetDateTime processedAt;
    private UUID customerId;

}

