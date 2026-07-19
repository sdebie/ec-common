package org.ecommerce.common.dto;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for the wishlist hydration endpoint.
 * Accepts a list of variant IDs to resolve into displayable product data.
 */
public class WishlistHydrationRequestDto {
    public List<UUID> variantIds;
}
