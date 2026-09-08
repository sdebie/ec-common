package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.UUID;

/**
 * Display data for one wishlist variant: catalogue fields assembled from the
 * variant, its product, prices and thumbnail — not the membership row.
 */
@Type
@Getter
@Setter
public class WishlistItemDto
{
    @Description("Variant UUID")
    private UUID variantId;

    @Description("Variant attributes JSON")
    private String variantLabel;

    private String sku;

    @Description("Product UUID")
    private UUID productId;

    private String productName;

    private String productSlug;

    private String imagePath;

    private VariantPriceDto retailPrice;

    private VariantPriceDto wholesalePrice;

    private VariantPriceDto retailSalePrice;

    private VariantPriceDto wholesaleSalePrice;

    @Description("True when the product is ACTIVE, the variant is ACTIVE, and stock is greater than zero")
    private Boolean inStock;

    @Description("True when the parent product is ACTIVE")
    private Boolean productActive;
}
