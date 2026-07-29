package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.QuoteRequestEntity;

import java.util.UUID;

@ApplicationScoped
public class QuoteRequestRepository extends BaseRepository<QuoteRequestEntity, UUID> {

    @Override
    protected Class<QuoteRequestEntity> getEntityClass() {
        return QuoteRequestEntity.class;
    }
}
