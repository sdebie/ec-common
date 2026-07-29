package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for the wishlist hydration endpoint.
 * Accepts a list of variant IDs to resolve into displayable product data.
 */
@Getter
@Setter
public class WishlistHydrationRequestDto
{
    private List<UUID> variantIds;
}
