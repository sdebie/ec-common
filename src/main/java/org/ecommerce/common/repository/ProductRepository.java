package org.ecommerce.common.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.dto.PageResponse;
import org.ecommerce.common.entity.CategoryEntity;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.enums.*;
import org.ecommerce.common.query.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
    /**
     * Lets a client-supplied {@code category.*} filter key (e.g. {@code category.id}) reach
     * through {@code categories}, a to-many association, without a direct JOIN — which would
     * multiply each product's row once per matching category and need DISTINCT to collapse
     * back down. {@link PanacheQueryBuilder} rewrites any filter whose key matches this prefix
     * into an EXISTS subquery instead.
     */
    private static final PanacheQueryBuilder.CollectionExistsRewrite CATEGORY_REWRITE =
            new PanacheQueryBuilder.CollectionExistsRewrite("category.", "p", "categories", "CategoryEntity", "category");

    /**
     * {@code productCount} is reachable with no {@code @RolesAllowed} at all whenever
     * {@code categoryId}/{@code brandId} are both omitted, so this allowlist is a real,
     * pre-auth-facing gate. Excludes every {@code variants.prices.*} path: {@code price} and
     * {@code priceType} would let an anonymous caller binary-search a product's undisclosed
     * (e.g. wholesale) price via GREATER_THAN/LESS_THAN filtering — a numeric-oracle variant of
     * the same bug class as the VIEWER password-hash oracle, and directly related to the
     * separately-tracked anonymous-wholesale-price-leak issue — and {@code createdBy}/{@code
     * updatedBy} are internal staff-audit fields with no client-facing purpose. Deliberately
     * omits the plain {@code categories.id} path some existing test code uses: unlike
     * {@code category.id} it bypasses {@link #CATEGORY_REWRITE}'s EXISTS rewrite and does a
     * direct join instead, which can duplicate a row for a product matched by more than one
     * requested category — the caller should use {@code category.id}, which is both safe and
     * already in this allowlist.
     */
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "id", "slug", "name", "status", "productType", "isFeatured", "createdAt",
            "brand.id", "category.id");

    @Override
    protected Class<ProductEntity> getEntityClass()
    {
        return ProductEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }

    /**
     * Routes the generic filtered listing through {@link #CATEGORY_REWRITE} instead of the
     * plain {@link PanacheQueryBuilder} {@link BaseRepository} uses by default, so a category
     * filter reaching this repository through any path — not just the bespoke shopping-list
     * methods below — gets the same EXISTS-rewrite.
     */
    @Override
    public List<ProductEntity> findAll(PageRequest pageRequest, FilterRequest filterRequest)
    {
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest, ProductEntity.class, CATEGORY_REWRITE, ALLOWED_FILTER_FIELDS);
        PanacheQuery<ProductEntity> query;

        if (queryBuilder.hasQuery() && queryBuilder.hasParams()) {
            query = find(queryBuilder.query(), queryBuilder.sort(), queryBuilder.params());
        } else if (queryBuilder.hasQuery()) {
            query = find(queryBuilder.query(), queryBuilder.sort());
        } else {
            query = findAll(queryBuilder.sort());
        }

        query.page(queryBuilder.page(pageRequest));
        return query.list();
    }

    /** @see #findAll(PageRequest, FilterRequest) */
    @Override
    public long count(FilterRequest filterRequest)
    {
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest, ProductEntity.class, CATEGORY_REWRITE, ALLOWED_FILTER_FIELDS);

        if (queryBuilder.hasQuery() && queryBuilder.hasParams()) {
            return count(queryBuilder.query(), queryBuilder.params());
        }
        if (queryBuilder.hasQuery()) {
            return count(queryBuilder.query());
        }
        return count();
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

        ProductEntity product = find("select p from ProductEntity p " +
                "left join fetch p.brand " +
                "where p.id = ?1", productId)
                .firstResult();

        if (product != null) {
            attachCategories(List.of(product));
        }
        return product;
    }

    public long countShoppingProducts(FilterRequest filterRequest, boolean onSale, Boolean inStockOnly)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> shoppingPriceTypes = onSale
                ? List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
                : List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest, ProductEntity.class, CATEGORY_REWRITE, ALLOWED_FILTER_FIELDS);

        String hql = "select count(p) from ProductEntity p where " + activeVariantExistsClause(true);

        if (Boolean.TRUE.equals(inStockOnly)) {
            hql += " AND EXISTS (SELECT 1 FROM ProductVariantEntity sv " +
                    "WHERE sv.product = p " +
                    "AND sv.status = :variantStatus " +
                    "AND sv.stockQuantity > 0)";
        }

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
                                                           CatalogueSortEn sortBy, PriceBasisEn priceBasis,
                                                           Boolean inStockOnly)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> shoppingPriceTypes = onSale
                ? List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
                : List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest, ProductEntity.class, CATEGORY_REWRITE, ALLOWED_FILTER_FIELDS);

        CatalogueSortEn effectiveSort = sortBy != null ? sortBy : CatalogueSortEn.NAME_ASC;
        PriceBasisEn effectiveBasis = priceBasis != null ? priceBasis : PriceBasisEn.RETAIL;

        // ─── Step 1: ID-selection query (paged, no collection fetch, no DISTINCT) ───
        boolean needsSortKey = effectiveSort != CatalogueSortEn.NAME_ASC;

        String sortKeyExpr = needsSortKey ? priceSortKeyExpression(effectiveBasis) : null;

        // Always select two columns so the result is consistently Object[]
        String idQuery = "select p.id, " +
                (needsSortKey ? sortKeyExpr + " as sortKey" : "p.name as sortKey") +
                " from ProductEntity p where " + activeVariantExistsClause(true);

        if (Boolean.TRUE.equals(inStockOnly)) {
            idQuery += " AND EXISTS (SELECT 1 FROM ProductVariantEntity sv " +
                    "WHERE sv.product = p " +
                    "AND sv.status = :variantStatus " +
                    "AND sv.stockQuantity > 0)";
        }

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
            bindPriceSortKeyParams(params, effectiveBasis);
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

    /**
     * Total count of products matching {@link #findOnSaleProductEntities}'s predicate,
     * for pagination metadata. Shares {@link #activeVariantExistsClause} with that
     * method, so the two can no longer silently diverge.
     */
    public long countOnSaleProducts(boolean ignoreStatus)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> salePriceTypes = List.of(
                PriceTypeEn.RETAIL_SALE_PRICE,
                PriceTypeEn.WHOLESALE_SALE_PRICE);

        String hql = "select count(p) from ProductEntity p where " + activeVariantExistsClause(!ignoreStatus);

        TypedQuery<Long> q = getEntityManager().createQuery(hql, Long.class);
        q.setParameter("priceTypes", salePriceTypes);
        q.setParameter("now", now);
        if (!ignoreStatus) {
            q.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }

        Long result = q.getSingleResult();
        return result != null ? result : 0L;
    }

    public List<ProductEntity> findOnSaleProductEntities(PageRequest pageRequest, boolean ignoreStatus)
    {
        LocalDateTime now = LocalDateTime.now();
        List<PriceTypeEn> salePriceTypes = List.of(
                PriceTypeEn.RETAIL_SALE_PRICE,
                PriceTypeEn.WHOLESALE_SALE_PRICE);

        // ─── Step 1: ID-selection query (paged, no collection fetch, no DISTINCT) ───
        String idQuery = "select p.id from ProductEntity p where " + activeVariantExistsClause(!ignoreStatus) +
                " ORDER BY p.name ASC, p.id ASC";

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

    /**
     * "This product currently has at least one variant — active, when
     * {@code requireActiveStatus} — with a price bound to {@code :priceTypes} within
     * the active date window." An EXISTS predicate against {@code ProductEntity}, not a
     * join, so it never multiplies the outer query's rows.
     *
     * @param requireActiveStatus whether the variant must also be {@code ACTIVE}; false
     *                            when the caller wants to ignore variant status (e.g. an
     *                            admin "ignore status" toggle)
     */
    private static String activeVariantExistsClause(boolean requireActiveStatus)
    {
        return "EXISTS (SELECT 1 FROM ProductVariantEntity v JOIN v.prices vp " +
                "WHERE v.product = p " +
                (requireActiveStatus ? "AND v.status = :variantStatus " : "") +
                "AND vp.priceType IN :priceTypes " +
                "AND " + VariantPricesRepository.activeWindowClause("vp", "now") + ")";
    }

    /**
     * A scalar subquery expression yielding a product's lowest active price for
     * {@code basis} (RETAIL or WHOLESALE), for use as an ORDER BY sort key. Pair with
     * {@link #bindPriceSortKeyParams}.
     */
    private static String priceSortKeyExpression(PriceBasisEn basis)
    {
        return "(SELECT MIN(vp2.price) FROM ProductVariantEntity v2 JOIN v2.prices vp2 " +
                "WHERE v2.product = p AND v2.status = :variantStatus " +
                "AND vp2.priceType IN :sortPriceTypes " +
                "AND " + VariantPricesRepository.activeWindowClause("vp2", "now") + ")";
    }

    /** Binds the {@code :sortPriceTypes} parameter {@link #priceSortKeyExpression} needs. */
    private static void bindPriceSortKeyParams(Map<String, Object> params, PriceBasisEn basis)
    {
        params.put("sortPriceTypes", basis == PriceBasisEn.WHOLESALE
                ? List.of(PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
                : List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.RETAIL_SALE_PRICE));
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
     * Fetches ProductEntity records for the given IDs with brand eagerly joined (a
     * to-one association — safe to fetch-join) and categories batch-loaded and attached
     * separately via {@link #attachCategories} (a to-many association — joining it
     * directly would multiply each product's row once per category). The returned list
     * preserves the order of the supplied IDs.
     */
    private List<ProductEntity> fetchProductsByIds(List<UUID> ids)
    {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<ProductEntity> unordered = getEntityManager()
                .createQuery(
                        "select p from ProductEntity p " +
                                "left join fetch p.brand " +
                                "where p.id in :ids",
                        ProductEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        attachCategories(unordered);

        // Restore the ranked order returned by the aggregation query
        Map<UUID, ProductEntity> byId = unordered.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns up to {@code limit} random products excluding the given IDs, brand eagerly
     * joined and categories batch-attached — see {@link #fetchProductsByIds}.
     */
    private List<ProductEntity> findRandomProductEntitiesExcluding(int limit, List<UUID> excludeIds)
    {
        TypedQuery<ProductEntity> q;
        if (excludeIds == null || excludeIds.isEmpty()) {
            q = getEntityManager().createQuery(
                    "select p from ProductEntity p " +
                            "left join fetch p.brand " +
                            "order by function('random')",
                    ProductEntity.class);
        } else {
            q = getEntityManager().createQuery(
                            "select p from ProductEntity p " +
                                    "left join fetch p.brand " +
                                    "where p.id not in :excludeIds " +
                                    "order by function('random')",
                            ProductEntity.class)
                    .setParameter("excludeIds", excludeIds);
        }
        List<ProductEntity> products = q.setMaxResults(limit).getResultList();
        attachCategories(products);
        return products;
    }

    /**
     * Loads {@code categories} for a batch of already-loaded products in one query
     * (grouped by product id) and attaches them in memory, instead of a to-many JOIN
     * FETCH — which would multiply each product's row once per category and require
     * DISTINCT to collapse back down. Safe to replace the collection outright:
     * {@code ProductEntity.categories} cascades PERSIST only, not orphan removal, so this
     * cannot trigger an accidental delete.
     */
    private void attachCategories(List<ProductEntity> products)
    {
        if (products == null || products.isEmpty()) return;

        List<UUID> ids = products.stream().map(ProductEntity::getId).collect(Collectors.toList());

        List<Object[]> rows = getEntityManager()
                .createQuery("select p.id, c from ProductEntity p join p.categories c where p.id in :ids", Object[].class)
                .setParameter("ids", ids)
                .getResultList();

        Map<UUID, Set<CategoryEntity>> categoriesByProductId = new HashMap<>();
        for (Object[] row : rows) {
            UUID productId = (UUID) row[0];
            CategoryEntity category = (CategoryEntity) row[1];
            categoriesByProductId.computeIfAbsent(productId, k -> new HashSet<>()).add(category);
        }

        for (ProductEntity product : products) {
            product.setCategories(categoriesByProductId.getOrDefault(product.getId(), new HashSet<>()));
        }
    }

    /**
     * Resolves product ids matching a name-or-SKU search term via two separately
     * indexable queries rather than one OR across a cross-table EXISTS. Postgres cannot
     * push an index scan through one branch of an OR when the other branch is a
     * correlated subquery against a different table, so that shape always fell back to
     * a full sequential scan of products regardless of any index on name — each half
     * here is a simple, single-table predicate the trigram indexes can serve directly.
     */
    private Set<UUID> findProductIdsMatchingSearch(String search)
    {
        String searchPattern = "%" + search.trim().toLowerCase() + "%";

        List<UUID> byName = getEntityManager()
                .createQuery("SELECT p.id FROM ProductEntity p WHERE LOWER(p.name) LIKE :search", UUID.class)
                .setParameter("search", searchPattern)
                .getResultList();

        List<UUID> bySku = getEntityManager()
                .createQuery("SELECT sv.product.id FROM ProductVariantEntity sv WHERE LOWER(sv.sku) LIKE :search", UUID.class)
                .setParameter("search", searchPattern)
                .getResultList();

        Set<UUID> matchedIds = new LinkedHashSet<>(byName);
        matchedIds.addAll(bySku);
        return matchedIds;
    }

    /**
     * Paged admin product list (entities), with optional status/category/brand/search
     * filters. Returns the page of entities plus pagination metadata; the caller maps
     * each entity to its DTO.
     */
    public PageResponse<ProductEntity> findAdminProductPage(int pageIndex, int pageSize, String status, String categoryId, String brandId, String search)
    {
        int effectivePageSize = Math.clamp(pageSize, 1, 100);
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
            Set<UUID> matchedIds = findProductIdsMatchingSearch(search);
            if (matchedIds.isEmpty()) {
                return new PageResponse<>(List.of(), 0, 0, effectivePageIndex, effectivePageSize);
            }
            whereClause.append(" AND p.id IN :searchMatchIds");
            params.put("searchMatchIds", matchedIds);
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

        // Brand (to-one) is safe to fetch-join; categories (to-many) is batch-loaded
        // separately via attachCategories — see fetchProductsByIds for why.
        String fetchHql = "SELECT p FROM ProductEntity p " +
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
        attachCategories(products);

        return new PageResponse<>(products, totalElements, totalPages, effectivePageIndex, effectivePageSize);
    }

}
