package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.VariantPricesEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductStatusEn;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class VariantPricesRepository extends BaseRepository<VariantPricesEntity, UUID>
{
    @Override
    protected Class<VariantPricesEntity> getEntityClass()
    {
        return VariantPricesEntity.class;
    }

    public VariantPricesEntity findLatestByVariantAndType(UUID variantId, PriceTypeEn priceType)
    {
        if (variantId == null || priceType == null) {
            return null;
        }
        return find("variant.id = ?1 and priceType = ?2 order by updatedAt desc", variantId, priceType).firstResult();
    }

    /**
     * Lowest active price for a product across its variants for a given price type,
     * within the active window. Tie-break: price, then earliest start date, then created.
     * (Used to build shopping list-item DTOs.)
     */
    public VariantPricesEntity findLowestActive(UUID productId, PriceTypeEn priceType, LocalDateTime now, boolean ignoreStatus)
    {
        LocalDateTime veryOldDate = LocalDateTime.of(1970, 1, 1, 0, 0);
        String q = "SELECT vp FROM VariantPricesEntity vp JOIN vp.variant v " +
                "WHERE v.product.id = :productId " +
                (ignoreStatus ? "" : "AND v.status = :variantStatus ") +
                "AND vp.priceType = :priceType " +
                "AND (vp.priceStartDate IS NULL OR vp.priceStartDate <= :now) " +
                "AND (vp.priceEndDate IS NULL OR vp.priceEndDate >= :now) " +
                "ORDER BY vp.price ASC, COALESCE(vp.priceStartDate, :veryOldDate) ASC, vp.createdAt ASC";
        TypedQuery<VariantPricesEntity> query = getEntityManager().createQuery(q, VariantPricesEntity.class)
                .setParameter("productId", productId)
                .setParameter("priceType", priceType)
                .setParameter("now", now)
                .setParameter("veryOldDate", veryOldDate)
                .setMaxResults(1);
        if (!ignoreStatus) {
            query.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }
        List<VariantPricesEntity> r = query.getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    /**
     * Lowest active RETAIL_PRICE across a product's ACTIVE variants, within the active
     * window, ordered by price. (Used to build admin list-item DTOs.)
     */
    public VariantPricesEntity findLowestActiveRetailForAdmin(UUID productId, LocalDateTime now)
    {
        String q = "SELECT vp FROM VariantPricesEntity vp JOIN vp.variant v " +
                "WHERE v.product.id = :productId " +
                "AND v.status = :activeStatus " +
                "AND vp.priceType = :priceType " +
                "AND (vp.priceStartDate IS NULL OR vp.priceStartDate <= :now) " +
                "AND (vp.priceEndDate IS NULL OR vp.priceEndDate >= :now) " +
                "ORDER BY vp.price ASC";
        List<VariantPricesEntity> r = getEntityManager().createQuery(q, VariantPricesEntity.class)
                .setParameter("productId", productId)
                .setParameter("activeStatus", ProductStatusEn.ACTIVE)
                .setParameter("priceType", PriceTypeEn.RETAIL_PRICE)
                .setParameter("now", now)
                .setMaxResults(1)
                .getResultList();
        return r.isEmpty() ? null : r.get(0);
    }

    /**
     * Returns the active candidate prices for a page of products in one query.
     * The list assembler applies the existing per-product tie-break rule in
     * memory. Keeping the query here preserves repository ownership of reads.
     */
    public List<VariantPricesEntity> findActiveForProductIds(
            List<UUID> productIds,
            List<PriceTypeEn> priceTypes,
            LocalDateTime now,
            boolean ignoreStatus)
    {
        if (productIds == null || productIds.isEmpty() || priceTypes == null || priceTypes.isEmpty()) {
            return Collections.emptyList();
        }

        String query = "SELECT vp FROM VariantPricesEntity vp " +
                "JOIN FETCH vp.variant v " +
                "JOIN FETCH v.product p " +
                "WHERE p.id IN :productIds " +
                (ignoreStatus ? "" : "AND v.status = :variantStatus ") +
                "AND vp.priceType IN :priceTypes " +
                "AND (vp.priceStartDate IS NULL OR vp.priceStartDate <= :now) " +
                "AND (vp.priceEndDate IS NULL OR vp.priceEndDate >= :now)";
        TypedQuery<VariantPricesEntity> typedQuery = getEntityManager().createQuery(query, VariantPricesEntity.class)
                .setParameter("productIds", productIds)
                .setParameter("priceTypes", priceTypes)
                .setParameter("now", now);
        if (!ignoreStatus) {
            typedQuery.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }
        return typedQuery.getResultList();
    }

    /**
     * Returns the active prices for a set of variant IDs (all requested price types,
     * within the active date window). Used by the wishlist hydration service to batch-fetch
     * prices for resolved variants.
     */
    public List<VariantPricesEntity> findActiveForVariantIds(
            List<UUID> variantIds,
            List<PriceTypeEn> priceTypes,
            LocalDateTime now)
    {
        if (variantIds == null || variantIds.isEmpty() || priceTypes == null || priceTypes.isEmpty()) {
            return Collections.emptyList();
        }

        String query = "SELECT vp FROM VariantPricesEntity vp " +
                "JOIN FETCH vp.variant v " +
                "WHERE v.id IN :variantIds " +
                "AND vp.priceType IN :priceTypes " +
                "AND (vp.priceStartDate IS NULL OR vp.priceStartDate <= :now) " +
                "AND (vp.priceEndDate IS NULL OR vp.priceEndDate >= :now)";
        return getEntityManager().createQuery(query, VariantPricesEntity.class)
                .setParameter("variantIds", variantIds)
                .setParameter("priceTypes", priceTypes)
                .setParameter("now", now)
                .getResultList();
    }
}
