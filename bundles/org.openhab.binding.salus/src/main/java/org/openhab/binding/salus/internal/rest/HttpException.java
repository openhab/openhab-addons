package org.openhab.binding.salus.internal.rest;

import java.io.Serial;

public class HttpException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1453496993827105778L;

    public HttpException(int code, String message, String method, String url) {
        super(message);
    }

    public HttpException(String method, String url, Exception ex) {
        super(String.format("Error occurred when executing %s %s", method, url), ex);
    }
}
