package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product list DTO for sales/specials that includes the full variant payload.
 */
@Type
public class SaleProductListItemDto {
    @Description("Product ID (UUID as string)")
    public String id;

    @Description("Product name")
    public String name;

    @Description("Short description")
    public String description;

    @Description("Category Name")
    public String categoryName;

    @Description("Starting active retail price for the product")
    public BigDecimal retailPrice;

    @Description("Starting active retail sale price for the product")
    public BigDecimal retailSalesPrice;

    @Description("Starting active wholesale price for the product")
    public BigDecimal wholesalePrice;

    @Description("Starting active wholesale sale price for the product")
    public BigDecimal wholesaleSalesPrice;

    @Description("Start date/time for the active sale price window")
    public LocalDateTime price_start_date;

    @Description("End date/time for the active sale price window")
    public LocalDateTime price_end_date;

    @Description("Product images (structured objects)")
    public List<ProductImageDto> productImages;

    @Description("All variants for this product")
    public List<ProductVariantDto> variants;

    public SaleProductListItemDto() {}

    public SaleProductListItemDto(String id,
                                  String name,
                                  String description,
                                  BigDecimal retailPrice,
                                  BigDecimal retailSalesPrice,
                                  BigDecimal wholesalePrice,
                                  BigDecimal wholesaleSalesPrice,
                                  LocalDateTime price_start_date,
                                  LocalDateTime price_end_date,
                                  List<ProductImageDto> productImages,
                                  List<ProductVariantDto> variants,
                                  String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.retailPrice = retailPrice;
        this.retailSalesPrice = retailSalesPrice;
        this.wholesalePrice = wholesalePrice;
        this.wholesaleSalesPrice = wholesaleSalesPrice;
        this.price_start_date = price_start_date;
        this.price_end_date = price_end_date;
        this.productImages = productImages;
        this.variants = variants;
        this.categoryName = categoryName;
    }
}

