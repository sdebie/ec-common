package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.WholesaleProfileEntity;

import java.util.UUID;

@ApplicationScoped
public class WholesaleProfileRepository extends BaseRepository<WholesaleProfileEntity, UUID>
{
    @Override
    protected Class<WholesaleProfileEntity> getEntityClass()
    {
        return WholesaleProfileEntity.class;
    }
}
