package org.ecommerce.common.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.query.SortDirection;

@Setter
@Getter
public class SortRequest
{
    private String field;
    private SortDirection direction;

}