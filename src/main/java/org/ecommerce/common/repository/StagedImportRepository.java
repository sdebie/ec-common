package org.ecommerce.common.repository;

import io.quarkus.panache.common.Page;

import java.util.List;
import java.util.UUID;

/**
 * Shared "rows staged under one import batch" queries — identical across every staged-import
 * entity, since each just names its batch relation the same way. One implementation so a new
 * staged-import type never has to copy these three methods again.
 */
public abstract class StagedImportRepository<T> extends BaseRepository<T, UUID>
{
    public List<T> findByBatchId(UUID batchId)
    {
        return list("batch.id = ?1", batchId);
    }

    public List<T> findNextUnprocessedByBatchId(UUID batchId, int limit)
    {
        if (limit <= 0) {
            return List.of();
        }

        return find("batch.id = ?1 and processed = false order by id asc", batchId)
                .page(Page.ofSize(limit))
                .list();
    }

    public long countByBatchId(UUID batchId)
    {
        return count("batch.id", batchId);
    }
}
