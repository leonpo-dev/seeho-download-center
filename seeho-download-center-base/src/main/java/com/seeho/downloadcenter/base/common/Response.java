package com.seeho.downloadcenter.base.common;

import lombok.Data;

/**
 * Lightweight API response wrapper.
 */
@Data
public class Response<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> error(Integer code, String message) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> Response<T> error(String message) {
        return error(500, message);
    }
}
