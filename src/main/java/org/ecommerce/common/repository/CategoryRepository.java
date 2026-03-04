package org.ecommerce.common.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.CategoryEntity;

@ApplicationScoped
public class CategoryRepository implements PanacheRepository<CategoryEntity>
{
}
