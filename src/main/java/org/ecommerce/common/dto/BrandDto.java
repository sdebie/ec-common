package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandDto
{
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String logoUrl;

}
