package org.ecommerce.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Enquiry form payload submitted by anonymous storefront visitors.
 * <p>
 * The recipient address is intentionally absent — it is resolved server-side
 * from {@code storefront.contact.enquiryEmail} to prevent open-relay abuse.
 * <p>
 * The {@code website} field is a honeypot: real users never fill it.
 */
public record ContactEnquiryRequestDto(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 40) String phone,
        @Size(max = 160) String company,
        @NotBlank @Size(max = 4000) String message,
        @Size(max = 200) String website
) {
}
