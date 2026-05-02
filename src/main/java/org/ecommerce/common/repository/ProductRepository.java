package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import io.quarkus.panache.common.Page;
import org.ecommerce.common.dto.ProductListItemDto;
import org.ecommerce.common.dto.ProductImageDto;
import org.ecommerce.common.dto.ProductShoppingListItemDto;
import org.ecommerce.common.dto.VariantPriceDto;
import org.ecommerce.common.entity.ProductImageEntity;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.entity.VariantPricesEntity;
import org.ecommerce.common.enums.OrderStatusEn;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductTypeEn;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.Filter;
import org.ecommerce.common.query.FilterGroup;
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
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
	public ProductEntity findBySlugIgnoreCase(String slug)
	{
		if (slug == null || slug.isBlank()) {
			return null;
		}
		return find("lower(slug) = ?1", slug.trim().toLowerCase()).firstResult();
	}

	public ProductEntity findByNameIgnoreCase(String name)
	{
		if (name == null || name.isBlank()) {
			return null;
		}
		return find("lower(name) = ?1", name.trim().toLowerCase()).firstResult();
	}

	public ProductEntity findByIdWithCategoryAndBrand(UUID productId)
	{
		if (productId == null) return null;

		return find("select p from ProductEntity p " +
				"left join fetch p.categories " +
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
		rewriteCategoryJoinFilterKeys(filterRequest);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.categories " +
				"left join fetch p.brand " +
				(hasFiltersOnCategories(filterRequest) ? "left join CategoryEntity c on c member of p.categories " : "") +
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

	public List<ProductListItemDto> findProductListItemsByCategoryIds(PageRequest pageRequest, FilterRequest filterRequest, List<UUID> categoryIds)
	{
		if (categoryIds == null || categoryIds.isEmpty()) {
			return Collections.emptyList();
		}

		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> basePriceTypes = List.of(
				PriceTypeEn.RETAIL_PRICE,
				PriceTypeEn.WHOLESALE_PRICE);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.categories " +
				"left join fetch p.brand " +
				"where exists (" +
				"select 1 from ProductEntity scopedProduct " +
				"join scopedProduct.categories scopedCategory " +
				"where scopedProduct = p " +
				"and scopedCategory.id in :categoryIds" +
				") " +
				"and exists (" +
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
		params.put("categoryIds", categoryIds);
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
		rewriteCategoryJoinFilterKeys(filterRequest);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(filterRequest);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.categories " +
				"left join fetch p.brand " +
				(hasFiltersOnCategories(filterRequest) ? "left join CategoryEntity c on c member of p.categories " : "") +
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

	public List<ProductShoppingListItemDto> findOnSaleShoppingProductList(PageRequest pageRequest)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> salePriceTypes = List.of(
				PriceTypeEn.RETAIL_SALE_PRICE,
				PriceTypeEn.WHOLESALE_SALE_PRICE);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.categories " +
				"left join fetch p.brand " +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join VariantPricesEntity vp on vp.variant = v " +
				"where v.product = p " +
				"and vp.priceType in :priceTypes " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
				") " +
				"order by p.name asc";

		return find(query,
				Map.of("priceTypes", salePriceTypes, "now", now))
				.page(Page.of(
						pageRequest != null ? pageRequest.getPageIndex() : 0,
						pageRequest != null ? pageRequest.getPageSize() : 10))
				.list().stream()
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

	/**
	 * Check if the filter request contains any filters on category fields
	 */
	private boolean hasFiltersOnCategories(FilterRequest filterRequest)
	{
		if (filterRequest == null) return false;

		if (filterRequest.getFilters() != null) {
			for (Filter f : filterRequest.getFilters()) {
				if (isCategoryFilterKey(f.getKey())) {
					return true;
				}
			}
		}

		if (filterRequest.getFilterGroups() != null) {
			for (FilterGroup fg : filterRequest.getFilterGroups()) {
				if (hasFiltersOnCategoriesInGroup(fg)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean hasFiltersOnCategoriesInGroup(FilterGroup group)
	{
		if (group == null) return false;

		if (group.getFilters() != null) {
			for (Filter f : group.getFilters()) {
				if (isCategoryFilterKey(f.getKey())) {
					return true;
				}
			}
		}

		if (group.getFilterGroups() != null) {
			for (FilterGroup sub : group.getFilterGroups()) {
				if (hasFiltersOnCategoriesInGroup(sub)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isCategoryFilterKey(String key)
	{
		if (key == null || key.isBlank()) {
			return false;
		}
		return key.startsWith("category") || key.startsWith("categories") || key.startsWith("c.");
	}

	/**
	 * Product queries join assigned categories as {@code CategoryEntity c on c member of p.categories}.
	 * Filters must reference {@code c.*}, not {@code category.*} / {@code categories.*} — those paths
	 * are not valid on {@link ProductEntity} (field is {@code categories}) and trigger Hibernate
	 * {@code SemanticException}.
	 */
	private void rewriteCategoryJoinFilterKeys(FilterRequest filterRequest)
	{
		if (filterRequest == null) {
			return;
		}
		if (filterRequest.getFilters() != null) {
			for (Filter f : filterRequest.getFilters()) {
				if (f != null && f.getKey() != null) {
					f.setKey(aliasCategoryJoinFilterKey(f.getKey()));
				}
			}
		}
		if (filterRequest.getFilterGroups() != null) {
			for (FilterGroup fg : filterRequest.getFilterGroups()) {
				rewriteCategoryJoinFilterKeysInGroup(fg);
			}
		}
	}

	private void rewriteCategoryJoinFilterKeysInGroup(FilterGroup group)
	{
		if (group == null) {
			return;
		}
		if (group.getFilters() != null) {
			for (Filter f : group.getFilters()) {
				if (f != null && f.getKey() != null) {
					f.setKey(aliasCategoryJoinFilterKey(f.getKey()));
				}
			}
		}
		if (group.getFilterGroups() != null) {
			for (FilterGroup sub : group.getFilterGroups()) {
				rewriteCategoryJoinFilterKeysInGroup(sub);
			}
		}
	}

	private static String aliasCategoryJoinFilterKey(String key)
	{
		if (key.startsWith("category.")) {
			return "c." + key.substring("category.".length());
		}
		if (key.startsWith("categories.")) {
			return "c." + key.substring("categories.".length());
		}
		return key;
	}

	private ProductListItemDto toProductListItemDto(ProductEntity product)
	{
		List<String> categoryNames = new ArrayList<>();

		if (product.categories != null && !product.categories.isEmpty()) {
			categoryNames = product.categories.stream()
					.map(c -> c.name)
					.toList();
		}

		return new ProductListItemDto(
				product.id == null ? null : product.id.toString(),
				product.name,
				product.description,
				null,
				Collections.emptyList(),
				categoryNames,
				product.brand != null ? product.brand.name : null);
	}

	private ProductShoppingListItemDto toShoppingListItemDto(ProductEntity product, LocalDateTime now)
	{
		ProductShoppingListItemDto dto = new ProductShoppingListItemDto();
		dto.id = product.id == null ? null : product.id.toString();
		dto.name = product.name;
		dto.shortDescription = product.shorDescription;
		dto.productType = product.productType == null ? null : product.productType.name();
		dto.variantCount = product.id == null ? 0 : countVariants(product.id);
		dto.variantId = product.id == null || product.productType != ProductTypeEn.SIMPLE
				? null
				: findFirstVariantId(product.id);
		dto.images = product.id == null ? List.of() : findProductImages(product.id);
		dto.retailPrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.RETAIL_PRICE, now);
		dto.wholesalePrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.WHOLESALE_PRICE, now);
		dto.retailSalePrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.RETAIL_SALE_PRICE, now);
		dto.wholesaleSalePrice = product.id == null ? null : findLowestActivePrice(product.id, PriceTypeEn.WHOLESALE_SALE_PRICE, now);
		return dto;
	}

	private String findFirstVariantId(UUID productId)
	{
		List<UUID> variantIds = getEntityManager()
				.createQuery(
						"select v.id from ProductVariantEntity v where v.product.id = :productId order by v.id asc",
						UUID.class)
				.setParameter("productId", productId)
				.setMaxResults(1)
				.getResultList();

		return variantIds.isEmpty() ? null : variantIds.get(0).toString();
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

	// ─── Best Sellers ──────────────────────────────────────────────────────────

	/**
	 * Returns the top 10 best-selling products based on total quantity sold in
	 * DELIVERED orders. If fewer than 10 exist, the remainder is filled with
	 * random products so the response always contains up to 10 entries.
	 */
	public List<ProductShoppingListItemDto> findTopBestSellers()
	{
		final int TARGET = 10;
		LocalDateTime now = LocalDateTime.now();

		// Step 1 – collect best-seller product IDs ranked by units sold
		List<Object[]> rows = getEntityManager()
				.createQuery(
						"select oi.variant.product.id, sum(oi.quantity) as total " +
								"from OrderItemEntity oi " +
								"join oi.orderEntity o " +
								"where o.status = :status " +
								"and oi.variant is not null " +
								"group by oi.variant.product.id " +
								"order by total desc",
						Object[].class)
				.setParameter("status", OrderStatusEn.DELIVERED)
				.setMaxResults(TARGET)
				.getResultList();

		List<UUID> bestSellerIds = rows.stream()
				.map(row -> (UUID) row[0])
				.collect(Collectors.toList());

		// Step 2 – fetch full product entities (with category + brand) preserving rank order
		List<ProductEntity> bestSellers = fetchProductsByIds(bestSellerIds);

		// Step 3 – pad with random products when fewer than TARGET were found
		List<ProductEntity> result = new ArrayList<>(bestSellers);
		if (result.size() < TARGET) {
			int needed = TARGET - result.size();
			List<ProductEntity> random = findRandomProductEntitiesExcluding(needed, bestSellerIds);
			result.addAll(random);
		}

		return result.stream()
				.map(p -> toShoppingListItemDto(p, now))
				.collect(Collectors.toList());
	}

	/**
	 * Fetches ProductEntity records for the given IDs with category and brand
	 * eagerly loaded. The returned list preserves the order of the supplied IDs.
	 */
	private List<ProductEntity> fetchProductsByIds(List<UUID> ids)
	{
		if (ids == null || ids.isEmpty()) return Collections.emptyList();

		List<ProductEntity> unordered = getEntityManager()
				.createQuery(
						"select p from ProductEntity p " +
								"left join fetch p.categories " +
								"left join fetch p.brand " +
								"where p.id in :ids",
						ProductEntity.class)
				.setParameter("ids", ids)
				.getResultList();

		// Restore the ranked order returned by the aggregation query
		Map<UUID, ProductEntity> byId = unordered.stream()
				.collect(Collectors.toMap(p -> p.id, p -> p));

		return ids.stream()
				.map(byId::get)
				.filter(p -> p != null)
				.collect(Collectors.toList());
	}

	/**
	 * Returns up to {@code limit} random products excluding the given IDs.
	 */
	private List<ProductEntity> findRandomProductEntitiesExcluding(int limit, List<UUID> excludeIds)
	{
		TypedQuery<ProductEntity> q;
		if (excludeIds == null || excludeIds.isEmpty()) {
			q = getEntityManager().createQuery(
					"select p from ProductEntity p " +
							"left join fetch p.categories " +
							"left join fetch p.brand " +
							"order by function('random')",
					ProductEntity.class);
		} else {
			q = getEntityManager().createQuery(
							"select p from ProductEntity p " +
									"left join fetch p.categories " +
									"left join fetch p.brand " +
									"where p.id not in :excludeIds " +
									"order by function('random')",
							ProductEntity.class)
					.setParameter("excludeIds", excludeIds);
		}
		return q.setMaxResults(limit).getResultList();
	}

}
