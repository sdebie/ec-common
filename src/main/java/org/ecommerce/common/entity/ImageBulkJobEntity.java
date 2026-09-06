package org.ecommerce.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ImageBulkJobStatusEn;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "image_bulk_jobs")
public class ImageBulkJobEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "destination_directory")
    private String destinationDirectory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImageBulkJobStatusEn status;

    @Column(name = "uploaded_count", nullable = false)
    private Integer uploadedCount = 0;

    @Column(name = "skipped_count", nullable = false)
    private Integer skippedCount = 0;

    @Column(name = "failed_count", nullable = false)
    private Integer failedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private StaffUserEntity uploadedBy;
}
