package org.ecommerce.common.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest
{
    private int pageIndex = 0;
    private int pageSize = 10;

    public int getOffset()
    {
        return pageIndex * pageSize;
    }

    public void setPage(int pageIndex)
    {
        this.pageIndex = Math.max(0, pageIndex);
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = Math.min(Math.max(1, pageSize), 200);
    }
}
