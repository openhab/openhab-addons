package org.openhab.binding.salusbinding.internal.rest;

import java.io.Serial;

public abstract class HttpException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 1453496993827105778L;

    public HttpException(int code, String message, String method, String url) {
        super(message);
    }
}
