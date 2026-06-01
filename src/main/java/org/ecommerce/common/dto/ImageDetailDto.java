package org.ecommerce.common.dto;

import org.eclipse.microprofile.graphql.Type;

import java.util.UUID;

@Type
public class ImageDetailDto {
    public UUID id;
    public String imageUrl;
    public Integer sortOrder;
}