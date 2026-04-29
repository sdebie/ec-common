package org.ecommerce.common.dto;

import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductPriceComparisonDto {
    public UUID stagedId;
    public String sku;

    public String validationErrors;
    public ProductImportValidationStatusEn validationStatus;

    public BigDecimal currentRetailPrice;
    public BigDecimal proposedRetailPrice;

    public BigDecimal currentWholesalePrice;
    public BigDecimal proposedWholesalePrice;

    public boolean hasChanges;
}
