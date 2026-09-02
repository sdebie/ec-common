package org.ecommerce.common.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductVariantEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductStatusEn;
import org.ecommerce.common.query.PageRequest;

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

    public ProductVariantEntity findByIdWithProduct(UUID id)
    {
        if (id == null) return null;
        return find("select v from ProductVariantEntity v left join fetch v.product where v.id = ?1", id)
                .firstResult();
    }

    public List<ProductVariantEntity> findByIdsWithProduct(List<UUID> ids)
    {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return list("select v from ProductVariantEntity v left join fetch v.product where v.id in ?1", ids);
    }

    public List<ProductVariantEntity> findByVariantsForProductId(UUID productId)
    {
        if (productId == null) return Collections.emptyList();
        return list(
                "select v from ProductVariantEntity v left join fetch v.product where v.product.id = ?1 order by v.id asc",
                productId);
    }

    public List<ProductVariantEntity> findForProductIds(List<UUID> productIds, boolean ignoreStatus)
    {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }

        String query = "select v from ProductVariantEntity v " +
                "join fetch v.product " +
                "where v.product.id in :productIds " +
                (ignoreStatus ? "" : "and v.status = :variantStatus ") +
                "order by v.product.id asc, v.id asc";
        var typedQuery = getEntityManager().createQuery(query, ProductVariantEntity.class)
                .setParameter("productIds", productIds);
        if (!ignoreStatus) {
            typedQuery.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }
        return typedQuery.getResultList();
    }

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

    /**
     * First variant id (by id ASC) for a product; ACTIVE-only unless {@code ignoreStatus}.
     */
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

    /**
     * Aggregated stock across all variants of a product.
     */
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
     * Fetch ACTIVE-only variants for a given product (excludes PENDING and DISABLED).
     * Used by the public storefront-detail read so an unpublished (PENDING) or
     * soft-deleted (DISABLED) variant is never customer-visible.
     */
    public List<ProductVariantEntity> findActiveVariantsForProductId(UUID productId)
    {
        if (productId == null) return Collections.emptyList();
        return list(
                "select v from ProductVariantEntity v left join fetch v.product " +
                        "where v.product.id = ?1 and v.status = ?2 order by v.id asc",
                productId, ProductStatusEn.ACTIVE);
    }

    /**
     * Fetch every variant the admin editor can still act on for a given product —
     * ACTIVE and PENDING, excludes only soft-deleted (DISABLED) variants.
     * Used by admin-edit reads so a soft-deleted variant is absent after save,
     * while a PENDING variant the admin is still staging remains visible.
     */
    public List<ProductVariantEntity> findNonDisabledVariantsForProductId(UUID productId)
    {
        if (productId == null) return Collections.emptyList();
        return list(
                "select v from ProductVariantEntity v left join fetch v.product " +
                        "where v.product.id = ?1 and v.status <> ?2 order by v.id asc",
                productId, ProductStatusEn.DISABLED);
    }

}
