package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Type
public class OrderItemDetailDto
{
    private String id;
    private BigDecimal unitPrice;
    private Integer quantity;
    private UUID variantId;
    private Integer stockQuantity;
    private String attributesJson;
    private BigDecimal weightKg;
    private String productName;
    private List<ProductImageDto> images;
}
