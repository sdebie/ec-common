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
    private String streetAddress;     // optional
    private String city;              // optional
    private String province;          // optional
    private String postalCode;        // optional
}
