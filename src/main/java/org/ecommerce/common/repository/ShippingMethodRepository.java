package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ShippingMethodEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ShippingMethodRepository extends BaseRepository<ShippingMethodEntity, UUID>
{
    @Override
    protected Class<ShippingMethodEntity> getEntityClass()
    {
        return ShippingMethodEntity.class;
    }

    public List<ShippingMethodEntity> findAllActive()
    {
        return list("isActive", true);
    }

    public ShippingMethodEntity save(ShippingMethodEntity entity)
    {
        if (entity.getId() == null) {
            entity.persist();
            return entity;
        } else {
            return getEntityManager().merge(entity);
        }
    }
}
