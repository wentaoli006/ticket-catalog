package com.owen.infrastructure.http;

/**
 * 出站 HTTP 传输失败。地址不合法、超时、连接失败、非 2xx 都算传输失败，业务错误码由调用方自己判断。
 */
public class HttpRequestException extends RuntimeException {

    public HttpRequestException(String message) {
        super(message);
    }

    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
