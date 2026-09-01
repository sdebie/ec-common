package org.ecommerce.common.repository;

import jakarta.persistence.TypedQuery;
import org.ecommerce.common.entity.WholesaleApplicationEntity;
import org.ecommerce.common.enums.WholesaleApplicationStatusEn;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.PanacheQueryBuilder;
import org.ecommerce.common.query.SortRequest;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class WholesaleApplicationRepository extends BaseRepository<WholesaleApplicationEntity, UUID>
{
    @Override
    protected Class<WholesaleApplicationEntity> getEntityClass()
    {
        return WholesaleApplicationEntity.class;
    }

    /**
     * Column names the admin queue may sort by. An unrecognised or absent column falls back
     * to the default (newest first) rather than erroring — see
     * {@link BaseRepository#adminOrderByClause}.
     */
    private static final Set<String> ADMIN_SORTABLE_FIELDS = Set.of("createdAt", "status");

    /**
     * Date-bounded admin listing, handwritten JPQL rather than the generic
     * {@link PanacheQueryBuilder}-backed {@code findAll} — the same reason
     * {@code OrderRepository.findForAdmin} exists: the generic filter coercion handles
     * boolean/UUID/Long/Double/String only, and would bind a plain date string against a
     * timestamp column instead of a real range comparison.
     */
    public List<WholesaleApplicationEntity> findForAdmin(WholesaleApplicationStatusEn status, OffsetDateTime from, OffsetDateTime to, SortRequest sort, PageRequest pageRequest)
    {
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

    /**
     * Total matching {@link #findForAdmin} under the same filters, for paging.
     */
    public long countForAdmin(WholesaleApplicationStatusEn status, OffsetDateTime from, OffsetDateTime to)
    {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = adminWhereClause(status, from, to, params);

        TypedQuery<Long> query = getEntityManager()
                .createQuery("select count(a.id) from WholesaleApplicationEntity a" + where, Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    /**
     * Builds the shared WHERE clause and fills {@code params} with its bindings.
     */
    private String adminWhereClause(WholesaleApplicationStatusEn status, OffsetDateTime from, OffsetDateTime to, Map<String, Object> params)
    {
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
