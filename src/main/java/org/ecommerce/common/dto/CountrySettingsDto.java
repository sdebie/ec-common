package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountrySettingsDto {
    private String countryCode;
    private String countryName;
    private String currencyCode;
    private String locale;
    private short decimalPlaces;
    private boolean isDefault;
    private boolean isActive;
}



