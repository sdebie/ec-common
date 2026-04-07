package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_upload_staged")
public class ProductUploadStagedEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    public ProductUploadBatchEntity batch;

    @Column(name = "product_slug")
    public String productSlug;

    public String sku;
    public String name;

    public String description;
    @Column(name = "short_description")
    public String shortDescription;

    @Column(name = "retail_price")
    public BigDecimal retailPrice;

    @Column(name = "retail_sale_price")
    public BigDecimal retailSalePrice;

    @Column(name = "wholesale_price")
    public BigDecimal wholesalePrice;

    @Column(name = "wholesale_sale_price")
    public BigDecimal wholesaleSalePrice;

    @Column(name = "category_slug")
    public String categorySlug;

    @Column(name = "brand_slug")
    public String brandSlug;

    @Column(name = "stock")
    public Integer stock;

    @Column(name = "images")
    public String images;

    @Column(name = "attributes")
    public String attributes;

    @Column(name = "is_valid_category")
    public Boolean isValidCategory;

    @Column(name = "is_valid_brand")
    public Boolean isValidBrand;

    @Column(name = "is_new_product")
    public Boolean isNewProduct;

    @Column(name = "is_new_variant")
    public Boolean isNewVariant;

    @Column(name = "validation_errors")
    public String validationErrors;

    @Column(name = "image_errors")
    public String imageErrors;

    @Column(name = "validation_status")
    @Enumerated(EnumType.STRING)
    public ProductImportValidationStatusEn validationStatus;

    @Column(name="has_changes")
    public Boolean hasChanges;

    public Boolean processed = false;
}
