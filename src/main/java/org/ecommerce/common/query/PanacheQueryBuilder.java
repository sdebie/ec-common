package org.ecommerce.common.query;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.ecommerce.common.query.enums.LogicalOperator;
import org.ecommerce.common.query.enums.SortDirection;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.UUID;

public class PanacheQueryBuilder
{
    /**
     * Rewrites a filter whose key starts with {@code keyPrefix} into an {@code EXISTS}
     * subquery against a to-many association, instead of letting it join directly — a direct
     * join against a collection-valued association multiplies the root query's rows once per
     * matched child, which then needs {@code DISTINCT} to collapse back; the EXISTS form never
     * multiplies anything. Pass one of these to {@link #from(FilterRequest, Class, CollectionExistsRewrite)}
     * when a filterable field lives on a collection-valued (one-to-many/many-to-many)
     * association rather than a plain or to-one one.
     *
     * @param keyPrefix       the filter-key prefix that should be rewritten, e.g. {@code "category."}
     * @param outerAlias      the root entity's alias in the enclosing query, e.g. {@code "p"}
     * @param collectionField the collection-valued field on the root entity, e.g. {@code "categories"}
     * @param targetEntity    the collection element's entity name, e.g. {@code "CategoryEntity"}
     * @param targetAlias     the alias for the EXISTS subquery's own entity reference
     */
    public record CollectionExistsRewrite(String keyPrefix, String outerAlias, String collectionField,
                                           String targetEntity, String targetAlias)
    {
    }

    private final FilterRequest filterRequest;
    private final Class<?> entityClass;
    private final CollectionExistsRewrite collectionRewrite;
    private final Set<String> allowedFields;
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> paramMap = new LinkedHashMap<>();

    // unique param name counter
    private int seq = 0;
    private String builtQuery;
    private Sort builtSort;
    private Map<String, Object> builtParams;

    /**
     * @param allowedFields the exact set of JPQL field-path strings (plain columns or dotted
     *                       association paths) a caller may filter or sort by, or {@code null}
     *                       for no restriction. {@link #sanitize} only checks a field name is
     *                       syntactically well-formed — it happily permits a dotted path
     *                       straight through an association to a sensitive column (e.g.
     *                       {@code "user.passwordHash"}) — so passing {@code null} here is a
     *                       real, unrestricted-reachability decision, not just "unset". An
     *                       empty (non-null) set means every field/sort request is rejected.
     */
    public PanacheQueryBuilder(FilterRequest filterRequest, Class<?> entityClass, CollectionExistsRewrite collectionRewrite, Set<String> allowedFields)
    {
        this.filterRequest = filterRequest != null ? filterRequest : new FilterRequest();
        this.entityClass = entityClass;
        this.collectionRewrite = collectionRewrite;
        this.allowedFields = allowedFields;
    }

    public static PanacheQueryBuilder from(FilterRequest filterRequest, Class<?> entityClass, CollectionExistsRewrite collectionRewrite, Set<String> allowedFields)
    {
        return new PanacheQueryBuilder(filterRequest, entityClass, collectionRewrite, allowedFields).build();
    }

    private PanacheQueryBuilder build()
    {
        // 1. Flat top-level filters (AND-ed together)
        if (filterRequest.getFilters() != null) {
            for (Filter filter : filterRequest.getFilters()) {
                String clause = buildFilter(filter);
                if (clause != null) {
                    whereClauses.add(clause);
                }
            }
        }

        // 2. Group filters (each group becomes a bracketed expression)
        if (filterRequest.getFilterGroups() != null) {
            for (FilterGroup filterGroup : filterRequest.getFilterGroups()) {
                String clause = buildGroup(filterGroup);
                if (clause != null && !clause.isBlank()) {
                    whereClauses.add(clause);
                }
            }
        }

        // 3. Combine everything at the top level with AND
        builtQuery = String.join(" AND ", whereClauses);

        // 4. Sort
        builtSort = buildSort(filterRequest.getSort());

        // 5. Parameters
        builtParams = Collections.unmodifiableMap(paramMap);

        return this;
    }


    private Sort buildSort(List<SortRequest> sortRequests)
    {
        if (sortRequests == null || sortRequests.isEmpty()) return null;

        Sort sort = null;

        for (SortRequest sortRequest : sortRequests) {
            if (sortRequest.getField() == null || sortRequest.getField().isBlank()) continue;
            String field = sanitize(sortRequest.getField());
            // Unlike an unpermitted filter (see buildFilter), an unpermitted sort field is
            // skipped rather than rejected — there is a sensible fallback (the next requested
            // field, or Sort.by("id") below) the way there isn't for a dropped filter, and
            // silently falling through gives a caller probing for gated fields no signal at
            // all, matching this codebase's existing "unresolvable sort request is not a
            // malformed query" convention (see BaseRepository#adminOrderByClause).
            if (allowedFields != null && !allowedFields.contains(field)) continue;
            Sort.Direction dir = sortRequest.getDirection() == SortDirection.DESC ? Sort.Direction.Descending : Sort.Direction.Ascending;
            sort = (sort == null) ? Sort.by(field, dir) : sort.and(field, dir);
        }
        return sort != null ? sort : Sort.by("id");
    }

