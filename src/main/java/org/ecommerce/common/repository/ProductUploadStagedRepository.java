package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductUploadStagedEntity;
import org.ecommerce.common.enums.ProductImportValidationStatusEn;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductUploadStagedRepository extends BaseRepository<ProductUploadStagedEntity, UUID>
{
    public List<ProductUploadStagedEntity> findByBatchId(UUID batchId)
    {
        return list("batch.id = ?1", batchId);
    }

    public List<ProductUploadStagedEntity> findUnprocessedByBatchId(UUID batchId)
    {
        return list("batch.id = ?1 and processed = false", batchId);
    }

    public long countByBatchId(UUID batchId)
    {
        return count("batch.id", batchId);
    }

    public long countProcessedValidByBatchId(UUID batchId)
    {
        return count("batch.id = ?1 and processed = true and validationStatus = ?2", batchId, ProductImportValidationStatusEn.VALID);
    }

    public long countProcessedInvalidByBatchId(UUID batchId)
    {
        return count("batch.id = ?1 and processed = true and (validationStatus is null or validationStatus <> ?2)", batchId, ProductImportValidationStatusEn.VALID);
    }
}

