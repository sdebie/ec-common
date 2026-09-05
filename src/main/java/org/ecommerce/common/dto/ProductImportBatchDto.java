package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ProductImportBatchDto
{
    private UUID id;
    private String filename;
    private String status;
    private String importSourceType;
    private Integer totalRows;
    private Integer processedRows;
    private Integer skippedRows;
    private Integer validationErrorCount;
    private Instant createdAt;
    private Instant completedAt;
    private String uploadedByUsername;
    private String approvedByUsername;
}
