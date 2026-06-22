package org.ecommerce.common.dto;

import org.ecommerce.common.enums.WholesaleApplicationStatusEn;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WholesaleApplicationListItemDto {
    private UUID id;
    private OffsetDateTime createdAt;
    private WholesaleApplicationStatusEn status;
    private String email;
    private String firstName;
    private String lastName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public WholesaleApplicationStatusEn getStatus() {
        return status;
    }

    public void setStatus(WholesaleApplicationStatusEn status) {
        this.status = status;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}

