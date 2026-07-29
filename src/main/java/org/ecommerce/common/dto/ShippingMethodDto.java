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
}
