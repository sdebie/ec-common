package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.CountrySettingsEntity;

@ApplicationScoped
public class CountrySettingsRepository extends BaseRepository<CountrySettingsEntity, String>
{
    @Override
    protected Class<CountrySettingsEntity> getEntityClass()
    {
        return CountrySettingsEntity.class;
    }
}
