package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderCreationItemDto
{
    private String variantId;
    private Integer quantity;
}
