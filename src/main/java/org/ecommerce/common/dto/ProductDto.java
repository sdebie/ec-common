package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO that represents the full product information available for product detail views.
 */
@Getter
@Setter
@Type
public class ProductDto
{
    @Description("Product ID (UUID as string)")
    private String id;

    @Description("Product slug")
    private String slug;

    @Description("Product name")
    private String name;

    @Description("Product description")
    private String description;

    @Description("Product short description")
    private String shortDescription;

    @Description("Product type")
    private String productType;

    @Description("Product status")
    private String status;

    @Description("Product created date/time")
    private String createdAt;

    @Description("Primary/first category (for backward compatibility)")
    private CategoryDto category;

    @Description("All categories this product belongs to")
    private List<CategoryDto> categories = new ArrayList<>();

    @Description("Brand")
    private BrandDto brand;

    @Description("Product variants")
    private List<ProductVariantDto> variants = new ArrayList<>();

    public ProductDto()
    {
    }
}

