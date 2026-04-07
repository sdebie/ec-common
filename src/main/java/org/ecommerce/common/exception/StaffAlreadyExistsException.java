package org.ecommerce.common.exception;

public class StaffAlreadyExistsException extends RuntimeException
{
    public StaffAlreadyExistsException(String message)
    {
        super(message);
    }
}

