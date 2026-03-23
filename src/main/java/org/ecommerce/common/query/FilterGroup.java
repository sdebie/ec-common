package org.ecommerce.common.query;

import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.query.enums.LogicalOperator;

import java.util.List;

@Getter
@Setter
public class FilterGroup
{
    private LogicalOperator operator = LogicalOperator.AND;
    private List<Filter> filters;
    private List<FilterGroup> filterGroups;

    public boolean isEmpty()
    {
        return (filters == null || filters.isEmpty()) && (filterGroups == null || filterGroups.isEmpty());
    }
}
