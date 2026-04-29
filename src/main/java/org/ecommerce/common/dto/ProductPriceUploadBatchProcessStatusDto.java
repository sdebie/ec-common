package org.ecommerce.common.dto;

import java.util.UUID;

public class ProductPriceUploadBatchProcessStatusDto {
    public UUID batchId;
    public String status;
    public Integer totalRows;
    public Long stagedRows;
    public Long processedRows;
    public Long skippedRows;
    public Integer validationErrorCount;
    public boolean completed;
}
