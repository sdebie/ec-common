package org.ecommerce.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quote_request_items")
public class QuoteRequestItemEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_request_id", referencedColumnName = "id", nullable = false)
    private QuoteRequestEntity quoteRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", referencedColumnName = "id")
    private ProductVariantEntity variant;

    @Column(name = "product_name_snapshot", length = 255, nullable = false)
    private String productNameSnapshot;

    @Column(name = "variant_sku_snapshot", length = 100)
    private String variantSkuSnapshot;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;
}
