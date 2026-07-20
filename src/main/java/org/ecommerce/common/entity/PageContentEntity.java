package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "page_content")
public class PageContentEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "slug", nullable = false, unique = true)
    public String slug;

    @Column(name = "title", nullable = false)
    public String title;

    @Column(name = "category", nullable = false)
    public String category;

    @Column(name = "draft_content", columnDefinition = "TEXT")
    public String draftContent;

    @Column(name = "published_content", columnDefinition = "TEXT")
    public String publishedContent;

    @Column(name = "published_at")
    public OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt = OffsetDateTime.now();
}
