package org.ecommerce.common.repository;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.dto.ProductListItemDto;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PanacheQueryBuilder;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.SortRequest;
import org.ecommerce.common.query.enums.SortDirection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
	public ProductEntity findByIdWithCategoryAndBrand(UUID productId)
	{
		if (productId == null) return null;

		return find("select p from ProductEntity p " +
					"left join fetch p.category " +
					"left join fetch p.brand " +
					"where p.id = ?1", productId)
				.firstResult();
	}

	public List<ProductListItemDto> findAllProductListItems(PageRequest pageRequest, FilterRequest filterRequest)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> basePriceTypes = List.of(
				PriceTypeEn.RETAIL_PRICE,
				PriceTypeEn.WHOLESALE_PRICE);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.category " +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join v.variantPrices vp " +
				"where v.product = p " +
				"and vp.priceType in :priceTypes " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
				")";

		if (queryBuilder.hasQuery()) {
			query += " AND " + queryBuilder.query();
		}

		// Append a fully-qualified ORDER BY directly into HQL to avoid Hibernate's
		// "Ambiguous unqualified attribute reference 'name'" error that arises when
		// both ProductEntity (p) and the join-fetched CategoryEntity share a 'name' field.
		query += buildOrderByClause(filterRequest != null ? filterRequest.getSort() : null, "p");

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("priceTypes", basePriceTypes);
		params.put("now", now);
		if (queryBuilder.hasParams()) {
			params.putAll(queryBuilder.params());
		}

		return find(query, params)
				.page(queryBuilder.page(pageRequest)).list().stream()
				.map(this::toProductListItemDto)
				.toList();
	}

	public List<ProductListItemDto> findActiveSaleProductListItems(PageRequest pageRequest)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> salePriceTypes = List.of(
				PriceTypeEn.RETAIL_SALE_PRICE,
				PriceTypeEn.WHOLESALE_SALE_PRICE);

		return find(
				"select distinct p from ProductEntity p " +
						"left join fetch p.category " +
						"where p.id in (" +
						"select distinct v.product.id from ProductVariantEntity v " +
						"join v.variantPrices vp " +
						"where vp.priceType in ?1 " +
						"and (vp.priceStartDate is null or vp.priceStartDate <= ?2) " +
						"and (vp.priceEndDate is null or vp.priceEndDate >= ?2)" +
						")",
				Sort.by("p.name"),
				salePriceTypes,
				now)
				.page(Page.of(
						pageRequest != null ? pageRequest.getPageIndex() : 0,
						pageRequest != null ? pageRequest.getPageSize() : 10))
				.list()
				.stream()
				.map(this::toProductListItemDto)
				.toList();
	}

	/**
	 * Builds a fully-qualified HQL ORDER BY clause using the given entity alias,
	 * preventing Hibernate's "Ambiguous unqualified attribute reference" error when
	 * joined entities share field names (e.g. both ProductEntity and CategoryEntity have 'name').
	 * <p>
	 * Unqualified fields (no dot) are automatically prefixed with {@code alias}.
	 * Navigation paths (e.g. {@code category.name}) are prefixed as {@code alias.category.name}.
	 * Falls back to {@code alias.name ASC} when no sort requests are present.
	 */
	private String buildOrderByClause(List<SortRequest> sortRequests, String alias)
	{
		if (sortRequests == null || sortRequests.isEmpty()) {
			return " ORDER BY " + alias + ".name ASC";
		}

		List<String> parts = new ArrayList<>();
		for (SortRequest s : sortRequests) {
			if (s.getField() == null || s.getField().isBlank()) continue;
			// Always prefix with the root alias so Hibernate knows which entity to sort by
			String field = alias + "." + s.getField();
			String dir = s.getDirection() == SortDirection.DESC ? "DESC" : "ASC";
			parts.add(field + " " + dir);
		}

		return parts.isEmpty()
				? " ORDER BY " + alias + ".name ASC"
				: " ORDER BY " + String.join(", ", parts);
	}

	private ProductListItemDto toProductListItemDto(ProductEntity product)
	{
		return new ProductListItemDto(
				product.id == null ? null : product.id.toString(),
				product.name,
				product.description,
				null,
				null,
				null,
				null,
				Collections.emptyList(),
				Collections.emptyList(),
				product.category != null ? product.category.name : null);
	}
}

