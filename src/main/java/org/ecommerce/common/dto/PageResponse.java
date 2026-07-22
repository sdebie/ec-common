package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Generic paginated response wrapper used by GraphQL queries that the
 * storefront frontend consumes. SmallRye GraphQL resolves the concrete
 * type parameter at compile time, generating a distinct GraphQL type for
 * each instantiation (e.g. PageResponseCategoryDto, PageResponseBrandDto).
 */
@Getter
@Setter
public class PageResponse<T>
{
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageIndex;
    private int pageSize;

    public PageResponse(List<T> content, long totalElements, int totalPages, int pageIndex, int pageSize)
    {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}
