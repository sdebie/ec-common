package org.ecommerce.common.repository;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductPriceImportBatchEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductPriceImportBatchRepository extends BaseRepository<ProductPriceImportBatchEntity, UUID>
{
    @Override
    protected Class<ProductPriceImportBatchEntity> getEntityClass()
    {
        return ProductPriceImportBatchEntity.class;
    }

    public List<ProductPriceImportBatchEntity> listAllOrderByCreatedAtDesc()
    {
        return listAll(Sort.by("createdAt", Sort.Direction.Descending)
                .and("id", Sort.Direction.Descending));
    }
}

