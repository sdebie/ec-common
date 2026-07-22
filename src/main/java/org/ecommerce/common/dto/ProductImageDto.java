package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Type;

/**
 * DTO representing a product image object (keeps object shape rather than plain URL strings).
 */
@Getter
@Setter
@Type
public class ProductImageDto
{
    @Description("Image ID (UUID as string)")
    private String id;

    @Description("Image URL")
    private String imageUrl;

    @Description("Sort order for display")
    private Integer sortOrder;

    @Description("Whether this image is featured")
    private boolean isFeatured;

    public ProductImageDto()
    {
    }

    public ProductImageDto(String id, String imageUrl, Integer sortOrder, boolean isFeatured)
    {
        this.id = id;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
        this.isFeatured = isFeatured;
    }
}
