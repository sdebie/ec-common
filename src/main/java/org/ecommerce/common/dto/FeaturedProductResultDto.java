package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

/**
 * Result DTO for the setProductFeatured mutation.
 * Returns the product ID and its updated featured state.
 */
@Type
public class FeaturedProductResultDto {

    public String productId;
    public boolean featured;

    public FeaturedProductResultDto() {
    }
}
