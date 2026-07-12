package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.PageContentEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PageContentRepository extends BaseRepository<PageContentEntity, UUID>
{
    @Override
    protected Class<PageContentEntity> getEntityClass()
    {
        return PageContentEntity.class;
    }

    public PageContentEntity findBySlug(String slug)
    {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return find("slug", slug).firstResult();
    }

    public List<PageContentEntity> findByCategory(String category)
    {
        if (category == null || category.isBlank()) {
            return List.of();
        }
        return list("category", category);
    }
}
