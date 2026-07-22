package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BrandDto
{
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private String logoUrl;

}
