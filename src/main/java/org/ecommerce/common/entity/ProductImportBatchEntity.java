package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ImportSourceTypeEn;
import org.ecommerce.common.enums.ProductUploadStatusEn;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_import_batches")
public class ProductImportBatchEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "filename")
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductUploadStatusEn productUploadStatusEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ImportSourceTypeEn importSourceTypeEn = ImportSourceTypeEn.FILE;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private StaffUserEntity uploadedBy;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "processed_rows")
    private Integer processedRows = 0;

    @Column(name = "skipped_rows")
    private Integer skippedRows = 0;

    @Column(name = "validation_error_count")
    private Integer validationErrorCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
