package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_price_upload_staged")
public class ProductPriceUploadStagedEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    public ProductPriceUploadBatchEntity batch;

    public String sku;

    @Column(name = "retail_price")
    public BigDecimal retailPrice;

    @Column(name = "wholesale_price")
    public BigDecimal wholesalePrice;

    @Column(name = "retail_sale_price")
    public BigDecimal retailSalePrice;

    @Column(name = "wholesale_sale_price")
    public BigDecimal wholesaleSalePrice;

    @Column(name = "validation_status")
    @Enumerated(EnumType.STRING)
    public ProductImportValidationStatusEn validationStatus;

    @Column(name = "validation_errors")
    public String validationErrors;

    @Column(name="has_changes")
    public Boolean hasChanges;

    public Boolean processed = false;
}

