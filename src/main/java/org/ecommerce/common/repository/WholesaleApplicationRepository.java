package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.WholesaleApplicationEntity;
import org.ecommerce.common.enums.WholesaleApplicationStatusEn;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.SortRequest;

import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class WholesaleApplicationRepository extends BaseRepository<WholesaleApplicationEntity, UUID>
{
    @Override
    protected Class<WholesaleApplicationEntity> getEntityClass() {
        return WholesaleApplicationEntity.class;
    }

    private static final Set<String> ADMIN_SORTABLE_FIELDS = Set.of("createdAt", "status");

    public List<WholesaleApplicationEntity> findForAdmin(WholesaleApplicationStatusEn status, OffsetDateTime from, OffsetDateTime to, SortRequest sort, PageRequest pageRequest) {

        PageRequest page = pageRequest == null ? new PageRequest() : pageRequest;
        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(status, from, to, params);

        TypedQuery<WholesaleApplicationEntity> query = getEntityManager()
                .createQuery("select a from WholesaleApplicationEntity a" + where + adminOrderByClause(sort, ADMIN_SORTABLE_FIELDS, "a", "createdAt"), WholesaleApplicationEntity.class)
                .setFirstResult(page.getOffset())
                .setMaxResults(page.getPageSize());
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    public long countForAdmin(WholesaleApplicationStatusEn status, OffsetDateTime from, OffsetDateTime to) {

        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(status, from, to, params);

        TypedQuery<Long> query = getEntityManager()
                .createQuery("select count(a.id) from WholesaleApplicationEntity a" + where, Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    private String adminWhereClause(WholesaleApplicationStatusEn status, OffsetDateTime from, OffsetDateTime to, Map<String, Object> params) {

        List<String> clauses = new ArrayList<>();

        if (status != null) {
            clauses.add("a.status = :status");
            params.put("status", status);
        }
        if (from != null) {
            clauses.add("a.createdAt >= :from");
            params.put("from", from);
        }
        if (to != null) {
            clauses.add("a.createdAt < :to");
            params.put("to", to);
        }

        return clauses.isEmpty() ? "" : " where " + String.join(" and ", clauses);
    }

}
