package com.seeho.downloadcenter.base.exception;

/**
 * Exception for business rule violations that should not be retried.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
