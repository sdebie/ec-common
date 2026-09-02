package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.QuoteRequestEntity;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class QuoteRequestRepository extends BaseRepository<QuoteRequestEntity, UUID>
{
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("status", "createdAt", "name", "company", "statusChangedAt", "quotedAmount");

    @Override
    protected Class<QuoteRequestEntity> getEntityClass()
    {
        return QuoteRequestEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }
}
