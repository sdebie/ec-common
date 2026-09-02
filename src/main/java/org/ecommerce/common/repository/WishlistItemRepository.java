package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.WishlistItemEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WishlistItemRepository extends BaseRepository<WishlistItemEntity, UUID>
{
    @Override
    protected Class<WishlistItemEntity> getEntityClass()
    {
        return WishlistItemEntity.class;
    }

    public List<WishlistItemEntity> findByCustomerId(UUID customerId)
    {
        return list("customer.id", customerId);
    }

    public WishlistItemEntity findByCustomerAndVariant(UUID customerId, UUID variantId)
    {
        return find("customer.id = ?1 and variant.id = ?2", customerId, variantId).firstResult();
    }

    public long deleteByCustomerAndVariant(UUID customerId, UUID variantId)
    {
        return delete("customer.id = ?1 and variant.id = ?2", customerId, variantId);
    }
}
