package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.CategoryEntity;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.enums.*;
import org.ecommerce.common.query.*;
import org.ecommerce.common.query.enums.FilterOperator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
    private static final PanacheQueryBuilder.CollectionExistsRewrite CATEGORY_REWRITE = new PanacheQueryBuilder.CollectionExistsRewrite("category.", "p", "categories", "CategoryEntity", "category");
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "id", "slug", "name", "status", "productType", "isFeatured", "createdAt",
            "brand.id", "category.id");
    private static final List<PriceTypeEn> SALE_PRICE_TYPES = List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
    private static final List<PriceTypeEn> ALL_SHOPPING_PRICE_TYPES = List.of(
            PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);

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

    @Override
    protected PanacheQueryBuilder.CollectionExistsRewrite collectionRewrite()
    {
        return CATEGORY_REWRITE;
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
        List<PriceTypeEn> shoppingPriceTypes = onSale ? SALE_PRICE_TYPES : ALL_SHOPPING_PRICE_TYPES;
        PanacheQueryBuilder queryBuilder = buildQueryBuilder(filterRequest);

        String hql = "select count(p) from ProductEntity p where " + shoppingProductsWhereClause(Boolean.TRUE.equals(inStockOnly), queryBuilder);

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
        List<PriceTypeEn> shoppingPriceTypes = onSale ? SALE_PRICE_TYPES : ALL_SHOPPING_PRICE_TYPES;
        PanacheQueryBuilder queryBuilder = buildQueryBuilder(filterRequest);

        CatalogueSortEn effectiveSort = sortBy != null ? sortBy : CatalogueSortEn.NAME_ASC;
        PriceBasisEn effectiveBasis = priceBasis != null ? priceBasis : PriceBasisEn.RETAIL;

        // ─── Step 1: ID-selection query (paged, no collection fetch, no DISTINCT) ───
        boolean needsSortKey = effectiveSort != CatalogueSortEn.NAME_ASC;

        String sortKeyExpr = needsSortKey ? priceSortKeyExpression(effectiveBasis) : null;

        // Always select two columns so the result is consistently Object[]
        String idQuery = "select p.id, " +
                (needsSortKey ? sortKeyExpr + " as sortKey" : "p.name as sortKey") +
                " from ProductEntity p where " + shoppingProductsWhereClause(Boolean.TRUE.equals(inStockOnly), queryBuilder);

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

        List<Object[]> rows = buildPagedQuery(idQuery, Object[].class, params, pageRequest).getResultList();

        List<UUID> ids = rows.stream()
                .map(row -> (UUID) row[0])
                .collect(Collectors.toList());

        return fetchProductsByIds(ids);
    }

    public long countOnSaleProducts(boolean ignoreStatus)
    {
        String hql = "select count(p) from ProductEntity p where " + activeVariantExistsClause(!ignoreStatus);

        TypedQuery<Long> q = getEntityManager().createQuery(hql, Long.class);
        q.setParameter("priceTypes", SALE_PRICE_TYPES);
        q.setParameter("now", LocalDateTime.now());
        if (!ignoreStatus) {
            q.setParameter("variantStatus", ProductStatusEn.ACTIVE);
        }

        Long result = q.getSingleResult();
        return result != null ? result : 0L;
    }

    public List<ProductEntity> findOnSaleProductEntities(PageRequest pageRequest, boolean ignoreStatus)
    {
        String idQuery = "select p.id from ProductEntity p where " + activeVariantExistsClause(!ignoreStatus) +
                " ORDER BY p.name ASC, p.id ASC";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("priceTypes", SALE_PRICE_TYPES);
        params.put("now", LocalDateTime.now());
        if (!ignoreStatus) {
            params.put("variantStatus", ProductStatusEn.ACTIVE);
        }

        List<UUID> ids = buildPagedQuery(idQuery, UUID.class, params, pageRequest).getResultList();

        return fetchProductsByIds(ids);
    }

    private static String activeVariantExistsClause(boolean requireActiveStatus)
    {
        return "EXISTS (SELECT 1 FROM ProductVariantEntity v JOIN v.prices vp " +
                "WHERE v.product = p " +
                (requireActiveStatus ? "AND v.status = :variantStatus " : "") +
                "AND vp.priceType IN :priceTypes " +
                "AND " + VariantPricesRepository.activeWindowClause("vp", "now") + ")";
    }

    private static String priceSortKeyExpression(PriceBasisEn basis)
    {
        return "(SELECT MIN(vp2.price) FROM ProductVariantEntity v2 JOIN v2.prices vp2 " +
                "WHERE v2.product = p AND v2.status = :variantStatus " +
                "AND vp2.priceType IN :sortPriceTypes " +
                "AND " + VariantPricesRepository.activeWindowClause("vp2", "now") + ")";
    }

    private static void bindPriceSortKeyParams(Map<String, Object> params, PriceBasisEn basis)
    {
        params.put("sortPriceTypes", basis == PriceBasisEn.WHOLESALE
                ? List.of(PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
                : List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.RETAIL_SALE_PRICE));
    }

    /**
     * The shared "shopping-eligible product" predicate: an active-variant EXISTS clause,
     * optionally narrowed to in-stock variants, optionally ANDed with a client filter —
     * shared by {@link #countShoppingProducts} and {@link #findShoppingProductEntities} so
     * the two can never diverge on which products qualify.
     */
    private static String shoppingProductsWhereClause(boolean inStockOnly, PanacheQueryBuilder queryBuilder)
    {
        String clause = activeVariantExistsClause(true);
        if (inStockOnly) {
            clause += " AND EXISTS (SELECT 1 FROM ProductVariantEntity sv " +
                    "WHERE sv.product = p " +
                    "AND sv.status = :variantStatus " +
                    "AND sv.stockQuantity > 0)";
        }
        if (queryBuilder.hasQuery()) {
            clause += " AND " + queryBuilder.query();
        }
        return clause;
    }

    /** {@link PanacheQueryBuilder#from} with this repository's fixed category-rewrite/allowlist args, so callers only ever vary {@code filterRequest}. */
    private static PanacheQueryBuilder buildQueryBuilder(FilterRequest filterRequest)
    {
        return PanacheQueryBuilder.from(filterRequest, ProductEntity.class, CATEGORY_REWRITE, ALLOWED_FILTER_FIELDS);
    }

    /** Binds {@code params} onto a paged {@link TypedQuery} for {@code hql}, defaulting {@code pageRequest} like this class's other paginated methods. */
    private <R> TypedQuery<R> buildPagedQuery(String hql, Class<R> resultType, Map<String, Object> params, PageRequest pageRequest)
    {
        PageRequest effectivePage = pageRequest != null ? pageRequest : new PageRequest();
        TypedQuery<R> query = getEntityManager().createQuery(hql, resultType);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(effectivePage.getPageIndex() * effectivePage.getPageSize());
        query.setMaxResults(effectivePage.getPageSize());
        return query;
    }

    public List<ProductEntity> findTopBestSellerEntities()
    {
        final int TARGET = 10;

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

        List<ProductEntity> bestSellers = fetchProductsByIds(bestSellerIds);

        List<ProductEntity> result = new ArrayList<>(bestSellers);
        if (result.size() < TARGET) {
            int needed = TARGET - result.size();
            List<ProductEntity> random = findRandomProductEntitiesExcluding(needed, bestSellerIds);
            result.addAll(random);
        }

        return result;
    }

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

        Map<UUID, ProductEntity> byId = unordered.stream().collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

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

    private record AdminProductFilter(String whereClause, Map<String, Object> params)
    {
    }

    private AdminProductFilter buildAdminProductFilter(String status, String categoryId, String brandId, String search)
    {
        List<Filter> filters = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            filters.add(new Filter("status", FilterOperator.EQUALS, ProductStatusEn.valueOf(status).name()));
        }
        if (categoryId != null && !categoryId.isBlank()) {
            filters.add(new Filter("category.id", FilterOperator.EQUALS, UUID.fromString(categoryId).toString()));
        }
        if (brandId != null && !brandId.isBlank()) {
            filters.add(new Filter("brand.id", FilterOperator.EQUALS, UUID.fromString(brandId).toString()));
        }

        FilterRequest filterRequest = new FilterRequest();
        filterRequest.setFilters(filters);
        PanacheQueryBuilder queryBuilder = buildQueryBuilder(filterRequest);

        List<String> clauses = new ArrayList<>();
        Map<String, Object> params = new LinkedHashMap<>(queryBuilder.params());
        if (queryBuilder.hasQuery()) {
            clauses.add(queryBuilder.query());
        }

        if (search != null && !search.isBlank()) {
            Set<UUID> searchMatchIds = findProductIdsMatchingSearch(search);
            if (searchMatchIds.isEmpty()) {
                clauses.add("1=0");
            } else {
                clauses.add("p.id IN :searchMatchIds");
                params.put("searchMatchIds", searchMatchIds);
            }
        }

        String whereClause = clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
        return new AdminProductFilter(whereClause, params);
    }

    public long countAdminProducts(String status, String categoryId, String brandId, String search)
    {
        AdminProductFilter filter = buildAdminProductFilter(status, categoryId, brandId, search);
        TypedQuery<Long> countQuery = getEntityManager()
                .createQuery("SELECT COUNT(p) FROM ProductEntity p" + filter.whereClause(), Long.class);
        filter.params().forEach(countQuery::setParameter);
        return countQuery.getSingleResult();
    }

    public List<ProductEntity> findAdminProducts(PageRequest pageRequest, String status, String categoryId, String brandId, String search)
    {
        AdminProductFilter filter = buildAdminProductFilter(status, categoryId, brandId, search);

        String fetchHql = "SELECT p FROM ProductEntity p LEFT JOIN FETCH p.brand" + filter.whereClause() + " ORDER BY p.name ASC";
        TypedQuery<ProductEntity> fetchQuery = getEntityManager().createQuery(fetchHql, ProductEntity.class);
        filter.params().forEach(fetchQuery::setParameter);
        fetchQuery.setFirstResult(pageRequest.getOffset());
        fetchQuery.setMaxResults(pageRequest.getPageSize());

        List<ProductEntity> products = fetchQuery.getResultList();
        attachCategories(products);
        return products;
    }

}
