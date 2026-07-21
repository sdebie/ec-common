package org.ecommerce.common.dto;

import org.ecommerce.common.enums.QuoteRequestStatusEn;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class QuoteRequestDetailsDto {

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public QuoteRequestStatusEn getStatus() { return status; }
    public void setStatus(QuoteRequestStatusEn status) { this.status = status; }

    public Instant getStatusChangedAt() { return statusChangedAt; }
    public void setStatusChangedAt(Instant statusChangedAt) { this.statusChangedAt = statusChangedAt; }

    public List<QuoteRequestItemDto> getItems() { return items; }
    public void setItems(List<QuoteRequestItemDto> items) { this.items = items; }
}
