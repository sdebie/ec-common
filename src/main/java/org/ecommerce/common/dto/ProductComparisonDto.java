package org.ecommerce.common.dto;

import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.util.UUID;

@SuppressWarnings("unused")
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
    public ProductImportValidationStatusEn validationStatus;
    public String imageErrors;

    // Name comparison
    public String currentName;
    public String proposedName;

    public String currentDescription;
    public String proposedDescription;
    public String currentShortDescription;
    public String proposedShortDescription;

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
