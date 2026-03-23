package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductEntity;

@ApplicationScoped
public class ProductRepository implements BaseRepository<ProductEntity, UUID> {

}
