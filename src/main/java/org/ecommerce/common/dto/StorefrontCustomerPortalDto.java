package org.ecommerce.common.dto;

public class StorefrontCustomerPortalDto {
    public String email;
    public String shopperType;      // "RETAILER" | "WHOLESALER" | "GUEST"
    public String firstName;
    public String lastName;
    public String phone;            // nullable
    public AddressDto physicalAddress;  // nullable
    public AddressDto postalAddress;    // nullable
    public boolean hasPassword;

    public static class AddressDto {
        public String line1;
        public String line2;        // nullable
        public String suburb;       // nullable
        public String city;
        public String province;
        public String postalCode;
    }
}
