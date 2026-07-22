package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for a single product row in the admin product list table.
 * Used by the adminProductList GraphQL query.
 */
@Getter
@Setter
@Type
public class AdminProductListItemDto
{

    private String id;
    private String name;
    private String slug;
    private String sku;
    private CategoryDto category;
    private String status;
    private String thumbnailUrl;
    private String retailPrice;
    private int stockCount;
    private String stockLevel;

    public AdminProductListItemDto()
    {
    }
}
