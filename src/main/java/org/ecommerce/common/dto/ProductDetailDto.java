package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

@Getter
@Setter
@Type
public class ProductDetailDto
{
    private String name;
}