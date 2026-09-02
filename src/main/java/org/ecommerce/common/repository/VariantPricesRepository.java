package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.VariantPricesEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductStatusEn;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class VariantPricesRepository extends BaseRepository<VariantPricesEntity, UUID>
{
    @Override
    protected Class<VariantPricesEntity> getEntityClass()
    {
        return VariantPricesEntity.class;
    }

    static String activeWindowClause(String alias, String nowParam)
    {
        String prefix = (alias == null || alias.isBlank()) ? "" : alias + ".";
        return "(" + prefix + "priceStartDate IS NULL OR " + prefix + "priceStartDate <= :" + nowParam + ") " +
                "AND (" + prefix + "priceEndDate IS NULL OR " + prefix + "priceEndDate >= :" + nowParam + ")";
    }

    public VariantPricesEntity findLatestByVariantAndType(UUID variantId, PriceTypeEn priceType)
    {
        if (variantId == null || priceType == null) {
            return null;
        }
        return find("variant.id = ?1 and priceType = ?2 order by updatedAt desc", variantId, priceType).firstResult();
    }

    public List<VariantPricesEntity> findByVariantId(UUID variantId)
    {
        if (variantId == null) return List.of();
        return list("variant.id = ?1 order by priceType asc, createdAt desc", variantId);
    }

    public VariantPricesEntity findActiveByVariantAndType(UUID variantId, PriceTypeEn priceType)
    {
        if (variantId == null || priceType == null) return null;

        Map<String, Object> params = new HashMap<>();
        params.put("variantId", variantId);
        params.put("priceType", priceType);
        params.put("now", LocalDateTime.now());
        return find("variant.id = :variantId and priceType = :priceType and " +
                        activeWindowClause(null, "now") +
                        " order by updatedAt desc",
                params
        ).firstResult();
    }

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
                "AND " + activeWindowClause("vp", "now");
        TypedQuery<VariantPricesEntity> typedQuery = getEntityManager().createQuery(query, VariantPricesEntity.class)
                .setParameter("productIds", productIds)
                .setParameter("priceTypes", priceTypes)
                .setParameter("now", now);
        if (!ignoreStatus) {
            typedQuery.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }
        return typedQuery.getResultList();
    }

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
                "AND " + activeWindowClause("vp", "now");
        return getEntityManager().createQuery(query, VariantPricesEntity.class)
                .setParameter("variantIds", variantIds)
                .setParameter("priceTypes", priceTypes)
                .setParameter("now", now)
                .getResultList();
    }
}
