package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.ProductUploadStatusEn;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_upload_batches")
public class ProductUploadBatchEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    public String filename;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public ProductUploadStatusEn productUploadStatusEn;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    public StaffUserEntity uploadedBy;

    @Column(name="total_rows")
    public Integer totalRows;

    @Column(name="processed_rows")
    public Integer processedRows = 0;

    @Column(name="skipped_rows")
    public Integer skippedRows = 0;

    @Column(name="validation_error_count")
    public Integer validationErrorCount = 0;

    @Column(name="created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
