package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.BrandEntity;

import java.util.UUID;

@ApplicationScoped
public class BrandRepository extends BaseRepository<BrandEntity, UUID>
{
    public BrandEntity findBySlugIgnoreCase(String slug)
    {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return find("lower(slug) = ?1", slug.trim().toLowerCase()).firstResult();
    }

    /** Returns a brand with the given name that belongs to a different record than {@code excludeId}. */
    public BrandEntity findByNameExcludingId(String name, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(name) = lower(?1)", name).firstResult();
        }
        return find("lower(name) = lower(?1) and id != ?2", name, excludeId).firstResult();
    }

    /** Returns a brand with the given slug that belongs to a different record than {@code excludeId}. */
    public BrandEntity findBySlugExcludingId(String slug, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(slug) = lower(?1)", slug).firstResult();
        }
        return find("lower(slug) = lower(?1) and id != ?2", slug, excludeId).firstResult();
    }
}