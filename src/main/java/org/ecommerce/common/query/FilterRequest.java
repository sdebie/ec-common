package org.ecommerce.common.query;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Unified filter + sort descriptor passed alongside PageRequest. Flat {@code filters}
 * (implicitly AND-ed) and {@code groups} (nestable, explicit AND/OR) combine with AND
 * at the top level; {@code sort} is one or more columns with direction.
 */
@Getter
@Setter
public class FilterRequest
{
    private List<Filter> filters;
    private List<FilterGroup> filterGroups;
    private List<SortRequest> sort;
}
