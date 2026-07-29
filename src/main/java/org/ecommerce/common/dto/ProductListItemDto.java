package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal DTO for listing products on the storefront.
 */
@Getter
@Setter
@Type
public class ProductListItemDto
{
    @Description("Product ID (UUID as string)")
    private String id;

    @Description("Product name")
    private String name;

    @Description("Short description")
    private String description;

    @Description("All category names this product belongs to")
    private List<String> categoryNames = new ArrayList<>();

    @Description("Brand Name")
    private String brandName;

    @Description("Featured variant image URL")
    private String imageName;

    @Description("All variant IDs for this product")
    private List<String> variantIds;

    @Description("Product status")
    private String status;

    public ProductListItemDto()
    {
    }

    public ProductListItemDto(String id, String name, String description,
                              String imageName,
                              List<String> variantIds, List<String> categoryNames,
                              String brandName)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
        this.variantIds = variantIds;
        this.categoryNames = categoryNames != null ? new ArrayList<>(categoryNames) : new ArrayList<>();
        this.brandName = brandName;
    }
}