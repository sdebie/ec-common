package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.QuoteRequestStatusEn;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class QuoteRequestListItemDto
{
    private UUID id;
    private String name;
    private String company;
    private int itemCount;
    private Instant createdAt;
    private QuoteRequestStatusEn status;
}
