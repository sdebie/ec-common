package org.ecommerce.common.dto;

import java.util.UUID;

/**
 * A single hydrated wishlist entry containing displayable product data
 * resolved from a variant ID.
 */
public class WishlistHydratedItemDto {
    public UUID variantId;
    public String variantLabel;
    public String sku;
    public UUID productId;
    public String productName;
    public String productSlug;
    public String imagePath;
    public VariantPriceDto retailPrice;
    public VariantPriceDto wholesalePrice;
    public VariantPriceDto retailSalePrice;
    public VariantPriceDto wholesaleSalePrice;
}
