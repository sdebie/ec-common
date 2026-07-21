package org.ecommerce.common.dto;

import org.ecommerce.common.enums.QuoteRequestStatusEn;

import java.time.Instant;
import java.util.UUID;

public class QuoteRequestListItemDto {

    private UUID id;
    private String name;
    private String company;
    private int itemCount;
    private Instant createdAt;
    private QuoteRequestStatusEn status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public QuoteRequestStatusEn getStatus() { return status; }
    public void setStatus(QuoteRequestStatusEn status) { this.status = status; }
}
