package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.ecommerce.common.entity.CategoryEntity;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class CategoryDto
{
    public UUID id;
    public String name;
    public String slug;
    public String description;
    public CategoryEntity parent;
    public String imageUrl;
}
