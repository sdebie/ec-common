package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.BrandEntity;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class BrandRepository extends BaseRepository<BrandEntity, UUID>
{
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("id", "name", "slug", "logoUrl");

    @Override
    protected Class<BrandEntity> getEntityClass()
    {
        return BrandEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }

    public BrandEntity findBySlugIgnoreCase(String slug)
    {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return find("lower(slug) = ?1", slug.trim().toLowerCase()).firstResult();
    }

    public BrandEntity findByNameExcludingId(String name, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(name) = lower(?1)", name).firstResult();
        }
        return find("lower(name) = lower(?1) and id != ?2", name, excludeId).firstResult();
    }

    public BrandEntity findBySlugExcludingId(String slug, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(slug) = lower(?1)", slug).firstResult();
        }
        return find("lower(slug) = lower(?1) and id != ?2", slug, excludeId).firstResult();
    }
}