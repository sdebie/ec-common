package org.ecommerce.common.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductVariantEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.query.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductVariantRepository extends BaseRepository<ProductVariantEntity, UUID>
{
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
     * Eagerly loads the parent product and its category to avoid N+1 queries.
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
                "left join fetch p.category " +
                "where v.id in (" +
                "  select v2.id from ProductVariantEntity v2 " +
                "  join v2.variantPrices vp " +
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
        if (productId == null) return BigDecimal.ZERO;

        List<ProductVariantEntity> variants = findByVariantsForProductId(productId);
        return variants.stream()
                .flatMap(v -> v.variantPrices.stream())
                .filter(p -> p.priceType.equals(priceType) && p.isActive())
                .map(p -> p.price)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}


