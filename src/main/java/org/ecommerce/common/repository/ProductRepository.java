package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.dto.PageResponse;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.enums.*;
import org.ecommerce.common.query.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
    @Override
    protected Class<ProductEntity> getEntityClass()
    {
        return ProductEntity.class;
    }

    public ProductEntity findBySlugIgnoreCase(String slug)
    {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return find("lower(slug) = ?1", slug.trim().toLowerCase()).firstResult();
    }

    public ProductEntity findByNameIgnoreCase(String name)
    {
        if (name == null || name.isBlank()) {
            return null;
        }
        return find("lower(name) = ?1", name.trim().toLowerCase()).firstResult();
    }

    public ProductEntity findByIdWithCategoryAndBrand(UUID productId)
    {
        if (productId == null) return null;

        return find("select p from ProductEntity p " +
                "left join fetch p.categories " +
                "left join fetch p.brand " +
                "where p.id = ?1", productId)
                .firstResult();
    }

    public long countShoppingProducts(FilterRequest filterRequest, boolean onSale)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> shoppingPriceTypes = onSale
                ? List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
                : List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
        FilterRequest normalizedFilterRequest = normalizeProductFilterRequest(filterRequest);
        ProductQueryBuilder queryBuilder = ProductQueryBuilder.fromProduct(normalizedFilterRequest, ProductEntity.class);

        String hql = "select count(p) from ProductEntity p " +
                "where exists (" +
                "select 1 from ProductVariantEntity v " +
                "join VariantPricesEntity vp on vp.variant = v " +
                "where v.product = p " +
                "and v.status = :variantStatus " +
                "and vp.priceType in :priceTypes " +
                "and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
                "and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
                ")";

        if (queryBuilder.hasQuery()) {
            hql += " AND " + queryBuilder.query();
        }

        TypedQuery<Long> q = getEntityManager().createQuery(hql, Long.class);
        q.setParameter("priceTypes", shoppingPriceTypes);
        q.setParameter("now", now);
        q.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        if (queryBuilder.hasParams()) {
            for (Map.Entry<String, Object> entry : queryBuilder.params().entrySet()) {
                q.setParameter(entry.getKey(), entry.getValue());
            }
        }

        Long result = q.getSingleResult();
        return result != null ? result : 0L;
    }

    public List<ProductEntity> findShoppingProductEntities(PageRequest pageRequest, FilterRequest filterRequest,
                                                           boolean onSale,
                                                           CatalogueSortEn sortBy, PriceBasisEn priceBasis)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> shoppingPriceTypes = onSale
                ? List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
                : List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
        FilterRequest normalizedFilterRequest = normalizeProductFilterRequest(filterRequest);
        ProductQueryBuilder queryBuilder = ProductQueryBuilder.fromProduct(normalizedFilterRequest, ProductEntity.class);

        CatalogueSortEn effectiveSort = sortBy != null ? sortBy : CatalogueSortEn.NAME_ASC;
        PriceBasisEn effectiveBasis = priceBasis != null ? priceBasis : PriceBasisEn.RETAIL;

        // ─── Step 1: ID-selection query (paged, no collection fetch, no DISTINCT) ───
        boolean needsSortKey = effectiveSort != CatalogueSortEn.NAME_ASC;

        String sortKeyExpr = needsSortKey ? buildPriceSortKeyExpression(effectiveBasis) : null;

        // Always select two columns so the result is consistently Object[]
        String idQuery = "select p.id, " +
                (needsSortKey ? sortKeyExpr + " as sortKey" : "p.name as sortKey") +
                " from ProductEntity p " +
                "where exists (" +
                "select 1 from ProductVariantEntity v " +
                "join VariantPricesEntity vp on vp.variant = v " +
                "where v.product = p " +
                "and v.status = :variantStatus " +
                "and vp.priceType in :priceTypes " +
                "and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
                "and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
                ")";

        if (queryBuilder.hasQuery()) {
            idQuery += " AND " + queryBuilder.query();
        }

        // ORDER BY — price sorts use sortKey + tie-break; NAME_ASC uses name + id only
        if (needsSortKey) {
            String dir = effectiveSort == CatalogueSortEn.PRICE_DESC ? "DESC" : "ASC";
            idQuery += " ORDER BY sortKey " + dir + " NULLS LAST, p.name ASC, p.id ASC";
        } else {
            idQuery += " ORDER BY p.name ASC, p.id ASC";
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("priceTypes", shoppingPriceTypes);
        params.put("now", now);
        params.put("variantStatus", ProductStatusEn.ACTIVE);
        if (needsSortKey) {
            bindSortKeyParams(params, effectiveBasis, now);
        }
        if (queryBuilder.hasParams()) {
            params.putAll(queryBuilder.params());
        }

        PageRequest effectivePage = pageRequest != null ? pageRequest : new PageRequest();
        int pageIndex = effectivePage.getPageIndex();
        int pageSize = effectivePage.getPageSize();

        TypedQuery<Object[]> idTypedQuery = getEntityManager().createQuery(idQuery, Object[].class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            idTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idTypedQuery.setFirstResult(pageIndex * pageSize);
        idTypedQuery.setMaxResults(pageSize);

        List<Object[]> rows = idTypedQuery.getResultList();

        List<UUID> ids = rows.stream()
                .map(row -> (UUID) row[0])
                .collect(Collectors.toList());

        // ─── Step 2: hydration (unpaged, no DISTINCT) ───────────────────────────────
        return fetchProductsByIds(ids);
    }

    public List<ProductEntity> findOnSaleProductEntities(PageRequest pageRequest, boolean ignoreStatus)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> salePriceTypes = List.of(
                PriceTypeEn.RETAIL_SALE_PRICE,
                PriceTypeEn.WHOLESALE_SALE_PRICE);

        // ─── Step 1: ID-selection query (paged, no collection fetch, no DISTINCT) ───
        String idQuery = "select p.id from ProductEntity p " +
                "where exists (" +
                "select 1 from ProductVariantEntity v " +
                "join VariantPricesEntity vp on vp.variant = v " +
                "where v.product = p " +
                (ignoreStatus ? "" : "and v.status = :variantStatus ") +
                "and vp.priceType in :priceTypes " +
                "and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
                "and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
                ") " +
                "ORDER BY p.name ASC, p.id ASC";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("priceTypes", salePriceTypes);
        params.put("now", now);
        if (!ignoreStatus) {
            params.put("variantStatus", ProductStatusEn.ACTIVE);
        }

        PageRequest effectivePage = pageRequest != null ? pageRequest : new PageRequest();
        int pageIndex = effectivePage.getPageIndex();
        int pageSize = effectivePage.getPageSize();

        TypedQuery<UUID> idTypedQuery = getEntityManager().createQuery(idQuery, UUID.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            idTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }
        idTypedQuery.setFirstResult(pageIndex * pageSize);
        idTypedQuery.setMaxResults(pageSize);

        List<UUID> ids = idTypedQuery.getResultList();

        // ─── Step 2: hydration (unpaged, no DISTINCT) ───────────────────────────────
        return fetchProductsByIds(ids);
    }


    // ─── Price Sort Key ───────────────────────────────────────────────────────────

    /**
     * Builds the HQL expression for the catalogue price sort key, parameterised by
     * price basis. The expression computes:
     * <pre>
     *   COALESCE(
     *     MIN(price) over active rows of the basis SALE tier,
     *     MIN(price) over active rows of the basis BASE tier
     *   )
     * </pre>
     * <p>
     * "Active row" mirrors {@link VariantPricesRepository#findActiveForProductIds}:
     * variant status = ACTIVE (unconditionally), price_start_date IS NULL OR ≤ :now,
     * price_end_date IS NULL OR ≥ :now.
     * <p>
     * The returned fragment is a correlated subquery suitable for a SELECT list (aliased
     * externally) or ORDER BY. The caller must bind the parameters returned by
     * {@link #bindSortKeyParams(Map, PriceBasisEn, LocalDateTime)} on the same query.
     *
     * @param basis which price tier pair to use (RETAIL or WHOLESALE)
     * @return the HQL fragment (no leading/trailing whitespace, no alias assignment)
     */
    private String buildPriceSortKeyExpression(PriceBasisEn basis)
    {
        // The two subqueries differ only in the price type parameter name; the
        // activeness predicate is identical and unconditional (no ignoreStatus).
        return "coalesce(" +
                "(select min(vp1.price) from VariantPricesEntity vp1 " +
                "join vp1.variant v1 " +
                "where v1.product = p " +
                "and v1.status = :variantStatus " +
                "and vp1.priceType = :salePriceType " +
                "and (vp1.priceStartDate is null or vp1.priceStartDate <= :now) " +
                "and (vp1.priceEndDate is null or vp1.priceEndDate >= :now)), " +
                "(select min(vp2.price) from VariantPricesEntity vp2 " +
                "join vp2.variant v2 " +
                "where v2.product = p " +
                "and v2.status = :variantStatus " +
                "and vp2.priceType = :basePriceType " +
                "and (vp2.priceStartDate is null or vp2.priceStartDate <= :now) " +
                "and (vp2.priceEndDate is null or vp2.priceEndDate >= :now))" +
                ")";
    }

    /**
     * Binds the parameters required by the sort-key expression produced by
     * {@link #buildPriceSortKeyExpression(PriceBasisEn)} into the supplied parameter map.
     * The caller must also ensure `:variantStatus` is bound (shared with the product
     * active-variant exists-predicate).
     * <p>
     * Parameters bound: {@code :salePriceType}, {@code :basePriceType}, {@code :now},
     * {@code :variantStatus}.
     *
     * @param params the mutable parameter map to populate
     * @param basis  the price basis (determines which sale/base enum values are bound)
     * @param now    the current timestamp for the active-window check
     */
    private void bindSortKeyParams(Map<String, Object> params, PriceBasisEn basis, LocalDateTime now)
    {
        PriceTypeEn salePriceType;
        PriceTypeEn basePriceType;

        if (basis == PriceBasisEn.WHOLESALE) {
            salePriceType = PriceTypeEn.WHOLESALE_SALE_PRICE;
            basePriceType = PriceTypeEn.WHOLESALE_PRICE;
        } else {
            salePriceType = PriceTypeEn.RETAIL_SALE_PRICE;
            basePriceType = PriceTypeEn.RETAIL_PRICE;
        }

        params.put("salePriceType", salePriceType);
        params.put("basePriceType", basePriceType);
        params.put("now", now);
        params.put("variantStatus", ProductStatusEn.ACTIVE);
    }

    private FilterRequest normalizeProductFilterRequest(FilterRequest filterRequest)
    {
        FilterRequest normalized = new FilterRequest();
        if (filterRequest == null) {
            return normalized;
        }

        normalized.setSort(filterRequest.getSort());
        normalized.setFilters(copyAndNormalizeFilters(filterRequest.getFilters()));
        normalized.setFilterGroups(copyAndNormalizeGroups(filterRequest.getFilterGroups()));
        return normalized;
    }

    private List<Filter> copyAndNormalizeFilters(List<Filter> filters)
    {
        if (filters == null || filters.isEmpty()) {
            return filters;
        }

        List<Filter> normalized = new ArrayList<>(filters.size());
        for (Filter original : filters) {
            if (original == null) {
                continue;
            }

            Filter copy = new Filter();
            copy.setKey(normalizeProductFilterKey(original.getKey()));
            copy.setOperator(original.getOperator());
            copy.setValue(original.getValue());
            copy.setValues(original.getValues() == null ? null : new ArrayList<>(original.getValues()));
            normalized.add(copy);
        }
        return normalized;
    }

    private List<FilterGroup> copyAndNormalizeGroups(List<FilterGroup> groups)
    {
        if (groups == null || groups.isEmpty()) {
            return groups;
        }

        List<FilterGroup> normalized = new ArrayList<>(groups.size());
        for (FilterGroup group : groups) {
            if (group == null) {
                continue;
            }

            FilterGroup copy = new FilterGroup();
            copy.setOperator(group.getOperator());
            copy.setFilters(copyAndNormalizeFilters(group.getFilters()));
            copy.setFilterGroups(copyAndNormalizeGroups(group.getFilterGroups()));
            normalized.add(copy);
        }
        return normalized;
    }

    private String normalizeProductFilterKey(String key)
    {
        if (key == null || key.isBlank()) {
            return key;
        }

        if (key.startsWith("p.") || key.startsWith("category.")) {
            return key;
        }

        if (key.startsWith("categories.")) {
            return "category." + key.substring("categories.".length());
        }

        if (key.contains(".")) {
            return "p." + key;
        }

        return "p." + key;
    }

    // ─── Best Sellers ──────────────────────────────────────────────────────────

    /**
     * Returns the top 10 best-selling products based on total quantity sold in
     * DELIVERED orders. If fewer than 10 exist, the remainder is filled with
     * random products so the response always contains up to 10 entries.
     */
    public List<ProductEntity> findTopBestSellerEntities()
    {
        final int TARGET = 10;

        // Step 1 – collect best-seller product IDs ranked by units sold
        List<Object[]> rows = getEntityManager()
                .createQuery(
                        "select oi.variant.product.id, sum(oi.quantity) as total " +
                                "from OrderItemEntity oi " +
                                "join oi.orderEntity o " +
                                "where o.status = :status " +
                                "and oi.variant is not null " +
                                "group by oi.variant.product.id " +
                                "order by total desc",
                        Object[].class)
                .setParameter("status", OrderStatusEn.DELIVERED)
                .setMaxResults(TARGET)
                .getResultList();

        List<UUID> bestSellerIds = rows.stream()
                .map(row -> (UUID) row[0])
                .collect(Collectors.toList());

        // Step 2 – fetch full product entities (with category + brand) preserving rank order
        List<ProductEntity> bestSellers = fetchProductsByIds(bestSellerIds);

        // Step 3 – pad with random products when fewer than TARGET were found
        List<ProductEntity> result = new ArrayList<>(bestSellers);
        if (result.size() < TARGET) {
            int needed = TARGET - result.size();
            List<ProductEntity> random = findRandomProductEntitiesExcluding(needed, bestSellerIds);
            result.addAll(random);
        }

        return result;
    }

    /**
     * Fetches ProductEntity records for the given IDs with category and brand
     * eagerly loaded. The returned list preserves the order of the supplied IDs.
     */
    private List<ProductEntity> fetchProductsByIds(List<UUID> ids)
    {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<ProductEntity> unordered = getEntityManager()
                .createQuery(
                        "select p from ProductEntity p " +
                                "left join fetch p.categories " +
                                "left join fetch p.brand " +
                                "where p.id in :ids",
                        ProductEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        // Restore the ranked order returned by the aggregation query
        Map<UUID, ProductEntity> byId = unordered.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns up to {@code limit} random products excluding the given IDs.
     */
    private List<ProductEntity> findRandomProductEntitiesExcluding(int limit, List<UUID> excludeIds)
    {
        TypedQuery<ProductEntity> q;
        if (excludeIds == null || excludeIds.isEmpty()) {
            q = getEntityManager().createQuery(
                    "select p from ProductEntity p " +
                            "left join fetch p.categories " +
                            "left join fetch p.brand " +
                            "order by function('random')",
                    ProductEntity.class);
        } else {
            q = getEntityManager().createQuery(
                            "select p from ProductEntity p " +
                                    "left join fetch p.categories " +
                                    "left join fetch p.brand " +
                                    "where p.id not in :excludeIds " +
                                    "order by function('random')",
                            ProductEntity.class)
                    .setParameter("excludeIds", excludeIds);
        }
        return q.setMaxResults(limit).getResultList();
    }

    /**
     * Paged admin product list (entities), with optional status/category/brand/search
     * filters. Returns the page of entities plus pagination metadata; the caller maps
     * each entity to its DTO.
     */
    public PageResponse<ProductEntity> findAdminProductPage(int pageIndex, int pageSize, String status, String categoryId, String brandId, String search)
    {
        int effectivePageSize = Math.min(Math.max(pageSize, 1), 100);
        int effectivePageIndex = Math.max(pageIndex, 0);

        StringBuilder whereClause = new StringBuilder("WHERE 1=1");
        Map<String, Object> params = new LinkedHashMap<>();

        if (status != null && !status.isBlank()) {
            whereClause.append(" AND p.status = :status");
            params.put("status", ProductStatusEn.valueOf(status));
        }
        if (categoryId != null && !categoryId.isBlank()) {
            whereClause.append(" AND EXISTS (SELECT 1 FROM p.categories c WHERE c.id = :categoryId)");
            params.put("categoryId", UUID.fromString(categoryId));
        }
        if (brandId != null && !brandId.isBlank()) {
            whereClause.append(" AND p.brand.id = :brandId");
            params.put("brandId", UUID.fromString(brandId));
        }
        if (search != null && !search.isBlank()) {
            String searchPattern = "%" + search.trim().toLowerCase() + "%";
            whereClause.append(" AND (LOWER(p.name) LIKE :search")
                    .append(" OR EXISTS (SELECT 1 FROM ProductVariantEntity sv WHERE sv.product = p AND LOWER(sv.sku) LIKE :search)")
                    .append(")");
            params.put("search", searchPattern);
        }

        String countHql = "SELECT COUNT(p) FROM ProductEntity p " + whereClause;
        TypedQuery<Long> countQuery = getEntityManager().createQuery(countHql, Long.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalElements = countQuery.getSingleResult();
        int totalPages = effectivePageSize > 0
                ? (int) Math.ceil((double) totalElements / effectivePageSize)
                : 0;
        // A deletion or filter change can make a previously valid client page fall
        // outside the result set between requests. Return the final available page
        // instead of an avoidable empty page; callers can render response metadata
        // without an effect-driven pagination correction.
        if (totalPages > 0) {
            effectivePageIndex = Math.min(effectivePageIndex, totalPages - 1);
        }

        String fetchHql = "SELECT DISTINCT p FROM ProductEntity p " +
                "LEFT JOIN FETCH p.categories " +
                "LEFT JOIN FETCH p.brand " +
                whereClause +
                " ORDER BY p.name ASC";
        TypedQuery<ProductEntity> fetchQuery = getEntityManager().createQuery(fetchHql, ProductEntity.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            fetchQuery.setParameter(entry.getKey(), entry.getValue());
        }
        fetchQuery.setFirstResult(effectivePageIndex * effectivePageSize);
        fetchQuery.setMaxResults(effectivePageSize);

        List<ProductEntity> products = fetchQuery.getResultList();

        return new PageResponse<>(products, totalElements, totalPages, effectivePageIndex, effectivePageSize);
    }

}
