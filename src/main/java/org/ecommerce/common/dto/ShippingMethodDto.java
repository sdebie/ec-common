package org.ecommerce.common.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ShippingMethodDto {
    public UUID id;
    public String name;
    public boolean isActive;
    public BigDecimal baseFee;
    public String estimatedDays;
}
