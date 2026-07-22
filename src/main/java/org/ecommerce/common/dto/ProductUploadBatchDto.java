package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProductUploadBatchDto
{
    private UUID id;
    private String filename;
    private String status;
    private Integer totalRows;
    private Integer processedRows;
    private Integer skippedRows;
    private Integer validationErrorCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String uploadedByUsername;
    private String approvedByUsername;
}
