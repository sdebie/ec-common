package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.graphql.Type;

/**
 * One entry of the admin order status timeline, newest first.
 */
@Getter
@Setter
@Type
public class AdminOrderStatusHistoryDto
{
    private String status;
    private String timestamp;
    /** Staff member who made the change, or "SYSTEM" for an automated one. */
    private String staffName;
    private String comment;
}
