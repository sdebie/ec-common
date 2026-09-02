package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * A single physical or postal address, shared by every wire DTO that exposes one:
 * StorefrontCustomerPortalDto, CustomerProfileDto, WholesaleApplicationDetailsDto,
 * WholesaleCustomerDto.
 */
@Getter
@Setter
public class AddressDto
{
    private String line1;
    private String line2;        // nullable
    private String suburb;       // nullable
    private String city;
    private String province;
    private String postalCode;
}
