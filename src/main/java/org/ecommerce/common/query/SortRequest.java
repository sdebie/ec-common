package org.ecommerce.common.query;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.query.enums.SortDirection;

@Getter
@Setter
public class SortRequest
{
    private String field;
    private SortDirection direction = SortDirection.ASC;
}
