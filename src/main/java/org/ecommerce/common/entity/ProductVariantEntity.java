package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.ecommerce.common.enums.CustomerTypeEn;
import org.ecommerce.common.enums.PriceTypeEn;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariantEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    public ProductEntity product;

    @Column(nullable = false, unique = true)
    public String sku;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VariantPricesEntity> variantPrices = Collections.emptyList();

    @Column(name = "stock_quantity")
    public Integer stockQuantity;

    // Store JSONB as String to keep mapping minimal; can be mapped to JSON later if needed
    @Column(name = "attributes")
    public String attributesJson;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    public BigDecimal weightKg;

    // Helper method to fetch a variant together with its Product entity
    public static ProductVariantEntity findByIdWithProduct(UUID id) {
        if (id == null) return null;
        return find("select v from ProductVariantEntity v left join fetch v.product where v.id = ?1", id).firstResult();
    }

    // Helper to fetch multiple variants with product in one go
    public static List<ProductVariantEntity> listByIdsWithProduct(List<UUID> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyList();

        return list("select v from ProductVariantEntity v left join fetch v.product where v.id in ?1", ids);
    }

    // Helper to fetch all variants for a given product id including the product relation
    public static List<ProductVariantEntity> listByProductIdWithProduct(UUID productId) {
        if (productId == null) return Collections.emptyList();
        return list("select v from ProductVariantEntity v left join fetch v.product where v.product.id = ?1 order by v.id asc", productId);
    }

}
