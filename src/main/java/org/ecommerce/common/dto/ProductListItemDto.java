package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

/**
 * Minimal DTO for listing products on the storefront.
 */
@Type
public class ProductListItemDto {
    @Description("Product ID (UUID as string)")
    public String id;

    @Description("Product name")
    public String name;

    @Description("Short description")
    public String description;

    @Description("Category Name")
    public String categoryName;

    @Description("Brand Name")
    public String brandName;

    @Description("Featured variant image URL")
    public String imageName;

    @Description("All variant IDs for this product")
    public List<String> variantIds;

    public ProductListItemDto() {}

    public ProductListItemDto(String id, String name, String description,
                              String imageName,
                              List<String> variantIds, String categoryName,
                              String brandName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
        this.variantIds = variantIds;
        this.categoryName = categoryName;
        this.brandName = brandName;
    }
}