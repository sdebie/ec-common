package org.ecommerce.common.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.PanacheQueryBuilder;

import java.util.List;

public abstract class BaseRepository<T, ID> implements PanacheRepositoryBase<T, ID>
{
    public List<T> findAll(PageRequest pageRequest, FilterRequest filterRequest)
    {

        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);
        PanacheQuery<T> query;

        if (queryBuilder.hasQuery()) {
            query = findAll(queryBuilder.sort());
        } else if (queryBuilder.hasParams()) {
            query = find(queryBuilder.query(), queryBuilder.sort(), queryBuilder.params());

        } else {
            // Query string with no bound params — e.g. "active IS NOT NULL"
            query = find(queryBuilder.query(), queryBuilder.sort());

        }
        query.page(queryBuilder.page(pageRequest));
        return query.list();
    }

    public long count(FilterRequest filterRequest)
    {
        PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);

        if (queryBuilder.hasQuery()) {
            return count();
        }

        if (queryBuilder.hasParams()) {
            return count(queryBuilder.query(), queryBuilder.params());
        }
        return count(queryBuilder.query());
    }
}
