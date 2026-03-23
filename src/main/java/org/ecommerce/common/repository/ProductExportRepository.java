package org.ecommerce.common.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.ecommerce.common.entity.ProductImageEntity;

public class ProductExportRepository implements PanacheRepository<ProductImageEntity> {
}
