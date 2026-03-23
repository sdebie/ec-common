package org.ecommerce.common.exception;

public class BrandAlreadyExistsException extends RuntimeException
{
    public BrandAlreadyExistsException(String message)
    {
        super(message);
    }
}