    /**
     * Prevent JPQL injection — only alphanumerics, underscores, and dots allowed.
     * Dot notation supports JOIN navigation (e.g. "address.city").
     * Delegates to the shared {@link FieldNameValidator} so the rule has one definition.
     */
    private String sanitize(String field)
    {
        return FieldNameValidator.validate(field);
    }

    private String buildGroup(FilterGroup filterGroup)
    {
        if (filterGroup == null || filterGroup.isEmpty()) return null;

        List<String> parts = new ArrayList<>();

        if (filterGroup.getFilters() != null) {
            for (Filter f : filterGroup.getFilters()) {
                String c = buildFilter(f);
                if (c != null) parts.add(c);
            }
        }

        if (filterGroup.getFilterGroups() != null) {
            for (FilterGroup sub : filterGroup.getFilterGroups()) {
                String c = buildGroup(sub);
                if (c != null && !c.isBlank()) parts.add("(" + c + ")");
            }
        }

        if (parts.isEmpty()) return null;

        String joiner = filterGroup.getOperator() == LogicalOperator.OR ? " OR " : " AND ";
        return parts.size() == 1 ? parts.getFirst() : "(" + String.join(joiner, parts) + ")";
    }

    private String buildFilter(Filter filter)
    {
        if (filter == null || filter.getKey() == null || filter.getKey().isBlank()) {
            return null;
        }

        String field = sanitize(filter.getKey());

        // Unlike an unpermitted sort field, an unpermitted filter field is rejected outright
        // rather than dropped: there is no sensible "default filter" to fall back to, and
        // silently dropping it would make the query return MORE rows than the caller (or a
        // security boundary relying on this filter) expects, instead of fewer — the opposite
        // failure mode from a malformed request. FieldNameValidator only checks that a field
        // name is syntactically well-formed (dots included, for JOIN navigation), never
        // whether it's actually safe to expose — that's this allowlist's job.
        if (allowedFields != null && !allowedFields.contains(field)) {
            throw new IllegalArgumentException("Filtering by \"" + field + "\" is not permitted");
        }

        String p = "p" + seq++;

        // Resolve this field's declared Java type via reflection so values coerce to what
        // the column actually is (an enum, an Instant, ...) instead of a shape-based guess —
        // see coerceValue.
        Class<?> fieldType = resolveFieldType(field);

        String clause = switch (filter.getOperator()) {
            case EQUALS -> {
                bind(p, coerceValue(filter.getValue(), fieldType));
                yield field + " = :" + p;
            }
            case NOT_EQUALS -> {
                bind(p, coerceValue(filter.getValue(), fieldType));
                yield field + " != :" + p;
            }
            case GREATER_THAN -> {
                bind(p, coerceValue(filter.getValue(), fieldType));
                yield field + " > :" + p;
            }
            case GREATER_THAN_OR_EQUALS -> {
                bind(p, coerceValue(filter.getValue(), fieldType));
                yield field + " >= :" + p;
            }
            case LESS_THAN -> {
                bind(p, coerceValue(filter.getValue(), fieldType));
                yield field + " < :" + p;
            }
            case LESS_THAN_OR_EQUALS -> {
                bind(p, coerceValue(filter.getValue(), fieldType));
                yield field + " <= :" + p;
            }
            case IN -> {
                bind(p, coerceValues(filter.getValues(), fieldType));
                yield field + " IN (:" + p + ")";
            }
            case NOT_IN -> {
                bind(p, coerceValues(filter.getValues(), fieldType));
                yield field + " NOT IN (:" + p + ")";
            }
            case BETWEEN -> {
                yield field + " BETWEEN :" + p + " AND :" + bindBetweenBounds(p, filter, fieldType);
            }
            case NOT_BETWEEN -> {
                yield field + " NOT BETWEEN :" + p + " AND :" + bindBetweenBounds(p, filter, fieldType);
            }
            case LIKE -> {
                bind(p, "%" + filter.getValue() + "%");
                yield field + " LIKE :" + p;
            }
            case ILIKE -> {
                bind(p, "%" + filter.getValue().toLowerCase() + "%");
                yield "LOWER(" + field + ") LIKE :" + p;
            }
            case NOT_LIKE -> {
                bind(p, "%" + filter.getValue() + "%");
                yield field + " NOT LIKE :" + p;
            }
            case IS_NULL -> field + " IS NULL";
            case IS_NOT_NULL -> field + " IS NOT NULL";
            default -> throw new IllegalArgumentException("Unsupported operator: " + filter.getOperator());
        };

        if (collectionRewrite != null && filter.getKey().startsWith(collectionRewrite.keyPrefix())) {
            return "EXISTS (SELECT 1 FROM " + collectionRewrite.targetEntity() + " " + collectionRewrite.targetAlias() +
                    " WHERE " + collectionRewrite.targetAlias() + " MEMBER OF " +
                    collectionRewrite.outerAlias() + "." + collectionRewrite.collectionField() +
                    " AND " + clause + ")";
        }
        return clause;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void bind(String key, Object value)
    {
        paramMap.put(key, value);
    }

    /**
     * Binds a BETWEEN/NOT_BETWEEN filter's two bounds — {@code fromParam} (already reserved by
     * the caller) and a freshly-generated second param name, which this returns so the caller
     * can splice it into the JPQL. Requires exactly two values, the same list {@link
     * Filter#getValues} already carries for IN/NOT_IN.
     */
    private String bindBetweenBounds(String fromParam, Filter filter, Class<?> fieldType)
    {
        List<Object> bounds = coerceValues(filter.getValues(), fieldType);
        if (bounds.size() != 2) {
            throw new IllegalArgumentException("BETWEEN/NOT_BETWEEN requires exactly two values for \"" + filter.getKey() + "\"");
        }

        String toParam = "p" + seq++;
        bind(fromParam, bounds.get(0));
        bind(toParam, bounds.get(1));
        return toParam;
    }

    /**
     * Resolves the declared Java type of a JPQL field-path by inspecting the entity class
     * via reflection. Dot-notation fields (e.g. "address.city") walk the chain.
     * Returns null if the path doesn't resolve or the entity class is unknown.
     */
    private Class<?> resolveFieldType(String fieldName)
    {
        if (entityClass == null) return null;
        try {
            String[] parts = fieldName.split("\\.");
            // If the first segment is not a Java field on the entity it is a JPQL alias
            // (e.g. "p" in "p.status"). Skip it so the remaining path resolves correctly.
            int start = (parts.length > 1 && findField(entityClass, parts[0]) == null) ? 1 : 0;
            Class<?> current = entityClass;
            for (int i = start; i < parts.length; i++) {
                Field f = findField(current, parts[i]);
                if (f == null) return null;
                current = f.getType();
            }
            return current;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> clazz, String name)
    {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        return null;
    }

    /**
     * Coerces a single filter value to the field's resolved type when one is known (an enum,
     * or {@link Instant} for a timestamp column) — falling back to {@link #coerce}'s
     * shape-based guess when the field type is unresolved or isn't one of those. Resolving
     * the real type first, rather than guessing from the string alone, is what lets an
     * ISO-8601 timestamp bind correctly against an {@code Instant} column instead of falling
     * through to a raw string.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object coerceValue(String value, Class<?> fieldType)
    {
        if (value == null) return null;
        if (fieldType != null && fieldType.isEnum()) return coerceToEnum(value, (Class<? extends Enum>) fieldType);
        if (fieldType == Instant.class) return coerceToInstant(value);
        return coerce(value);
    }

    private List<Object> coerceValues(List<String> values, Class<?> fieldType)
    {
        if (values == null) return Collections.emptyList();
        List<Object> out = new ArrayList<>();
        for (String v : values) out.add(coerceValue(v, fieldType));
        return out;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object coerceToEnum(String value, Class<? extends Enum> enumClass)
    {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException ignored) {
            return value;
        }
    }

    /**
     * Parses an ISO-8601 instant string (e.g. "2026-01-01T00:00:00Z"). Falls back to the raw
     * string on a malformed value — the same best-effort contract as {@link #coerce} — so a
     * bad filter value degrades to a clause that fails to match (or errors at the query
     * layer) rather than this method throwing.
     */
    private Object coerceToInstant(String value)
    {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return value;
        }
    }

    /**
     * Best-effort coercion from String to a more specific type, used when the field's actual
     * type is unknown (no entity class was given) or isn't one of the types
     * {@link #coerceValue} special-cases.
     */
    private Object coerce(String value)
    {
        if (value == null) return null;
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) return Boolean.parseBoolean(value);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    public boolean hasQuery()
    {
        return builtQuery != null && !builtQuery.isBlank();
    }

    /**
     * The JPQL where-clause string, or empty string if no filters were set.
     */
    public String query()
    {
        return builtQuery;
    }

    /**
     * The Panache Sort descriptor. Defaults to "id ASC" if no sort was set.
     */
    public Sort sort()
    {
        return builtSort;
    }

    /**
     * Named parameters as a plain Map — pass directly to Panache's
     * find(query, sort, params) overload that accepts Map<String, Object>.
     * Only call this when hasParams() is true.
     */
    public Map<String, Object> params()
    {
        return builtParams;
    }

    /**
     * True when filters produced bound parameters (i.e. not IS_NULL / IS_NOT_NULL only).
     */
    public boolean hasParams()
    {
        return !builtParams.isEmpty();
    }

    /**
     * Converts a PageRequest to a Panache Page.
     */
    public Page page(PageRequest pageRequest)
    {
        PageRequest p = pageRequest != null ? pageRequest : new PageRequest();
        return Page.of(p.getPageIndex(), p.getPageSize());
    }
}
