package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
public class CategoryDto
{
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private CategoryDto parent;
    private String imageUrl;
}
