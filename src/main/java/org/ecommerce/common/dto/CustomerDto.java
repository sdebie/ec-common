package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple DTO for customer information coming from the frontend.
 * For now, it only contains the email address.
 */
@Getter
@Setter
public class CustomerDto
{
    private String email;

}
