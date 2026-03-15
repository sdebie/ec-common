package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.BrandEntity;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class BrandRepository extends BaseRepository<BrandEntity, UUID>
{
    private static final Map<String, String> ALLOWED_FIELDS = Map.of("id", "id", "name", "name", "slug", "slug", "createdAt", "createdAt");

    @Override
    protected Map<String, String> getAllowedFields()
    {
        return ALLOWED_FIELDS;
    }

    @Override
    protected String getDefaultSortField()
    {
        return "name";
    }
}