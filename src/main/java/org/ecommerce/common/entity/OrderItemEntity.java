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
@Table(name = "order_items")
public class OrderItemEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    private OrderEntity orderEntity;

    // Link to product variant via JPA relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", referencedColumnName = "id")
    private ProductVariantEntity variant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    // --- Optional Helpers ---

    /**
     * Calculates the subtotal for this specific line item.
     */
    public BigDecimal getSubtotal()
    {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}