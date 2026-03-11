package org.ecommerce.common.dto;

import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductComparisonDto {
    public UUID stagedId;
    public String sku;

    // CSV-layout fields for import review
    public String categorySlug;
    public String brandSlug;
    public Integer currentStock;
    public Integer proposedStock;
    public String currentImages;
    public String proposedImages;
    public String currentAttributes;
    public String proposedAttributes;
    public String validationErrors;
    public String validationStatus;
    public String imageErrors;

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

    public boolean isValidCategory;
    public boolean isValidBrand;
    public boolean isNewProduct;
    public boolean isNewVariant;
    public boolean hasChanges;

    // Keep GraphQL schema backward-compatible with clients querying `isNewProduct`.
    public boolean getIsNewProduct() {
        return isNewProduct;
    }
    public boolean getIsNewVariant() {
        return isNewVariant;
    }
    public boolean getIsValidCategory() {
        return isValidCategory;
    }
    public boolean getIsValidBrand() {
        return isValidBrand;
    }
}
