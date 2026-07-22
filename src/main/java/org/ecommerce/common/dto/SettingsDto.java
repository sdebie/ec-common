package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SettingsDto
{
    private List<StoreSettingsDto> storeSettings;
    private List<ShippingMethodDto> shippingMethods;
    private List<CountrySettingsDto> countrySettings;
}
