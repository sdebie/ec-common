package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductPriceUploadBatchEntity;

import java.util.UUID;

@ApplicationScoped
public class ProductPriceUploadBatchRepository extends BaseRepository<ProductPriceUploadBatchEntity, UUID>
{
}

