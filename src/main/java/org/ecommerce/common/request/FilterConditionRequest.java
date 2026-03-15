package org.ecommerce.common.request;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.FilterOperators;

@Setter
@Getter
public class FilterConditionRequest
{
    private String field;
    private FilterOperators operator;
    private String value;
}