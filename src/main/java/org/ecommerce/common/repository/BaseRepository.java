package org.ecommerce.common.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.ecommerce.common.enums.FilterOperators;
import org.ecommerce.common.request.FilterConditionRequest;
import org.ecommerce.common.request.SearchRequest;
import org.ecommerce.common.request.SortRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.ecommerce.common.enums.FilterOperators.EQUALS;

public abstract class BaseRepository<T, ID> implements PanacheRepositoryBase<T, ID>
{
    protected abstract Map<String, String> getAllowedFields();

    protected abstract String getDefaultSortField();

    public List<T> search(SearchRequest request)
    {
        QueryData queryData = buildWhereClause(request);
        Sort sort = buildSort(request != null ? request.getSort() : null);

        PanacheQuery<T> query = queryData.whereClause().isBlank()
                ? findAll(sort)
                : find(queryData.whereClause(), sort, queryData.params());

        query.page(buildPage(request));
        return query.list();
    }

    public long countSearch(SearchRequest request)
    {
        QueryData queryData = buildWhereClause(request);
        return queryData.whereClause().isBlank()
                ? count()
                : count(queryData.whereClause(), queryData.params());
    }

    private QueryData buildWhereClause(SearchRequest request)
    {
        if (request == null || request.getConditions() == null || request.getConditions().isEmpty()) {
            return new QueryData("", Map.of());
        }

        String logicalOperator = "OR".equalsIgnoreCase(request.getOperator().toString()) ? " OR " : " AND ";
        StringBuilder where = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        int paramIndex = 1;

        for (FilterConditionRequest condition : request.getConditions()) {
            String entityField = getAllowedFields().get(condition.getField());
            if (entityField == null) continue;

            String paramName = "p" + paramIndex++;
            String clause = buildConditionClause(entityField, condition, paramName, params);

            if (clause != null && !clause.isBlank()) {
                if (!where.isEmpty()) where.append(logicalOperator);
                where.append(clause);
            }
        }
        return new QueryData(where.toString(), params);
    }

    private String buildConditionClause(String field, FilterConditionRequest cond, String pName, Map<String, Object> params)
    {
        FilterOperators operator = FilterOperators.valueOf(cond.getOperator().toString().toUpperCase(Locale.ROOT));
        return switch (operator) {
            case EQUALS -> {
                params.put(pName, convertValue(field, cond.getValue()));
                yield field + " = :" + pName;
            }
            case LIKE -> {
                params.put(pName, "%" + cond.getValue().toLowerCase(Locale.ROOT) + "%");
                yield "lower(" + field + ") like :" + pName;
            }
            case NOT_LIKE -> {
                params.put(pName, "%" + cond.getValue().toLowerCase(Locale.ROOT) + "%");
                yield "lower(" + field + ") not like :" + pName;
            }
            case GREATER_THAN -> {
                params.put(pName, convertValue(field, cond.getValue()));
                yield field + " > :" + pName;
            }
            case GREATER_THAN_OR_EQUALS -> {
                params.put(pName, convertValue(field, cond.getValue()));
                yield field + " >= :" + pName;
            }
            case LESS_THAN -> {
                params.put(pName, convertValue(field, cond.getValue()));
                yield field + " < :" + pName;
            }
            case LESS_THAN_OR_EQUALS -> {
                params.put(pName, convertValue(field, cond.getValue()));
                yield field + " <= :" + pName;
            }
            case IS_NULL -> field + " is null";
            case IS_NOT_NULL -> field + " is not null";
            case IN, NOT_IN -> {
                String[] values = cond.getValue().split(",");
                List<Object> list = new ArrayList<>();
                for (String value : values) {
                    list.add(convertValue(value, cond.getValue()));
                }
                params.put(pName, list);
                yield operator == FilterOperators.IN ? field + " in (:" + pName + ")" : field + " not in (:" + pName + ")";
            }
            default -> null;
        };
    }

    protected Object convertValue(String entityField, String value)
    {
        if (value == null) return null;
        // Centralized date handling
        if (entityField.toLowerCase().endsWith("at") || entityField.toLowerCase().endsWith("date")) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                return value;
            }
        }
        return value;
    }

    private Sort buildSort(List<SortRequest> sortRequests)
    {
        if (sortRequests == null || sortRequests.isEmpty()) {
            return Sort.by(getDefaultSortField());
        }
        Sort sort = null;
        for (SortRequest req : sortRequests) {
            String field = getAllowedFields().get(req.getField());
            if (field == null) continue;

            Sort.Direction dir = "DESC".equalsIgnoreCase(req.getDirection().toString())
                    ? Sort.Direction.Descending : Sort.Direction.Ascending;

            sort = (sort == null) ? Sort.by(field, dir) : sort.and(field, dir);
        }
        return sort != null ? sort : Sort.by(getDefaultSortField());
    }

    private Page buildPage(SearchRequest request)
    {
        int index = (request != null && request.getPageIndex() != null) ? request.getPageIndex() : 0;
        int size = (request != null && request.getPageSize() != null) ? request.getPageSize() : 10;
        return Page.of(index, size);
    }

    private record QueryData(String whereClause, Map<String, Object> params)
    {
    }
}
