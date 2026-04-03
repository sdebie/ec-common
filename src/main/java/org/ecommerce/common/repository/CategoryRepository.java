package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.CategoryEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoryRepository extends BaseRepository<CategoryEntity, UUID>
{
    // returns all the sub-categories
    public List<CategoryEntity> findSubCategoriesByParentId(Long parentId)
    {
        return list("parent.id", parentId);
    }

    public CategoryEntity findByNameExcludingId(String name, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(name) = lower(?1)", name).firstResult();
        }
        return find("lower(name) = lower(?1) and id != ?2", name, excludeId).firstResult();
    }

    public CategoryEntity findBySlugExcludingId(String slug, Object excludeId)
    {
        if (excludeId == null) {
            return find("lower(slug) = lower(?1)", slug).firstResult();
        }

        return find("lower(slug) = lower(?1) and id != ?2", slug, excludeId).firstResult();
    }
}
