package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductPriceComparisonDto
{
    private UUID stagedId;
    private String sku;

    private String validationErrors;
    private ProductImportValidationStatusEn validationStatus;

    private BigDecimal currentRetailPrice;
    private BigDecimal proposedRetailPrice;

    private BigDecimal currentWholesalePrice;
    private BigDecimal proposedWholesalePrice;

    private boolean hasChanges;
}
