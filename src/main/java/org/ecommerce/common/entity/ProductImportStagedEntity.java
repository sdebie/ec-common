package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_import_staged")
public class ProductImportStagedEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private ProductImportBatchEntity batch;

    @Column(name = "product_slug")
    private String productSlug;

    private String sku;
    private String name;

    private String description;
    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "category_slug")
    private String categorySlug;

    @Column(name = "brand_slug")
    private String brandSlug;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "images")
    private String images;

    @Column(name = "attributes")
    private String attributes;

    @Column(name = "is_valid_category")
    private Boolean isValidCategory;

    @Column(name = "is_valid_brand")
    private Boolean isValidBrand;

    @Column(name = "is_new_product")
    private Boolean isNewProduct;

    @Column(name = "is_new_variant")
    private Boolean isNewVariant;

    @Column(name = "validation_errors")
    private String validationErrors;

    @Column(name = "image_errors")
    private String imageErrors;

    @Column(name = "validation_status")
    @Enumerated(EnumType.STRING)
    private ProductImportValidationStatusEn validationStatus;

    @Column(name = "has_changes")
    private Boolean hasChanges;

    // Current (live) values captured at import time for comparison
    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "current_images")
    private String currentImages;

    @Column(name = "current_attributes")
    private String currentAttributes;

    @Column(name = "current_name")
    private String currentName;

    @Column(name = "current_description")
    private String currentDescription;

    @Column(name = "current_short_description")
    private String currentShortDescription;

    private Boolean processed = false;
}
