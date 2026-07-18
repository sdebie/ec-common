package org.ecommerce.common.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductVariantEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductStatusEn;
import org.ecommerce.common.query.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductVariantRepository extends BaseRepository<ProductVariantEntity, UUID>
{
    @Override
    protected Class<ProductVariantEntity> getEntityClass()
    {
        return ProductVariantEntity.class;
    }

    public ProductVariantEntity findBySku(String sku)
    {
        if (sku == null || sku.isBlank()) {
            return null;
        }
        return find("sku", sku.trim()).firstResult();
    }

    public ProductVariantEntity findBySkuWithProduct(String sku)
    {
        if (sku == null || sku.isBlank()) {
            return null;
        }
        return find("select v from ProductVariantEntity v left join fetch v.product where v.sku = ?1", sku.trim())
                .firstResult();
    }

    /**
     * Fetch a single variant together with its parent Product entity.
     */
    public ProductVariantEntity findByIdWithProduct(UUID id)
    {
        if (id == null) return null;
        return find("select v from ProductVariantEntity v left join fetch v.product where v.id = ?1", id)
                .firstResult();
    }

    /**
     * Fetch multiple variants (by ID list) together with their parent Product entities.
     */
    public List<ProductVariantEntity> findByIdsWithProduct(List<UUID> ids)
    {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return list("select v from ProductVariantEntity v left join fetch v.product where v.id in ?1", ids);
    }

    /**
     * Fetch all variants for a given product together with the parent Product entity.
     */
    public List<ProductVariantEntity> findByVariantsForProductId(UUID productId)
    {
        if (productId == null) return Collections.emptyList();
        return list(
                "select v from ProductVariantEntity v left join fetch v.product where v.product.id = ?1 order by v.id asc",
                productId);
    }

    /**
     * Fetch all variants that carry an active RETAIL_SALE_PRICE or WHOLESALE_SALE_PRICE.
     * Eagerly loads the parent product and its categories to avoid N+1 queries.
     */
    public List<ProductVariantEntity> findOnSaleVariants(PageRequest pageRequest)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> salePriceTypes = List.of(
                PriceTypeEn.RETAIL_SALE_PRICE,
                PriceTypeEn.WHOLESALE_SALE_PRICE);

        return find(
                "select v from ProductVariantEntity v " +
                "left join fetch v.product p " +
                "left join fetch p.categories " +
                "where v.id in (" +
                "  select v2.id from ProductVariantEntity v2 " +
                "  join VariantPricesEntity vp on vp.variant = v2 " +
                "  where vp.priceType in ?1 " +
                "  and (vp.priceStartDate is null or vp.priceStartDate <= ?2) " +
                "  and (vp.priceEndDate is null or vp.priceEndDate >= ?2)" +
                ")",
                Sort.by("sku"),
                salePriceTypes,
                now)
                .page(Page.of(
                        pageRequest != null ? pageRequest.getPageIndex() : 0,
                        pageRequest != null ? pageRequest.getPageSize() : 10))
                .list();
    }

    /**
     * Returns the lowest active price for a product across all its variants for a given price type.
     * Returns {@link BigDecimal#ZERO} when no matching active price is found.
     */
    public BigDecimal getMinimumPrice(UUID productId, PriceTypeEn priceType)
    {
        return BigDecimal.ZERO;

        //TODO::SDB Get the prices in window
    }

    /** Number of variants for a product; ACTIVE-only unless {@code ignoreStatus}. */
    public int countForProduct(UUID productId, boolean ignoreStatus)
    {
        String q = "SELECT COUNT(v.id) FROM ProductVariantEntity v WHERE v.product.id = :productId " +
                (ignoreStatus ? "" : "AND v.status = :variantStatus");
        var query = getEntityManager().createQuery(q, Long.class).setParameter("productId", productId);
        if (!ignoreStatus) {
            query.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }
        Long count = query.getSingleResult();
        return count == null ? 0 : count.intValue();
    }

    /** First variant id (by id ASC) for a product; ACTIVE-only unless {@code ignoreStatus}. */
    public String findFirstVariantId(UUID productId, boolean ignoreStatus)
    {
        String q = "SELECT v.id FROM ProductVariantEntity v WHERE v.product.id = :productId " +
                (ignoreStatus ? "" : "AND v.status = :variantStatus ") +
                "ORDER BY v.id ASC";
        var query = getEntityManager().createQuery(q, UUID.class)
                .setParameter("productId", productId)
                .setMaxResults(1);
        if (!ignoreStatus) {
            query.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }
        List<UUID> ids = query.getResultList();
        return ids.isEmpty() ? null : ids.get(0).toString();
    }

    /** Aggregated stock across all variants of a product. */
    public int sumStock(UUID productId)
    {
        Long total = getEntityManager().createQuery(
                        "SELECT COALESCE(SUM(v.stockQuantity), 0) FROM ProductVariantEntity v WHERE v.product.id = :productId",
                        Long.class)
                .setParameter("productId", productId)
                .getSingleResult();
        return total == null ? 0 : total.intValue();
    }

    /**
     * Returns true if the given variant is referenced by any order_items row.
     * Used by the Deletion Policy to prevent hard-deleting order-referenced variants.
     */
    public boolean isReferencedByOrders(UUID variantId)
    {
        if (variantId == null) return false;
        Long count = getEntityManager().createQuery(
                        "SELECT COUNT(oi.id) FROM OrderItemEntity oi WHERE oi.variant.id = :variantId",
                        Long.class)
                .setParameter("variantId", variantId)
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Fetch all ACTIVE variants for a given product (excludes DISABLED).
     * Used by admin-edit and storefront-detail reads so that soft-deleted variants
     * are absent after save.
     */
    public List<ProductVariantEntity> findActiveVariantsForProductId(UUID productId)
    {
        if (productId == null) return Collections.emptyList();
        return list(
                "select v from ProductVariantEntity v left join fetch v.product " +
                "where v.product.id = ?1 and v.status <> ?2 order by v.id asc",
                productId, ProductStatusEn.DISABLED);
    }
}


