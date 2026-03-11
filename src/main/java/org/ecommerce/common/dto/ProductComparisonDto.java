package org.ecommerce.common.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductComparisonDto {
    public UUID stagedId;
    public String sku;

    // Name comparison
    public String currentName;
    public String proposedName;

    // Price comparison
    public BigDecimal currentRetailPrice;
    public BigDecimal proposedRetailPrice;

    public BigDecimal currentRetailSalePrice;
    public BigDecimal proposedRetailSalePrice;

    public BigDecimal currentWholesalePrice;
    public BigDecimal proposedWholesalePrice;

    public BigDecimal currentWholesaleSalePrice;
    public BigDecimal proposedWholesaleSalePrice;

    public boolean isNewProduct;
    public boolean hasChanges;

    // Keep GraphQL schema backward-compatible with clients querying `isNewProduct`.
    public boolean getIsNewProduct() {
        return isNewProduct;
    }
}
