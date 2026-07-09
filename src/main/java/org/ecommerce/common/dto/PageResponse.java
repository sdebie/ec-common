package org.ecommerce.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Generic paginated response wrapper used by GraphQL queries that the
 * storefront frontend consumes. SmallRye GraphQL resolves the concrete
 * type parameter at compile time, generating a distinct GraphQL type for
 * each instantiation (e.g. PageResponseCategoryDto, PageResponseBrandDto).
 */
@Getter
@AllArgsConstructor
public class PageResponse<T>
{
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageIndex;
    private int pageSize;
}
