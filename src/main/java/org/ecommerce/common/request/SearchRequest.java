package org.ecommerce.common.request;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.LogicalOperator;

import java.util.List;

@Setter
@Getter
public class SearchRequest
{
    private LogicalOperator operator;
    private List<FilterConditionRequest> conditions;
    private Integer pageIndex;
    private Integer pageSize;
    private List<SortRequest> sort;

}