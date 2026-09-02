package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductPriceImportStagedEntity;

@ApplicationScoped
public class ProductPriceImportStagedRepository extends StagedImportRepository<ProductPriceImportStagedEntity>
{
    @Override
    protected Class<ProductPriceImportStagedEntity> getEntityClass()
    {
        return ProductPriceImportStagedEntity.class;
    }
}
