package org.ecommerce.common.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.ecommerce.common.query.*;
import org.ecommerce.common.query.enums.SortDirection;

import java.util.List;
import java.util.Set;

public abstract class BaseRepository<T, ID> implements PanacheRepositoryBase<T, ID>
{
    protected abstract Class<T> getEntityClass();

    /**
     * The exact set of JPQL field-path strings (plain columns, or dotted association paths
     * like {@code "user.email"}) a client may filter or sort this entity by via
     * {@link FilterRequest}. {@link org.ecommerce.common.query.FieldNameValidator} only
     * checks that a field name is syntactically well-formed — dots included, so it happily
     * permits association traversal to a column like {@code "user.passwordHash"} — it has no
     * idea which paths are actually safe to expose to a client. Empty by default: a repository
     * whose {@link #findAll(PageRequest, FilterRequest)}/{@link #count(FilterRequest)} is
     * never reached by client-supplied filters needs nothing here, and one that is must opt
     * in field-by-field, deliberately, rather than inherit a payload-shaped access-control
     * decision it never made.
     */
    protected Set<String> filterableFields()
    {
        return Set.of();
    }

    /**
     * Lets a subclass route its {@link #findAll(PageRequest, FilterRequest)}/
     * {@link #count(FilterRequest)} filtering through a
     * {@link PanacheQueryBuilder.CollectionExistsRewrite} — e.g. rewriting a filter on a
     * to-many association into a safe EXISTS subquery — without having to override either
     * method just to inject it. {@code null} by default (no rewrite).
     */
    protected PanacheQueryBuilder.CollectionExistsRewrite collectionRewrite()
    {
        return null;
    }

    /**
     * Shared "sort an admin list by a whitelisted column, newest-first by default" ORDER BY
     * builder — generic (no entity-specific field names), so it lives here rather than in a
     * one-off class. An unrecognized or absent sort field falls back to {@code defaultField
     * DESC} rather than erroring, matching {@link PanacheQueryBuilder}'s own default-sort
     * behaviour: an unresolvable sort request is not a malformed query, it is a request for
     * "however you'd normally order these".
     *
     * @param sort          the caller's requested sort, or null for the default
     * @param allowedFields the whitelist of field names this list may be sorted by
     * @param alias         the root entity's alias in the enclosing query
     * @param defaultField  the field to sort by (descending) when {@code sort} is null, blank,
     *                      or names a field outside {@code allowedFields}
     * @return {@code " order by alias.field asc"} or {@code " order by alias.field desc"}
     */
    protected static String adminOrderByClause(SortRequest sort, Set<String> allowedFields, String alias, String defaultField)
    {
        String field = defaultField;
        boolean descending = true;

        if (sort != null && sort.getField() != null && allowedFields.contains(sort.getField())) {
            field = sort.getField();
            descending = sort.getDirection() != SortDirection.ASC;
        }

        return " order by " + alias + "." + FieldNameValidator.validate(field) + (descending ? " desc" : " asc");
    }

    public List<T> findAll(PageRequest pageRequest, FilterRequest filterRequest)
    {
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest, getEntityClass(), collectionRewrite(), filterableFields());
        PanacheQuery<T> query;

        if (queryBuilder.hasQuery() && queryBuilder.hasParams()) {
            // Filtered query with bound parameters (e.g., ILIKE, EQUALS, …)
            query = find(queryBuilder.query(), queryBuilder.sort(), queryBuilder.params());
        } else if (queryBuilder.hasQuery()) {
            // Param-free clauses such as IS NULL / IS NOT NULL
            query = find(queryBuilder.query(), queryBuilder.sort());
        } else {
            // No filter at all — return every row
            query = findAll(queryBuilder.sort());
        }

        query.page(queryBuilder.page(pageRequest));
        return query.list();
    }

    public long count(FilterRequest filterRequest)
    {
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest, getEntityClass(), collectionRewrite(), filterableFields());

        if (queryBuilder.hasQuery() && queryBuilder.hasParams()) {
            return count(queryBuilder.query(), queryBuilder.params());
        }

        if (queryBuilder.hasQuery()) {
            // Param-free clauses such as IS NULL / IS NOT NULL
            return count(queryBuilder.query());
        }

        return count();
    }
}
