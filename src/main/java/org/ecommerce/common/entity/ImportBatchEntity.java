package org.ecommerce.common.entity;

import org.ecommerce.common.enums.ProductUploadStatusEn;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Common interface for all import batch entities.
 * Implemented by ProductImportBatchEntity and ProductPriceImportBatchEntity.
 */
public interface ImportBatchEntity {
    UUID getId();

    String getFilename();

    ProductUploadStatusEn getProductUploadStatusEn();

    void setProductUploadStatusEn(ProductUploadStatusEn status);

    Integer getTotalRows();

    void setTotalRows(Integer totalRows);

    Integer getProcessedRows();

    void setProcessedRows(Integer processedRows);

    Integer getSkippedRows();

    void setSkippedRows(Integer skippedRows);

    Integer getValidationErrorCount();

    void setValidationErrorCount(Integer count);

    LocalDateTime getCompletedAt();

    void setCompletedAt(LocalDateTime completedAt);
}
