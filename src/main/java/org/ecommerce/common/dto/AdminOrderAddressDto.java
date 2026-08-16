package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * The shipping address captured at checkout.
 * <p>
 * There is no country field: the order never records one, and this is a
 * single-country store.
 */
@Getter
@Setter
@Type
public class AdminOrderAddressDto
{
    private String street;
    private String city;
    private String province;
    private String postalCode;
}
