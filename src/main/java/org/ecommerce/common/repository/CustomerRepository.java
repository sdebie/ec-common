package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.CustomerEntity;
import org.ecommerce.common.enums.CustomerStatusEn;
import org.ecommerce.common.enums.CustomerTypeEn;
import org.ecommerce.common.query.Filter;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data access for admin customer listing. Owns the filter→query translation so
 * services stay business-logic-only.
 */
@ApplicationScoped
public class CustomerRepository extends BaseRepository<CustomerEntity, UUID>
{
    private static final Logger LOG = Logger.getLogger(CustomerRepository.class);

    @Override
    protected Class<CustomerEntity> getEntityClass()
    {
        return CustomerEntity.class;
    }

    /** Paged admin customer list matching the given filters. */
    public List<CustomerEntity> findForAdmin(FilterRequest filterRequest, PageRequest pageRequest)
    {
        QuerySpec spec = buildQuery(filterRequest);
        int pageIndex = pageRequest != null ? pageRequest.getPageIndex() : 0;
        int pageSize = pageRequest != null ? pageRequest.getPageSize() : 10;
        return find(spec.query, spec.params).page(pageIndex, pageSize).list();
    }

    /** Count of admin customers matching the given filters. */
    public long countForAdmin(FilterRequest filterRequest)
    {
        QuerySpec spec = buildQuery(filterRequest);
        return count(spec.query, spec.params);
    }

    private QuerySpec buildQuery(FilterRequest filterRequest)
    {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (filterRequest != null && filterRequest.getFilters() != null) {
            for (Filter f : filterRequest.getFilters()) {
                if (f.getKey() == null || f.getValue() == null) continue;

                switch (f.getKey()) {
                    case "shopperType" -> {
                        try {
                            params.put("shopperType", CustomerTypeEn.valueOf(f.getValue()));
                            query.append(" and shopperType = :shopperType");
                        } catch (IllegalArgumentException e) {
                            LOG.warnf("Invalid shopperType filter value: %s", f.getValue());
                        }
                    }
                    case "status" -> {
                        try {
                            params.put("status", CustomerStatusEn.valueOf(f.getValue()));
                            query.append(" and status = :status");
                        } catch (IllegalArgumentException e) {
                            LOG.warnf("Invalid status filter value: %s", f.getValue());
                        }
                    }
                    case "search" -> {
                        String term = "%" + f.getValue().toLowerCase() + "%";
                        params.put("search", term);
                        query.append(" and (lower(firstName) like :search or lower(lastName) like :search or lower(user.email) like :search)");
                    }
                }
            }
        }

        return new QuerySpec(query.toString(), params);
    }

    private record QuerySpec(String query, Map<String, Object> params) {}
}
