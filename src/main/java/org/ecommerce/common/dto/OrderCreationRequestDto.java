package org.ecommerce.common.dto;

import java.util.List;

public class OrderCreationRequestDto {

    private List<OrderCreationItemDto> items;

    public List<OrderCreationItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderCreationItemDto> items) {
        this.items = items;
    }
}
