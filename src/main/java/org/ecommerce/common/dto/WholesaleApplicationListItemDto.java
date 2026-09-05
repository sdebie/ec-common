package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.WholesaleApplicationStatusEn;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public class WholesaleApplicationListItemDto
{
    private UUID id;
    private Instant createdAt;
    private WholesaleApplicationStatusEn status;
    private String email;
    private String firstName;
    private String lastName;
}

