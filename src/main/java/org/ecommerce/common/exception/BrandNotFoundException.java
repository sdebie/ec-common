package org.ecommerce.common.exception;

public class BrandNotFoundException extends RuntimeException
{
    public BrandNotFoundException(String message)
    {
        super(message);
    }
}
