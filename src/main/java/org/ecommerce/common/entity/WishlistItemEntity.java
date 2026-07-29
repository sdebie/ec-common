package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "customer_wishlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "variant_id"}))
public class WishlistItemEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariantEntity variant;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ── Finders ──────────────────────────────────────────────────────────────
    public static List<WishlistItemEntity> findByCustomerId(UUID customerId)
    {
        return list("customer.id", customerId);
    }

    public static WishlistItemEntity findByCustomerAndVariant(UUID customerId, UUID variantId)
    {
        return find("customer.id = ?1 and variant.id = ?2", customerId, variantId).firstResult();
    }

    public static long deleteByCustomerAndVariant(UUID customerId, UUID variantId)
    {
        return delete("customer.id = ?1 and variant.id = ?2", customerId, variantId);
    }
}
