package org.ecommerce.common.dto;

import java.util.UUID;

public class QuoteRequestItemDto {

    private UUID variantId;
    private String productNameSnapshot;
    private String variantSkuSnapshot;
    private int quantity;

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }

    public String getVariantSkuSnapshot() { return variantSkuSnapshot; }
    public void setVariantSkuSnapshot(String variantSkuSnapshot) { this.variantSkuSnapshot = variantSkuSnapshot; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
