package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import io.quarkus.panache.common.Page;
import org.ecommerce.common.dto.PageResponse;
import org.ecommerce.common.dto.ProductListItemDto;
import org.ecommerce.common.entity.ProductEntity;
import org.ecommerce.common.enums.OrderStatusEn;
import org.ecommerce.common.enums.PriceTypeEn;
import org.ecommerce.common.enums.ProductStatusEn;
import org.ecommerce.common.query.FilterRequest;
import org.ecommerce.common.query.Filter;
import org.ecommerce.common.query.FilterGroup;
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
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductRepository extends BaseRepository<ProductEntity, UUID>
{
	@Override
	protected Class<ProductEntity> getEntityClass()
	{
		return ProductEntity.class;
	}

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

	public List<ProductListItemDto> findAllProductListItems(PageRequest pageRequest, FilterRequest filterRequest, boolean ignoreStatus)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> basePriceTypes = List.of(
				PriceTypeEn.RETAIL_PRICE,
				PriceTypeEn.WHOLESALE_PRICE);
		FilterRequest normalizedFilterRequest = normalizeProductFilterRequest(filterRequest);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(normalizedFilterRequest, ProductEntity.class);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.categories " +
				"left join fetch p.brand " +
				(hasFiltersOnCategories(normalizedFilterRequest) ? "left join CategoryEntity category on category member of p.categories " : "") +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join VariantPricesEntity vp on vp.variant = v " +
				"where v.product = p " +
				(ignoreStatus ? "" : "and v.status = :variantStatus ") +
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
		query += buildOrderByClause(normalizedFilterRequest != null ? normalizedFilterRequest.getSort() : null, "p");

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("priceTypes", basePriceTypes);
		params.put("now", now);
		if (!ignoreStatus) {
			params.put("variantStatus", ProductStatusEn.ACTIVE);
		}
		if (queryBuilder.hasParams()) {
			params.putAll(queryBuilder.params());
		}

		return find(query, params)
				.page(queryBuilder.page(pageRequest)).list().stream()
				.map(this::toProductListItemDto)
				.toList();
	}

	 public List<ProductListItemDto> findProductListItemsByCategoryIds(PageRequest pageRequest, FilterRequest filterRequest, List<UUID> categoryIds, boolean ignoreStatus)
 {
	 if (categoryIds == null || categoryIds.isEmpty()) {
		 return Collections.emptyList();
	 }

	 LocalDateTime now = LocalDateTime.now();
	 List<PriceTypeEn> basePriceTypes = List.of(
			 PriceTypeEn.RETAIL_PRICE,
			 PriceTypeEn.WHOLESALE_PRICE);
	 FilterRequest normalizedFilterRequest = normalizeProductFilterRequest(filterRequest);
	 PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(normalizedFilterRequest, ProductEntity.class);

	 String query = "select distinct p from ProductEntity p " +
			 "left join fetch p.categories " +
			 "left join fetch p.brand " +
			 (hasFiltersOnCategories(normalizedFilterRequest) ? "left join CategoryEntity category on category member of p.categories " : "") +
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
	 			 (ignoreStatus ? "" : "and v.status = :variantStatus ") +
			 "and vp.priceType in :priceTypes " +
			 "and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
			 "and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
			 ")";

	 if (queryBuilder.hasQuery()) {
		 query += " AND " + queryBuilder.query();
	 }

	 query += buildOrderByClause(normalizedFilterRequest != null ? normalizedFilterRequest.getSort() : null, "p");

	 Map<String, Object> params = new LinkedHashMap<>();
	 params.put("categoryIds", categoryIds);
	 params.put("priceTypes", basePriceTypes);
	 params.put("now", now);
	 	 if (!ignoreStatus) {
	 	 	 params.put("variantStatus", ProductStatusEn.ACTIVE);
	 	 }
	 if (queryBuilder.hasParams()) {
		 params.putAll(queryBuilder.params());
	 }

	 return find(query, params)
			 .page(queryBuilder.page(pageRequest)).list().stream()
			 .map(this::toProductListItemDto)
			 .toList();
 }

	public long countShoppingProducts(FilterRequest filterRequest, boolean onSale, boolean ignoreStatus)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> shoppingPriceTypes = onSale
				? List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
				: List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
		FilterRequest normalizedFilterRequest = normalizeProductFilterRequest(filterRequest);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(normalizedFilterRequest, ProductEntity.class);

		String hql = "select count(distinct p) from ProductEntity p " +
				(hasFiltersOnCategories(normalizedFilterRequest) ? "left join CategoryEntity category on category member of p.categories " : "") +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join VariantPricesEntity vp on vp.variant = v " +
				"where v.product = p " +
				(ignoreStatus ? "" : "and v.status = :variantStatus ") +
				"and vp.priceType in :priceTypes " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
				")";

		if (queryBuilder.hasQuery()) {
			hql += " AND " + queryBuilder.query();
		}

		TypedQuery<Long> q = getEntityManager().createQuery(hql, Long.class);
		q.setParameter("priceTypes", shoppingPriceTypes);
		q.setParameter("now", now);
		if (!ignoreStatus) {
			q.setParameter("variantStatus", ProductStatusEn.ACTIVE);
		}
		if (queryBuilder.hasParams()) {
			for (Map.Entry<String, Object> entry : queryBuilder.params().entrySet()) {
				q.setParameter(entry.getKey(), entry.getValue());
			}
		}

		Long result = q.getSingleResult();
		return result != null ? result : 0L;
	}

	public List<ProductEntity> findShoppingProductEntities(PageRequest pageRequest, FilterRequest filterRequest, boolean onSale, boolean ignoreStatus)
	{
		LocalDateTime now = LocalDateTime.now();
		List<PriceTypeEn> shoppingPriceTypes = onSale
				? List.of(PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE)
				: List.of(PriceTypeEn.RETAIL_PRICE, PriceTypeEn.WHOLESALE_PRICE, PriceTypeEn.RETAIL_SALE_PRICE, PriceTypeEn.WHOLESALE_SALE_PRICE);
		FilterRequest normalizedFilterRequest = normalizeProductFilterRequest(filterRequest);
		PanacheQueryBuilder queryBuilder = PanacheQueryBuilder.from(normalizedFilterRequest, ProductEntity.class);

		String query = "select distinct p from ProductEntity p " +
				"left join fetch p.categories " +
				"left join fetch p.brand " +
				(hasFiltersOnCategories(normalizedFilterRequest) ? "left join CategoryEntity category on category member of p.categories " : "") +
				"where exists (" +
				"select 1 from ProductVariantEntity v " +
				"join VariantPricesEntity vp on vp.variant = v " +
				"where v.product = p " +
				(ignoreStatus ? "" : "and v.status = :variantStatus ") +
				"and vp.priceType in :priceTypes " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
				")";

		if (queryBuilder.hasQuery()) {
			query += " AND " + queryBuilder.query();
		}

		query += buildOrderByClause(normalizedFilterRequest != null ? normalizedFilterRequest.getSort() : null, "p");

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("priceTypes", shoppingPriceTypes);
		params.put("now", now);
		if (!ignoreStatus) {
			params.put("variantStatus", ProductStatusEn.ACTIVE);
		}
		if (queryBuilder.hasParams()) {
			params.putAll(queryBuilder.params());
		}

		return find(query, params)
				.page(queryBuilder.page(pageRequest)).list();
	}

	public List<ProductEntity> findOnSaleProductEntities(PageRequest pageRequest, boolean ignoreStatus)
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
				(ignoreStatus ? "" : "and v.status = :variantStatus ") +
				"and vp.priceType in :priceTypes " +
				"and (vp.priceStartDate is null or vp.priceStartDate <= :now) " +
				"and (vp.priceEndDate is null or vp.priceEndDate >= :now)" +
				") " +
				"order by p.name asc";

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("priceTypes", salePriceTypes);
		params.put("now", now);
		if (!ignoreStatus) {
			params.put("variantStatus", ProductStatusEn.ACTIVE);
		}

		return find(query, params)
				.page(Page.of(
						pageRequest != null ? pageRequest.getPageIndex() : 0,
						pageRequest != null ? pageRequest.getPageSize() : 10))
				.list();
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
				if (f.getKey() != null && (f.getKey().startsWith("category") || f.getKey().startsWith("categories"))) {
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
				if (f.getKey() != null && (f.getKey().startsWith("category") || f.getKey().startsWith("categories"))) {
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

	private FilterRequest normalizeProductFilterRequest(FilterRequest filterRequest)
	{
		FilterRequest normalized = new FilterRequest();
		if (filterRequest == null) {
			return normalized;
		}

		normalized.setSort(filterRequest.getSort());
		normalized.setFilters(copyAndNormalizeFilters(filterRequest.getFilters()));
		normalized.setFilterGroups(copyAndNormalizeGroups(filterRequest.getFilterGroups()));
		return normalized;
	}

	private List<Filter> copyAndNormalizeFilters(List<Filter> filters)
	{
		if (filters == null || filters.isEmpty()) {
			return filters;
		}

		List<Filter> normalized = new ArrayList<>(filters.size());
		for (Filter original : filters) {
			if (original == null) {
				continue;
			}

			Filter copy = new Filter();
			copy.setKey(normalizeProductFilterKey(original.getKey()));
			copy.setOperator(original.getOperator());
			copy.setValue(original.getValue());
			copy.setValues(original.getValues() == null ? null : new ArrayList<>(original.getValues()));
			normalized.add(copy);
		}
		return normalized;
	}

	private List<FilterGroup> copyAndNormalizeGroups(List<FilterGroup> groups)
	{
		if (groups == null || groups.isEmpty()) {
			return groups;
		}

		List<FilterGroup> normalized = new ArrayList<>(groups.size());
		for (FilterGroup group : groups) {
			if (group == null) {
				continue;
			}

			FilterGroup copy = new FilterGroup();
			copy.setOperator(group.getOperator());
			copy.setFilters(copyAndNormalizeFilters(group.getFilters()));
			copy.setFilterGroups(copyAndNormalizeGroups(group.getFilterGroups()));
			normalized.add(copy);
		}
		return normalized;
	}

	private String normalizeProductFilterKey(String key)
	{
		if (key == null || key.isBlank()) {
			return key;
		}

		if (key.startsWith("p.") || key.startsWith("category.")) {
			return key;
		}

		if (key.startsWith("categories.")) {
			return "category." + key.substring("categories.".length());
		}

		if (key.contains(".")) {
			return "p." + key;
		}

		return "p." + key;
	}

	private ProductListItemDto toProductListItemDto(ProductEntity product)
	{
		List<String> categoryNames = new ArrayList<>();

		if (product.categories != null && !product.categories.isEmpty()) {
			categoryNames = product.categories.stream()
					.map(c -> c.name)
					.toList();
		}

		ProductListItemDto dto = new ProductListItemDto(
				product.id == null ? null : product.id.toString(),
				product.name,
				product.description,
				null,
				Collections.emptyList(),
				categoryNames,
				product.brand != null ? product.brand.name : null);
		dto.status = product.status == null ? null : product.status.name();
		return dto;
	}

	// ─── Best Sellers ──────────────────────────────────────────────────────────

	/**
	 * Returns the top 10 best-selling products based on total quantity sold in
	 * DELIVERED orders. If fewer than 10 exist, the remainder is filled with
	 * random products so the response always contains up to 10 entries.
	 */
	public List<ProductEntity> findTopBestSellerEntities()
	{
		final int TARGET = 10;

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

		return result;
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

	/**
	 * Paged admin product list (entities), with optional status/category/brand/search
	 * filters. Returns the page of entities plus pagination metadata; the caller maps
	 * each entity to its DTO.
	 */
	public PageResponse<ProductEntity> findAdminProductPage(int pageIndex, int pageSize, String status, String categoryId, String brandId, String search)
	{
		int effectivePageSize = Math.min(Math.max(pageSize, 1), 100);
		int effectivePageIndex = Math.max(pageIndex, 0);

		StringBuilder whereClause = new StringBuilder("WHERE 1=1");
		Map<String, Object> params = new LinkedHashMap<>();

		if (status != null && !status.isBlank()) {
			whereClause.append(" AND p.status = :status");
			params.put("status", ProductStatusEn.valueOf(status));
		}
		if (categoryId != null && !categoryId.isBlank()) {
			whereClause.append(" AND EXISTS (SELECT 1 FROM p.categories c WHERE c.id = :categoryId)");
			params.put("categoryId", UUID.fromString(categoryId));
		}
		if (brandId != null && !brandId.isBlank()) {
			whereClause.append(" AND p.brand.id = :brandId");
			params.put("brandId", UUID.fromString(brandId));
		}
		if (search != null && !search.isBlank()) {
			String searchPattern = "%" + search.trim().toLowerCase() + "%";
			whereClause.append(" AND (LOWER(p.name) LIKE :search")
					.append(" OR EXISTS (SELECT 1 FROM ProductVariantEntity sv WHERE sv.product = p AND LOWER(sv.sku) LIKE :search)")
					.append(")");
			params.put("search", searchPattern);
		}

		String countHql = "SELECT COUNT(p) FROM ProductEntity p " + whereClause;
		TypedQuery<Long> countQuery = getEntityManager().createQuery(countHql, Long.class);
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			countQuery.setParameter(entry.getKey(), entry.getValue());
		}
		long totalElements = countQuery.getSingleResult();
		int totalPages = effectivePageSize > 0
				? (int) Math.ceil((double) totalElements / effectivePageSize)
				: 0;
		// A deletion or filter change can make a previously valid client page fall
		// outside the result set between requests. Return the final available page
		// instead of an avoidable empty page; callers can render response metadata
		// without an effect-driven pagination correction.
		if (totalPages > 0) {
			effectivePageIndex = Math.min(effectivePageIndex, totalPages - 1);
		}

		String fetchHql = "SELECT DISTINCT p FROM ProductEntity p " +
				"LEFT JOIN FETCH p.categories " +
				"LEFT JOIN FETCH p.brand " +
				whereClause +
				" ORDER BY p.name ASC";
		TypedQuery<ProductEntity> fetchQuery = getEntityManager().createQuery(fetchHql, ProductEntity.class);
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			fetchQuery.setParameter(entry.getKey(), entry.getValue());
		}
		fetchQuery.setFirstResult(effectivePageIndex * effectivePageSize);
		fetchQuery.setMaxResults(effectivePageSize);

		List<ProductEntity> products = fetchQuery.getResultList();

		return new PageResponse<>(products, totalElements, totalPages, effectivePageIndex, effectivePageSize);
	}

}
