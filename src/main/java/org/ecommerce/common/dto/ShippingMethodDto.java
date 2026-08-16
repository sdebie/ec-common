package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ShippingMethodDto
{
    private UUID id;
    private String name;
    private boolean isActive;
    private BigDecimal baseFee;
    private String estimatedDays;

    /**
     * Whether the method needs a delivery address. Defaults true so an omitted value
     * fails safe — this DTO is also the admin {@code ShippingMethodDtoInput}, and it is
     * mapped onto the existing entity, so a caller that leaves it out would otherwise
     * write {@code false} and quietly stop asking for addresses on a courier method.
     * <p>
     * Wire key is {@code requiresAddress} on both sides: the field does not start with
     * {@code is}, so neither Jackson nor SmallRye's prefix-stripping rewrites it.
     */
    private boolean requiresAddress = true;
}
