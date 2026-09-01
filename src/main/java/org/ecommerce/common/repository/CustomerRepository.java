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

/**
 * Data access for admin customer listing.
 * <p>
 * Delegates filtering, sorting and paging to {@link BaseRepository}, which already builds a
 * {@link org.ecommerce.common.query.PanacheQueryBuilder} from a {@link FilterRequest} — the
 * one thing this class still does by hand is expand the frontend's single {@code search}
 * filter key into the OR-across-three-fields group {@code PanacheQueryBuilder} has no way to
 * express as one flat filter. Everything else (the {@code shopperType}/{@code status} enum
 * filters, and now {@code sort}) passes straight through.
 * <p>
 * Sorting by {@code email} or by registration date means sorting by {@code user.email} /
 * {@code user.createdAt} — neither is a column on {@code CustomerEntity} itself, both live on
 * the linked {@link org.ecommerce.common.entity.UserEntity} via the existing {@code user}
 * association. {@code PanacheQueryBuilder} already navigates dotted paths through a JOIN for
 * filters (the search expansion below does exactly that for {@code user.email}), and the same
 * mechanism works for a sort field — so no new column, no migration, is needed to sort a
 * customer list by when the account was created.
 */
@ApplicationScoped
public class CustomerRepository extends BaseRepository<CustomerEntity, UUID>
{
    /**
     * Plain columns plus the two {@code user.*} paths the admin customer list actually
     * filters/sorts by today ({@code user.email} via the search expansion below, and both via
     * the retail/wholesale admin tables' sortable columns). Every other reachable path off
     * {@code user} — {@code passwordHash}, the {@code passwordResetCode*} family, {@code
     * mfaEnabled}, {@code roles} — is a credential or security-posture field and must never be
     * filterable: this is the exact shape of a real, previously-live vulnerability (a VIEWER-role
     * staff account could read password-reset-code hashes one character at a time via this
     * filter). {@code wholesaleProfile.creditLimit} is excluded for the same reason as the
     * anonymous wholesale-price-leak issue elsewhere in this codebase — a negotiated commercial
     * term, not something any staff role should be able to range-query.
     */
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "id", "firstName", "lastName", "shopperType", "status",
            "user.email", "user.createdAt", "user.lastLogin");

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

    /** Finds a customer by the email stored on the linked {@link org.ecommerce.common.entity.UserEntity}. */
    public CustomerEntity findByEmail(String email)
    {
        return find("lower(user.email) = lower(?1)", email).firstResult();
    }

    /** Paged admin customer list matching the given filters, ordered per {@code filterRequest.getSort()}. */
    public List<CustomerEntity> findForAdmin(FilterRequest filterRequest, PageRequest pageRequest)
    {
        return findAll(pageRequest, expandSearch(filterRequest));
    }

    /** Count of admin customers matching the given filters. */
    public long countForAdmin(FilterRequest filterRequest)
    {
        return count(expandSearch(filterRequest));
    }

    /**
     * Rewrites a {@code search} filter (meaning "match name or email") into the OR group it
     * actually is, leaving every other filter, group and the sort untouched. A request with
     * no {@code search} filter passes through unchanged rather than being wrapped in a copy —
     * the common case does no extra work.
     */
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
