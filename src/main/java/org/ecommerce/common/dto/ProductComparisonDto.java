package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.util.UUID;

@Getter
@Setter
public class ProductComparisonDto
{
    private UUID stagedId;
    private String sku;

    // CSV-layout fields for import review
    private String categorySlug;
    private String brandSlug;
    private Integer currentStock;
    private Integer proposedStock;
    private String currentImages;
    private String proposedImages;
    private String currentAttributes;
    private String proposedAttributes;
    private String validationErrors;
    private ProductImportValidationStatusEn validationStatus;
    private String imageErrors;

    // Name comparison
    private String currentName;
    private String proposedName;

    private String currentDescription;
    private String proposedDescription;
    private String currentShortDescription;
    private String proposedShortDescription;

    private boolean isValidCategory;
    private boolean isValidBrand;
    private boolean isNewProduct;
    private boolean isNewVariant;
    public boolean hasChanges;
}
