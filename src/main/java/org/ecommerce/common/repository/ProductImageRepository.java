package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductImageEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ProductImageRepository extends BaseRepository<ProductImageEntity, UUID>
{
    @Override
    protected Class<ProductImageEntity> getEntityClass()
    {
        return ProductImageEntity.class;
    }

    public List<ProductImageEntity> findByVariantId(UUID variantId)
    {
        if (variantId == null) {
            return List.of();
        }
        return list("productVariant.id", variantId);
    }

    public List<ProductImageEntity> findByProductId(UUID productId)
    {
        return list("productVariant.product.id = ?1 ORDER BY sortOrder ASC", productId);
    }

    public List<ProductImageEntity> findForListingProductIds(List<UUID> productIds)
    {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return getEntityManager().createQuery(
                        "SELECT pi FROM ProductImageEntity pi " +
                                "JOIN FETCH pi.productVariant pv " +
                                "JOIN FETCH pv.product p " +
                                "WHERE p.id IN :productIds " +
                                "ORDER BY p.id ASC, CASE WHEN pi.isFeatured = true THEN 0 ELSE 1 END ASC, pi.sortOrder ASC, pi.id ASC",
                        ProductImageEntity.class)
                .setParameter("productIds", productIds)
                .getResultList();
    }

    public List<ProductImageEntity> findForVariantIds(List<UUID> variantIds)
    {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }
        return getEntityManager().createQuery(
                        "SELECT pi FROM ProductImageEntity pi " +
                                "JOIN FETCH pi.productVariant pv " +
                                "WHERE pv.id IN :variantIds " +
                                "ORDER BY pv.id ASC, CASE WHEN pi.isFeatured = true THEN 0 ELSE 1 END ASC, pi.sortOrder ASC, pi.id ASC",
                        ProductImageEntity.class)
                .setParameter("variantIds", variantIds)
                .getResultList();
    }

    /**
     * {@link #findForVariantIds}, grouped by variant id — the shape an order-hydration path
     * needs to attach each line's pictures without ever touching {@code ProductVariantEntity}'s
     * own managed {@code images} collection. Each list keeps the query's own order: featured
     * image first, then by sort order — so a caller wanting just the display image can take
     * element 0.
     */
    public Map<UUID, List<ProductImageEntity>> findGroupedByVariantIds(List<UUID> variantIds)
    {
        Map<UUID, List<ProductImageEntity>> byVariantId = new LinkedHashMap<>();
        for (ProductImageEntity image : findForVariantIds(variantIds)) {
            byVariantId.computeIfAbsent(image.getProductVariant().getId(), k -> new ArrayList<>()).add(image);
        }
        return byVariantId;
    }

    public ProductImageEntity findFeaturedByProductId(UUID productId)
    {
        return find("productVariant.product.id = ?1 AND isFeatured = true", productId).firstResult();
    }

    public void setFeaturedImage(UUID productId, UUID imageId)
    {
        // Nonfeature all images for this product
        update("isFeatured = false WHERE productVariant.product.id = ?1", productId);

        // Feature the specific image
        update("isFeatured = true WHERE id = ?1 AND productVariant.product.id = ?2", imageId, productId);
    }
}
