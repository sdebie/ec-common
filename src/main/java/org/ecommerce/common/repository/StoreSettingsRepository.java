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

    public long countByValueContaining(String path)
    {
        return getEntityManager()
                .createQuery("SELECT COUNT(s) FROM StoreSettingsEntity s WHERE s.value LIKE CONCAT('%', :path, '%')", Long.class)
                .setParameter("path", path)
                .getSingleResult();
    }
}
