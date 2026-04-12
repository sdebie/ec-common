package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.VariantPricesEntity;
import org.ecommerce.common.enums.PriceTypeEn;

import java.util.UUID;

@ApplicationScoped
public class VariantPricesRepository extends BaseRepository<VariantPricesEntity, UUID>
{
    public VariantPricesEntity findLatestByVariantAndType(UUID variantId, PriceTypeEn priceType)
    {
        if (variantId == null || priceType == null) {
            return null;
        }
        return find("variant.id = ?1 and priceType = ?2 order by updatedAt desc", variantId, priceType).firstResult();
    }
}

