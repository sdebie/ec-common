package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ImageBulkJobItemEntity;
import org.ecommerce.common.enums.ImageBulkJobItemStatusEn;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ImageBulkJobItemRepository extends BaseRepository<ImageBulkJobItemEntity, UUID>
{
    @Override
    protected Class<ImageBulkJobItemEntity> getEntityClass()
    {
        return ImageBulkJobItemEntity.class;
    }

    public List<ImageBulkJobItemEntity> listByJobId(UUID jobId)
    {
        return list("job.id", jobId);
    }

    public List<UUID> listPendingIdsByJobId(UUID jobId)
    {
        return getEntityManager().createQuery(
                        "SELECT i.id FROM ImageBulkJobItemEntity i WHERE i.job.id = :jobId AND i.status = :status ORDER BY i.relativePath",
                        UUID.class)
                .setParameter("jobId", jobId)
                .setParameter("status", ImageBulkJobItemStatusEn.PENDING)
                .getResultList();
    }

    public long countByJobIdAndStatus(UUID jobId, ImageBulkJobItemStatusEn status)
    {
        return count("job.id = ?1 and status = ?2", jobId, status);
    }

    public List<String> listErrorSamples(UUID jobId, int limit)
    {
        return getEntityManager().createQuery(
                        "SELECT i.error FROM ImageBulkJobItemEntity i WHERE i.job.id = :jobId AND i.status = :status AND i.error IS NOT NULL ORDER BY i.relativePath",
                        String.class)
                .setParameter("jobId", jobId)
                .setParameter("status", ImageBulkJobItemStatusEn.FAILED)
                .setMaxResults(limit)
                .getResultList();
    }
}
