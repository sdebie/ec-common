package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A single hydrated wishlist entry containing displayable product data
 * resolved from a variant ID.
 */
@Getter
@Setter
public class WishlistHydratedItemDto
{
    private UUID variantId;
    private String variantLabel;
    private String sku;
    private UUID productId;
    private String productName;
    private String productSlug;
    private String imagePath;
    private VariantPriceDto retailPrice;
    private VariantPriceDto wholesalePrice;
    private VariantPriceDto retailSalePrice;
    private VariantPriceDto wholesaleSalePrice;
}
