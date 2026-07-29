package org.ecommerce.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Quote request form payload submitted by anonymous storefront visitors.
 * <p>
 * The {@code website} field is a honeypot: real users never fill it.
 */
public record QuoteRequestSubmissionDto(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 40) String phone,
        @Size(max = 160) String company,
        @Size(max = 4000) String message,
        @Size(max = 200) String website,
        @NotEmpty @Valid List<QuoteRequestLineDto> items
) {
}
