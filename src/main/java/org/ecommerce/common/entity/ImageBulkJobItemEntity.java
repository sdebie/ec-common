package org.ecommerce.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ImageBulkJobItemStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "image_bulk_job_items")
public class ImageBulkJobItemEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private ImageBulkJobEntity job;

    @Column(name = "relative_path", nullable = false, length = 512)
    private String relativePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImageBulkJobItemStatusEn status;

    @Column(name = "error")
    private String error;
}
