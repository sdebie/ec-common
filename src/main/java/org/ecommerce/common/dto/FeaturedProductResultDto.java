package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * Result DTO for the setProductFeatured mutation.
 * Returns the product ID and its updated featured state.
 */
@Getter
@Setter
@Type
public class FeaturedProductResultDto
{
    private String productId;
    private boolean featured;

    public FeaturedProductResultDto()
    {
    }
}
