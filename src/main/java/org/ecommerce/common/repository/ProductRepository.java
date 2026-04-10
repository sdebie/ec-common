package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import org.ecommerce.common.dto.ProductListItemDto;
import org.ecommerce.common.dto.ProductImageDto;
import org.ecommerce.common.dto.ProductShoppingListItemDto;
import org.ecommerce.common.dto.VariantPriceDto;
import org.ecommerce.common.entity.ProductImageEntity;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.entity.VariantPricesEntity;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.PanacheQueryBuilder;
import org.ecommerce.common.query.PageRequest;
import org.ecommerce.common.query.SortRequest;
import org.ecommerce.common.query.enums.SortDirection;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
				"left join fetch p.brand " +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join VariantPricesEntity vp on vp.variant = v " +
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

	public List<ProductShoppingListItemDto> findShoppingProductList(PageRequest pageRequest, FilterRequest filterRequest)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> shoppingPriceTypes = List.of(
				PriceTypeEn.RETAIL_PRICE,
				PriceTypeEn.WHOLESALE_PRICE,
				PriceTypeEn.RETAIL_SALE_PRICE,
				PriceTypeEn.WHOLESALE_SALE_PRICE);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.category " +
				"left join fetch p.brand " +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join VariantPricesEntity vp on vp.variant = v " +
				"where v.product = p " +
				"and vp.priceType in :priceTypes " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
				")";

		if (queryBuilder.hasQuery()) {
			query += " AND " + queryBuilder.query();
		}

		query += buildOrderByClause(filterRequest != null ? filterRequest.getSort() : null, "p");

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("priceTypes", shoppingPriceTypes);
		params.put("now", now);
		if (queryBuilder.hasParams()) {
			params.putAll(queryBuilder.params());
		}

		return find(query, params)
				.page(queryBuilder.page(pageRequest)).list().stream()
				.map(product -> toShoppingListItemDto(product, now))
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
				Collections.emptyList(),
				product.category != null ? product.category.name : null,
				product.brand != null ? product.brand.name : null);
	}

	private ProductShoppingListItemDto toShoppingListItemDto(ProductEntity product, LocalDateTime now)
	{
		ProductShoppingListItemDto dto = new ProductShoppingListItemDto();
		dto.id = product.id == null ? null : product.id.toString();
		dto.name = product.name;
		dto.shortDescription = product.shorDescription;
		dto.variantCount = product.id == null ? 0 : countVariants(product.id);
		dto.images = product.id == null ? List.of() : findProductImages(product.id);
		dto.retailPrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.RETAIL_PRICE, now);
		dto.wholesalePrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.WHOLESALE_PRICE, now);
		dto.retailSalePrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.RETAIL_SALE_PRICE, now);
		dto.wholesaleSalePrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.WHOLESALE_SALE_PRICE, now);
		return dto;
	}

	private Integer countVariants(UUID productId)
	{
		Long count = getEntityManager()
				.createQuery("select count(v.id) from ProductVariantEntity v where v.product.id = :productId", Long.class)
				.setParameter("productId", productId)
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	private List<ProductImageDto> findProductImages(UUID productId)
	{
		return getEntityManager().createQuery(
				"select pi from ProductImageEntity pi " +
				"where pi.productVariant.product.id = :productId " +
				"order by case when pi.isFeatured = true then 0 else 1 end asc, pi.sortOrder asc, pi.id asc",
				ProductImageEntity.class)
				.setParameter("productId", productId)
				.getResultList()
				.stream()
				.map(this::toProductImageDto)
				.toList();
	}

	private ProductImageDto toProductImageDto(ProductImageEntity image)
	{
		return new ProductImageDto(
				image.id == null ? null : image.id.toString(),
				image.imageUrl,
				image.sortOrder,
				Boolean.TRUE.equals(image.isFeatured));
	}

	private VariantPriceDto findLowestActivePrice(UUID productId, PriceTypeEn priceType, LocalDateTime now)
	{
		LocalDateTime veryOldDate = LocalDateTime.of(1970, 1, 1, 0, 0);

		TypedQuery<VariantPricesEntity> query = getEntityManager().createQuery(
				"select vp from VariantPricesEntity vp " +
				"join vp.variant v " +
				"where v.product.id = :productId " +
				"and vp.priceType = :priceType " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now) " +
				"order by vp.price asc, coalesce(vp.priceStartDate, :veryOldDate) asc, vp.createdAt asc",
				VariantPricesEntity.class);

		List<VariantPricesEntity> prices = query
				.setParameter("productId", productId)
				.setParameter("priceType", priceType)
				.setParameter("now", now)
				.setParameter("veryOldDate", veryOldDate)
				.setMaxResults(1)
				.getResultList();

		if (prices.isEmpty()) {
			return null;
		}

		VariantPricesEntity price = prices.get(0);
		VariantPriceDto dto = new VariantPriceDto();
		dto.id = price.id == null ? null : price.id.toString();
		dto.priceType = price.priceType == null ? null : price.priceType.name();
		dto.price = price.price;
		dto.priceStartDate = price.priceStartDate;
		dto.priceEndDate = price.priceEndDate;
		dto.isActive = Boolean.TRUE;
		dto.saleDaysRemaining = calculateSaleDaysRemaining(price.priceType, price.priceEndDate, now);
		return dto;
	}

	private Long calculateSaleDaysRemaining(PriceTypeEn priceType, LocalDateTime endDate, LocalDateTime now)
	{
		if (priceType == null || endDate == null) {
			return null;
		}

		if (priceType != PriceTypeEn.RETAIL_SALE_PRICE && priceType != PriceTypeEn.WHOLESALE_SALE_PRICE) {
			return null;
		}

		long daysRemaining = ChronoUnit.DAYS.between(now.toLocalDate(), endDate.toLocalDate());
		return Math.max(daysRemaining, 0L);
	}


}
