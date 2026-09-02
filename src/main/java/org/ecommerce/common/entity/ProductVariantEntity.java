package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ProductStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product_variants")
public class ProductVariantEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    // Store JSONB as String to keep mapping minimal; can be mapped to JSON later if needed
    @Column(name = "attributes")
    private String attributesJson;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatusEn status; // PENDING, ACTIVE, DISABLED

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantPricesEntity> prices;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC") // Automatically keeps your images sorted for the storefront
    private List<ProductImageEntity> images = new ArrayList<>();

    /**
     * The image that stands for this variant: the one flagged featured, else the first by
     * sort order. A variant deleted from the catalogue leaves order lines intact, so having
     * no image at all is a normal state rather than an error.
     */
    public String displayImageUrl()
    {
        if (images == null || images.isEmpty()) {
            return null;
        }

        ProductImageEntity chosen = null;
        for (ProductImageEntity image : images) {
            if (image == null) {
                continue;
            }
            if (Boolean.TRUE.equals(image.getIsFeatured())) {
                return image.getImageUrl();
            }
            if (chosen == null) {
                chosen = image;
            }
        }
        return chosen == null ? null : chosen.getImageUrl();
    }
}
