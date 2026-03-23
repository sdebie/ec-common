package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductEntity;

import java.util.UUID;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID> {

}
