package com.seeho.downloadcenter.base.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑错误，不应重试
 *
 * @author Leonpo
 * @since 2025-12-02
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

