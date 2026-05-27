package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country_settings")
public class CountrySettingsEntity extends PanacheEntityBase {

    @Id
    @Column(name = "country_code", length = 2)
    public String countryCode;

    @Column(name = "country_name", nullable = false)
    public String countryName;

    @Column(name = "currency_code", nullable = false, length = 3)
    public String currencyCode;

    @Column(name = "locale", nullable = false)
    public String locale;

    @Column(name = "decimal_places", nullable = false)
    public short decimalPlaces = 2;

    @Column(name = "is_default", nullable = false)
    public boolean isDefault;

    @Column(name = "is_active", nullable = false)
    public boolean isActive = true;
}

