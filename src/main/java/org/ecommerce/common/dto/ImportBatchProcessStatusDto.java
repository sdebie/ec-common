package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ImportBatchProcessStatusDto
{
    private UUID batchId;
    private String status;
    private Integer totalRows;
    private Long stagedRows;
    private Long processedRows;
    private Long skippedRows;
    private Integer validationErrorCount;
    private boolean completed;
}
