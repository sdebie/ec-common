package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
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

    @Description("Starting price for the selected category")
    public BigDecimal retailPrice;

    @Description("Starting sale price for the selected category")
    public BigDecimal retailSalesPrice;

    @Description("Starting price for the selected category")
    public BigDecimal wholesalePrice;

    @Description("Starting sale price for the selected category")
    public BigDecimal wholesaleSalesPrice;

    @Description("Product images (structured objects)")
    public List<ProductImageDto> productImages;

    @Description("All variant IDs for this product")
    public List<String> variantIds;

    public ProductListItemDto() {}

    public ProductListItemDto(String id, String name, String description, BigDecimal retailPrice,
                             BigDecimal retailSalesPrice, BigDecimal wholesalePrice,
                              BigDecimal wholesaleSalesPrice, List<ProductImageDto> productImages,
                             List<String> variantIds, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.retailPrice = retailPrice;
        this.retailSalesPrice = retailSalesPrice;
        this.wholesalePrice = wholesalePrice;
        this.wholesaleSalesPrice = wholesaleSalesPrice;
        this.productImages = productImages;
        this.variantIds = variantIds;
        this.categoryName = categoryName;
    }
}