package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

/**
 * Admin DTO for a single product row in the admin product list table.
 * Used by the adminProductList GraphQL query.
 */
@Type
public class AdminProductListItemDto {

    public String id;
    public String name;
    public String slug;
    public String sku;
    public CategoryDto category;
    public String status;
    public String thumbnailUrl;
    public String retailPrice;
    public int stockCount;
    public String stockLevel;

    public AdminProductListItemDto() {
    }
}
