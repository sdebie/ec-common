package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "quote_request_items")
public class QuoteRequestItemEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_request_id", referencedColumnName = "id", nullable = false)
    public QuoteRequestEntity quoteRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "variant_id", referencedColumnName = "id", nullable = true)
    public ProductVariantEntity variant;

    @Column(name = "product_name_snapshot", length = 255, nullable = false)
    public String productNameSnapshot;

    @Column(name = "variant_sku_snapshot", length = 100)
    public String variantSkuSnapshot;

    @Column(name = "quantity", nullable = false)
    public int quantity;
}
