package org.ecommerce.common.repository;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductImportBatchEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductImportBatchRepository extends BaseRepository<ProductImportBatchEntity, UUID>
{
    @Override
    protected Class<ProductImportBatchEntity> getEntityClass()
    {
        return ProductImportBatchEntity.class;
    }

	public List<ProductImportBatchEntity> listAllOrderByCreatedAtDesc()
	{
		return listAll(Sort.by("createdAt", Sort.Direction.Descending)
				.and("id", Sort.Direction.Descending));
	}
}

