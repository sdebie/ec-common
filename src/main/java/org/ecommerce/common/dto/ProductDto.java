package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

/**
 * DTO that represents the full product information available for product detail views.
 */
@Type
public class ProductDto {
    @Description("Product ID (UUID as string)")
    public String id;

    @Description("Product slug")
    public String slug;

    @Description("Product name")
    public String name;

    @Description("Product description")
    public String description;

    @Description("Product short description")
    public String shortDescription;

    @Description("Product type")
    public String productType;

    @Description("Product created date/time")
    public String createdAt;

    @Description("Category ID (UUID as string)")
    public String categoryId;

    @Description("Brand ID (UUID as string)")
    public String brandId;

    public ProductDto() {}

    public ProductDto(String id,
                      String slug,
                      String name,
                      String description,
                      String shortDescription,
                      String productType,
                      String createdAt,
                      String categoryId,
                      String brandId) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.shortDescription = shortDescription;
        this.productType = productType;
        this.createdAt = createdAt;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }
}

