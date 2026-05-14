package org.ecommerce.common.dto;

import java.util.UUID;

public class WholesaleCustomerDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String physicalAddressLine1;
    private String physicalAddressLine2;
    private String physicalSuburb;
    private String physicalCity;
    private String physicalProvince;
    private String physicalPostalCode;
    private String postalAddressLine1;
    private String postalAddressLine2;
    private String postalSuburb;
    private String postalCity;
    private String postalProvince;
    private String postalPostalCode;
    private String additionalInfo;
    private String status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhysicalAddressLine1() { return physicalAddressLine1; }
    public void setPhysicalAddressLine1(String physicalAddressLine1) { this.physicalAddressLine1 = physicalAddressLine1; }

    public String getPhysicalAddressLine2() { return physicalAddressLine2; }
    public void setPhysicalAddressLine2(String physicalAddressLine2) { this.physicalAddressLine2 = physicalAddressLine2; }

    public String getPhysicalSuburb() { return physicalSuburb; }
    public void setPhysicalSuburb(String physicalSuburb) { this.physicalSuburb = physicalSuburb; }

    public String getPhysicalCity() { return physicalCity; }
    public void setPhysicalCity(String physicalCity) { this.physicalCity = physicalCity; }

    public String getPhysicalProvince() { return physicalProvince; }
    public void setPhysicalProvince(String physicalProvince) { this.physicalProvince = physicalProvince; }

    public String getPhysicalPostalCode() { return physicalPostalCode; }
    public void setPhysicalPostalCode(String physicalPostalCode) { this.physicalPostalCode = physicalPostalCode; }

    public String getPostalAddressLine1() { return postalAddressLine1; }
    public void setPostalAddressLine1(String postalAddressLine1) { this.postalAddressLine1 = postalAddressLine1; }

    public String getPostalAddressLine2() { return postalAddressLine2; }
    public void setPostalAddressLine2(String postalAddressLine2) { this.postalAddressLine2 = postalAddressLine2; }

    public String getPostalSuburb() { return postalSuburb; }
    public void setPostalSuburb(String postalSuburb) { this.postalSuburb = postalSuburb; }

    public String getPostalCity() { return postalCity; }
    public void setPostalCity(String postalCity) { this.postalCity = postalCity; }

    public String getPostalProvince() { return postalProvince; }
    public void setPostalProvince(String postalProvince) { this.postalProvince = postalProvince; }

    public String getPostalPostalCode() { return postalPostalCode; }
    public void setPostalPostalCode(String postalPostalCode) { this.postalPostalCode = postalPostalCode; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

