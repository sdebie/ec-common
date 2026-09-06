package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ImageBulkJobEntity;

import java.util.UUID;

@ApplicationScoped
public class ImageBulkJobRepository extends BaseRepository<ImageBulkJobEntity, UUID>
{
    @Override
    protected Class<ImageBulkJobEntity> getEntityClass()
    {
        return ImageBulkJobEntity.class;
    }
}
