package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.QuoteRequestStatusEn;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class QuoteRequestDetailsDto
{
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String message;
    private Instant createdAt;
    private QuoteRequestStatusEn status;
    private Instant statusChangedAt;
    private List<QuoteRequestItemDto> items;
}
