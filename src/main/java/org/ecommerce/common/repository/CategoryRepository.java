package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.CategoryEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class CategoryRepository extends BaseRepository<CategoryEntity, UUID>
{
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("id", "name", "slug", "description", "imageUrl", "parent.id", "parent.name", "parent.slug");

    @Override
    protected Class<CategoryEntity> getEntityClass()
    {
        return CategoryEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }

    public CategoryEntity findBySlugIgnoreCase(String slug)
    {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return find("lower(slug) = ?1", slug.trim().toLowerCase()).firstResult();
    }

    public CategoryEntity findByNameExcludingId(String name, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(name) = lower(?1)", name).firstResult();
        }
        return find("lower(name) = lower(?1) and id != ?2", name, excludeId).firstResult();
    }

    public CategoryEntity findBySlugExcludingId(String slug, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(slug) = lower(?1)", slug).firstResult();
        }

        return find("lower(slug) = lower(?1) and id != ?2", slug, excludeId).firstResult();
    }

    public long countByImageUrl(String imageUrl)
    {
        return count("imageUrl", imageUrl);
    }

    public List<CategoryEntity> findByParentId(UUID parentId)
    {
        return list("parent.id", parentId);
    }

    public long countByParentId(UUID parentId)
    {
        return count("parent.id = ?1", parentId);
    }

    /** Categories whose name still holds the literal HTML entity from bad legacy input — {@code fixCategoryNamesAmpersand}'s one-off correction. */
    public List<CategoryEntity> findWithAmpersandEntityInName()
    {
        return list("name like ?1", "%&amp;%");
    }
}
