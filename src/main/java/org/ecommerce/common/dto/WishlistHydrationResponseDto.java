package org.ecommerce.common.dto;

import java.util.List;

/**
 * Response payload for the wishlist hydration endpoint.
 * Contains the list of hydrated wishlist items resolved from variant IDs.
 */
public class WishlistHydrationResponseDto {
    public List<WishlistHydratedItemDto> items;

    public WishlistHydrationResponseDto() {}

    public WishlistHydrationResponseDto(List<WishlistHydratedItemDto> items) {
        this.items = items;
    }
}
