package org.ecommerce.common.query;

/**
 * Product-specific extension of {@link PanacheQueryBuilder} that rewrites
 * {@code category.*} predicates as {@code EXISTS} subqueries instead of relying
 * on a row-multiplying join + DISTINCT.
 * <p>
 * The override wraps category predicates so that:
 * <pre>
 *   category.id IN (:p0)
 * </pre>
 * becomes:
 * <pre>
 *   EXISTS (SELECT 1 FROM CategoryEntity category WHERE category MEMBER OF p.categories AND category.id IN (:p0))
 * </pre>
 * The subquery alias {@code category} shadows the same name that the parent's
 * {@code buildFilter} already emits as the field prefix, so the inner clause is
 * byte-identical to the non-wrapped version.
 */
public class ProductQueryBuilder extends PanacheQueryBuilder
{
    private ProductQueryBuilder(FilterRequest filterRequest, Class<?> entityClass)
    {
        super(filterRequest, entityClass);
    }

    /**
     * Factory method mirroring {@link PanacheQueryBuilder#from(FilterRequest, Class)}
     * but producing a product-aware builder that rewrites category predicates.
     */
    public static ProductQueryBuilder fromProduct(FilterRequest filterRequest, Class<?> entityClass)
    {
        ProductQueryBuilder builder = new ProductQueryBuilder(filterRequest, entityClass);
        builder.build();
        return builder;
    }

    @Override
    protected String buildFilter(Filter filter)
    {
        String clause = super.buildFilter(filter);
        if (clause == null) {
            return null;
        }
        if (isCategoryKey(filter)) {
            return wrapInExists(clause);
        }
        return clause;
    }

    private boolean isCategoryKey(Filter filter)
    {
        if (filter == null || filter.getKey() == null) {
            return false;
        }
        return filter.getKey().startsWith("category.") || filter.getKey().startsWith("categories.");
    }

    private String wrapInExists(String clause)
    {
        return "EXISTS (SELECT 1 FROM CategoryEntity category WHERE category MEMBER OF p.categories AND " + clause + ")";
    }
}
