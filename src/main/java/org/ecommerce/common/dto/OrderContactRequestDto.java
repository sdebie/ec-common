package org.ecommerce.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderContactRequestDto
{
    // Not @NotBlank, unlike ContactEnquiryRequestDto/QuoteRequestSubmissionDto's
    // equivalent fields: a PATCH here may omit any of these to leave it
    // unchanged (OrderContactResource null-guards each before persisting), so
    // only the shape of a PROVIDED value is validated, never its presence.
    @Email
    @Size(max = 254)
    private String email;

    @Size(max = 120)
    private String firstName;

    @Size(max = 120)
    private String lastName;

    private String shippingMethodId;  // optional

    // Same not-@NotBlank reasoning as above: OrderContactResource's own
    // firstNonBlank/address-required check (not this DTO) is what decides
    // whether a delivery method's address requirement is satisfied, so these
    // stay omittable here regardless of length caps.
    @Size(max = 200)
    private String streetAddress;     // optional

    @Size(max = 120)
    private String city;              // optional

    @Size(max = 120)
    private String province;          // optional

    // 20 is deliberately generous, not South-Africa-specific (law 1: no
    // client-specific code) — no format/digit pattern, since a future
    // non-SA client's postal codes are not this codebase's business to shape.
    @Size(max = 20)
    private String postalCode;        // optional
}
