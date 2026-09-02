package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.StoreSettingsEntity;

@ApplicationScoped
public class StoreSettingsRepository extends BaseRepository<StoreSettingsEntity, String>
{

    @Override
    protected Class<StoreSettingsEntity> getEntityClass()
    {
        return StoreSettingsEntity.class;
    }
}
