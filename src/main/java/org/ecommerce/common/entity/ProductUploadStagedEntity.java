package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

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

    public String sku;
    public String name;

    @Column(name = "retail_price")
    public BigDecimal retailPrice;

    @Column(name = "wholesale_price")
    public BigDecimal wholesalePrice;

    @Column(name = "category_name")
    public String categoryName;

    @Column(name = "is_new_product")
    public Boolean isNewProduct;

    @Column(name = "validation_errors")
    public String validationErrors;

    @Column(name = "validation_status")
    public String validationStatus;

    @Column(name="has_changes")
    public Boolean hasChanges;

    public Boolean processed = false;
}
