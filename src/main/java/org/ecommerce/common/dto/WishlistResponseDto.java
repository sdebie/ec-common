package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class WishlistResponseDto
{
    private List<UUID> variantIds;
}
