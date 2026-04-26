package org.ecommerce.common.repository;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.ProductUploadBatchEntity;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductUploadBatchRepository extends BaseRepository<ProductUploadBatchEntity, UUID>
{
	public List<ProductUploadBatchEntity> listAllOrderByCreatedAtDesc()
	{
		return listAll(Sort.by("createdAt", Sort.Direction.Descending)
				.and("id", Sort.Direction.Descending));
	}
}

