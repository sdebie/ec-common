package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.ecommerce.common.enums.ProductTypeEn;

@Entity
@Table(name = "products")
public class ProductEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(nullable = false, unique = true)
    public String slug;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    public Set<CategoryEntity> categories = new HashSet<>();

    public void setCategory(CategoryEntity cat) {
        if (cat == null) return;
        this.categories.add(cat);
    }

    public CategoryEntity getCategory() {
        return categories.isEmpty() ? null : categories.iterator().next();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    public BrandEntity brand;

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name="short_description", columnDefinition = "TEXT")
    public String shorDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    public ProductTypeEn productType; // SIMPLE or VARIABLE

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ProductVariantEntity> variants;
}
