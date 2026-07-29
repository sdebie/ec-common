package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderCreationRequestDto
{
    private List<OrderCreationItemDto> items;
}
