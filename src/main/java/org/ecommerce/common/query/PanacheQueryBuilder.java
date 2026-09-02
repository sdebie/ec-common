package org.ecommerce.common.query;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.ecommerce.common.query.enums.LogicalOperator;
import org.ecommerce.common.query.enums.SortDirection;

import java.lang.reflect.Field;
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

    public PanacheQueryBuilder(FilterRequest filterRequest)
    {
        this(filterRequest, null, null, null);
    }

    public PanacheQueryBuilder(FilterRequest filterRequest, Class<?> entityClass)
    {
        this(filterRequest, entityClass, null, null);
    }

    public PanacheQueryBuilder(FilterRequest filterRequest, Class<?> entityClass, CollectionExistsRewrite collectionRewrite)
    {
        this(filterRequest, entityClass, collectionRewrite, null);
    }

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

    public static PanacheQueryBuilder from(FilterRequest filterRequest)
    {
        return new PanacheQueryBuilder(filterRequest, null, null, null).build();
    }

    public static PanacheQueryBuilder from(FilterRequest filterRequest, Class<?> entityClass)
    {
        return new PanacheQueryBuilder(filterRequest, entityClass, null, null).build();
    }

    public static PanacheQueryBuilder from(FilterRequest filterRequest, Class<?> entityClass, CollectionExistsRewrite collectionRewrite)
    {
        return new PanacheQueryBuilder(filterRequest, entityClass, collectionRewrite, null).build();
    }

    public static PanacheQueryBuilder from(FilterRequest filterRequest, Class<?> entityClass, Set<String> allowedFields)
    {
        return new PanacheQueryBuilder(filterRequest, entityClass, null, allowedFields).build();
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

        // Resolve the exact enum class for this field via reflection on the entity class.
        @SuppressWarnings("rawtypes")
        Class<? extends Enum> enumType = resolveEnumType(field);

        String clause = switch (filter.getOperator()) {
            case EQUALS -> {
                bind(p, enumType != null ? coerceToEnum(filter.getValue(), enumType) : coerce(filter.getValue()));
                yield field + " = :" + p;
            }
            case NOT_EQUALS -> {
                bind(p, enumType != null ? coerceToEnum(filter.getValue(), enumType) : coerce(filter.getValue()));
                yield field + " != :" + p;
            }
            case GREATER_THAN -> {
                bind(p, coerce(filter.getValue()));
                yield field + " > :" + p;
            }
            case GREATER_THAN_OR_EQUALS -> {
                bind(p, coerce(filter.getValue()));
                yield field + " >= :" + p;
            }
            case LESS_THAN -> {
                bind(p, coerce(filter.getValue()));
                yield field + " < :" + p;
            }
            case LESS_THAN_OR_EQUALS -> {
                bind(p, coerce(filter.getValue()));
                yield field + " <= :" + p;
            }
            case IN -> {
                bind(p, enumType != null ? coerceToEnumList(filter.getValues(), enumType) : coerceList(filter.getValues()));
                yield field + " IN (:" + p + ")";
            }
            case NOT_IN -> {
                bind(p, enumType != null ? coerceToEnumList(filter.getValues(), enumType) : coerceList(filter.getValues()));
                yield field + " NOT IN (:" + p + ")";
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
     * Resolves the enum class for a given JPQL field name by inspecting the entity class
     * via reflection. Dot-notation fields (e.g. "address.city") walk the chain.
     * Returns null if the field is not an enum or the entity class is unknown.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Class<? extends Enum> resolveEnumType(String fieldName)
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
            return current.isEnum() ? (Class<? extends Enum>) current : null;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object coerceToEnum(String value, Class<? extends Enum> enumClass)
    {
        if (value == null) return null;
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException ignored) {
            return value;
        }
    }

    @SuppressWarnings("rawtypes")
    private List<Object> coerceToEnumList(List<String> values, Class<? extends Enum> enumClass)
    {
        if (values == null) return Collections.emptyList();
        List<Object> out = new ArrayList<>();
        for (String v : values) out.add(coerceToEnum(v, enumClass));
        return out;
    }

    /**
     * Best-effort coercion from String to a more specific type.
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

    private List<Object> coerceList(List<String> values)
    {
        if (values == null) return Collections.emptyList();
        List<Object> out = new ArrayList<>();
        for (String v : values) out.add(coerce(v));
        return out;
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
