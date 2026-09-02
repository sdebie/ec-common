package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.SageSettingsEntity;

import java.util.UUID;

@ApplicationScoped
public class SageSettingsRepository extends BaseRepository<SageSettingsEntity, UUID>
{
    @Override
    protected Class<SageSettingsEntity> getEntityClass() {
        return SageSettingsEntity.class;
    }
}
