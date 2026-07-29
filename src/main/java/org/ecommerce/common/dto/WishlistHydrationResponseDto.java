package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response payload for the wishlist hydration endpoint.
 * Contains the list of hydrated wishlist items resolved from variant IDs.
 */
@Getter
@Setter
public class WishlistHydrationResponseDto
{
    private List<WishlistHydratedItemDto> items;

    public WishlistHydrationResponseDto()
    {
    }

    public WishlistHydrationResponseDto(List<WishlistHydratedItemDto> items)
    {
        this.items = items;
    }
}
