package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.VariantPricesEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductStatusEn;

import java.time.LocalDateTime;
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
}

