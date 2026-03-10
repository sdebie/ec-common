package org.ecommerce.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductUploadBatchDto {
    public UUID id;
    public String filename;
    public String status;
    public Integer totalRows;
    public LocalDateTime createdAt;
    public String uploadedByUsername;
}
