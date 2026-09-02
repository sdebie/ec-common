package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductImportStagedEntity;

@ApplicationScoped
public class ProductImportStagedRepository extends StagedImportRepository<ProductImportStagedEntity>
{
    @Override
    protected Class<ProductImportStagedEntity> getEntityClass()
    {
        return ProductImportStagedEntity.class;
    }
}
