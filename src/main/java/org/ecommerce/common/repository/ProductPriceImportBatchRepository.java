package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductPriceImportBatchEntity;

import java.util.UUID;

@ApplicationScoped
public class ProductPriceImportBatchRepository extends BaseRepository<ProductPriceImportBatchEntity, UUID>
{
    @Override
    protected Class<ProductPriceImportBatchEntity> getEntityClass()
    {
        return ProductPriceImportBatchEntity.class;
    }
}

