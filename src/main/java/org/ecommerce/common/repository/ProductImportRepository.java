package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductUploadBatchEntity;
import java.util.UUID;

@ApplicationScoped
public class ProductImportRepository extends BaseRepository<ProductUploadBatchEntity, UUID> {
}
