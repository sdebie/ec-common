package org.ecommerce.common.query;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.ecommerce.common.query.enums.LogicalOperator;
import org.ecommerce.common.query.enums.SortDirection;

import java.util.*;
import java.util.UUID;

public class PanacheQueryBuilder
{
    private final FilterRequest filterRequest;
    private final List<String> whereClauses = new ArrayList<>();
    private final Map<String, Object> paramMap = new LinkedHashMap<>();

    // unique param name counter
    private int seq = 0;
    private String builtQuery;
    private Sort builtSort;
    private Map<String, Object> builtParams;

    public PanacheQueryBuilder(FilterRequest filterRequest)
    {
        this.filterRequest = filterRequest != null ? filterRequest : new FilterRequest();
    }

    public static PanacheQueryBuilder from(FilterRequest filterRequest)
    {
        return new PanacheQueryBuilder(filterRequest).build();
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
        Sort panacheSort = buildSort(filterRequest.getSort());
        if (panacheSort != null) {
            builtSort = buildSort(filterRequest.getSort());
        }

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
            Sort.Direction dir = sortRequest.getDirection() == SortDirection.DESC ? Sort.Direction.Descending : Sort.Direction.Ascending;
            sort = (sort == null) ? Sort.by(sanitize(sortRequest.getField()), dir)
                    : sort.and(sanitize(sortRequest.getField()), dir);
        }
        return sort != null ? sort : Sort.by("id");
    }

    /**
     * Prevent JPQL injection — only alphanumerics, underscores, and dots allowed.
     * Dot notation supports JOIN navigation (e.g. "address.city").
     */
    private String sanitize(String field)
    {
        if (!field.matches("[\\w.]+")) {
            throw new IllegalArgumentException("Invalid field name: '" + field + "'");
        }
        return field;
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
        String p = "p" + seq++;

        return switch (filter.getOperator()) {
            case EQUALS -> {
                bind(p, coerce(filter.getValue()));
                yield field + " = :" + p;
            }
            case NOT_EQUALS -> {
                bind(p, coerce(filter.getValue()));
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
                bind(p, coerceList(filter.getValues()));
                yield field + " IN (:" + p + ")";
            }
            case NOT_IN -> {
                bind(p, coerceList(filter.getValues()));
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
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void bind(String key, Object value)
    {
        paramMap.put(key, value);
    }

    /**
     * Best-effort coercion from String to a more specific type.
     * Override in a subclass if you need entity-aware type coercion.
     */
    protected Object coerce(String value)
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

    protected List<Object> coerceList(List<String> values)
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
