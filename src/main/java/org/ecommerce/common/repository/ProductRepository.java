package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.dto.ProductListItemDto;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
	public List<ProductListItemDto> findAllProductListItems(PageRequest pageRequest, FilterRequest filterRequest)
	{
		return findAll(pageRequest, filterRequest).stream()
				.map(product -> new ProductListItemDto(
						product.id == null ? null : product.id.toString(),
						product.name,
						product.description,
						null,
						null,
						null,
						null,
						Collections.emptyList(),
						Collections.emptyList(),
						product.category != null ? product.category.name : null))
				.toList();
	}
}

