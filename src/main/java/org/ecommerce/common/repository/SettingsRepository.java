package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.StoreSettingsEntity;

import java.util.List;

@ApplicationScoped
public class SettingsRepository extends BaseRepository<StoreSettingsEntity, String>
{

    @Override
    protected Class<StoreSettingsEntity> getEntityClass()
    {
        return StoreSettingsEntity.class;
    }

    public List<StoreSettingsEntity> getAllStoreSettings()
    {
        return listAll();
    }

    public void saveStoreSettings(StoreSettingsEntity entity)
    {
        getEntityManager().merge(entity);
    }
}
