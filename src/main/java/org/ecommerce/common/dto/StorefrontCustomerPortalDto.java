package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontCustomerPortalDto
{
    private String email;
    private String shopperType;      // "RETAILER" | "WHOLESALER" | "GUEST"
    private String firstName;
    private String lastName;
    private String phone;            // nullable
    private AddressDto physicalAddress;  // nullable
    private AddressDto postalAddress;    // nullable
    private boolean hasPassword;

    @Getter
    @Setter
    public static class AddressDto
    {
        private String line1;
        private String line2;        // nullable
        private String suburb;       // nullable
        private String city;
        private String province;
        private String postalCode;
    }
}
