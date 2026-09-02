package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductImageEntity;

import java.util.List;
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

    public void deleteByVariantId(UUID variantId)
    {
        if (variantId == null) {
            return;
        }
        delete("productVariant.id", variantId);
    }

    public List<ProductImageEntity> findByProductId(UUID productId)
    {
        return list("productVariant.product.id = ?1 ORDER BY sortOrder ASC", productId);
    }

    public List<ProductImageEntity> findForListing(UUID productId)
    {
        return getEntityManager().createQuery(
                        "SELECT pi FROM ProductImageEntity pi " +
                                "WHERE pi.productVariant.product.id = :productId " +
                                "ORDER BY CASE WHEN pi.isFeatured = true THEN 0 ELSE 1 END ASC, pi.sortOrder ASC, pi.id ASC",
                        ProductImageEntity.class)
                .setParameter("productId", productId)
                .getResultList();
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

    public ProductImageEntity findThumbnailForVariant(UUID variantId)
    {
        List<ProductImageEntity> images = getEntityManager().createQuery(
                        "SELECT pi FROM ProductImageEntity pi WHERE pi.productVariant.id = :variantId " +
                                "ORDER BY CASE WHEN pi.isFeatured = true THEN 0 ELSE 1 END ASC, pi.sortOrder ASC, pi.id ASC",
                        ProductImageEntity.class)
                .setParameter("variantId", variantId)
                .setMaxResults(1)
                .getResultList();
        return images.isEmpty() ? null : images.get(0);
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
