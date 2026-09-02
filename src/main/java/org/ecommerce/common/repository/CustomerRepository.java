package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.CustomerEntity;
import org.ecommerce.common.query.Filter;
import org.ecommerce.common.query.FilterGroup;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.enums.FilterOperator;
import org.ecommerce.common.query.enums.LogicalOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class CustomerRepository extends BaseRepository<CustomerEntity, UUID>
{
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("id", "firstName", "lastName", "shopperType", "status", "user.email", "user.createdAt", "user.lastLogin");

    @Override
    protected Class<CustomerEntity> getEntityClass()
    {
        return CustomerEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }

    public CustomerEntity findByEmail(String email)
    {
        return find("lower(user.email) = lower(?1)", email).firstResult();
    }

    public List<CustomerEntity> findForAdmin(FilterRequest filterRequest, PageRequest pageRequest)
    {
        return findAll(pageRequest, expandSearch(filterRequest));
    }

    public long countForAdmin(FilterRequest filterRequest)
    {
        return count(expandSearch(filterRequest));
    }

    private FilterRequest expandSearch(FilterRequest filterRequest)
    {
        if (filterRequest == null || filterRequest.getFilters() == null) {
            return filterRequest;
        }

        List<Filter> remaining = new ArrayList<>();
        String searchTerm = null;
        for (Filter filter : filterRequest.getFilters()) {
            if ("search".equals(filter.getKey()) && filter.getValue() != null) {
                searchTerm = filter.getValue();
            } else {
                remaining.add(filter);
            }
        }

        if (searchTerm == null) {
            return filterRequest;
        }

        FilterGroup searchGroup = new FilterGroup();
        searchGroup.setOperator(LogicalOperator.OR);
        searchGroup.setFilters(List.of(
                new Filter("firstName", FilterOperator.ILIKE, searchTerm),
                new Filter("lastName", FilterOperator.ILIKE, searchTerm),
                new Filter("user.email", FilterOperator.ILIKE, searchTerm)
        ));

        List<FilterGroup> groups = new ArrayList<>(
                filterRequest.getFilterGroups() == null ? List.of() : filterRequest.getFilterGroups());
        groups.add(searchGroup);

        FilterRequest expanded = new FilterRequest();
        expanded.setFilters(remaining);
        expanded.setFilterGroups(groups);
        expanded.setSort(filterRequest.getSort());
        return expanded;
    }
}
