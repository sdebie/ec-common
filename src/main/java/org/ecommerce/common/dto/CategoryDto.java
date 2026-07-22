package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDto
{
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private CategoryDto parent;
    private String imageUrl;
}
