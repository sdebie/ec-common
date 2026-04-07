package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ShippingMethodEntity;
import org.ecommerce.common.entity.StoreSettingsEntity;

import java.util.List;

@ApplicationScoped
public class SettingsRepository extends BaseRepository<StoreSettingsEntity, String>{

    public List<StoreSettingsEntity> getAllStoreSettings() {
        return StoreSettingsEntity.listAll();
    }

    public List<ShippingMethodEntity> getAllShippingMethods() {
        return ShippingMethodEntity.listAll();
    }

    public void saveStoreSettings(StoreSettingsEntity entity) {
        getEntityManager().merge(entity);
    }

    public ShippingMethodEntity saveShippingMethod(ShippingMethodEntity entity) {
        if (entity.id == null) {
            entity.persist();
            return entity;
        } else {
            return getEntityManager().merge(entity);
        }
    }
}
