package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_price_import_staged")
public class ProductPriceImportStagedEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private ProductPriceImportBatchEntity batch;

    private String sku;

    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    @Column(name = "wholesale_price")
    private BigDecimal wholesalePrice;

    @Column(name = "validation_status")
    @Enumerated(EnumType.STRING)
    private ProductImportValidationStatusEn validationStatus;

    @Column(name = "validation_errors")
    private String validationErrors;

    @Column(name = "has_changes")
    private Boolean hasChanges;

    @Column(name = "current_retail_price")
    private BigDecimal currentRetailPrice;

    @Column(name = "current_wholesale_price")
    private BigDecimal currentWholesalePrice;

    private Boolean processed = false;
}

