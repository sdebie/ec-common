package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.TestimonialEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TestimonialRepository extends BaseRepository<TestimonialEntity, UUID>
{
    @Override
    protected Class<TestimonialEntity> getEntityClass()
    {
        return TestimonialEntity.class;
    }

    public List<TestimonialEntity> findPublished()
    {
        return list("isPublished = true ORDER BY sortOrder ASC");
    }

    public List<TestimonialEntity> findAllOrdered()
    {
        return list("ORDER BY sortOrder ASC, createdAt DESC");
    }
}
