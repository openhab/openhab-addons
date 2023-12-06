package org.openhab.binding.salusbinding.internal.rest;

import java.io.Serial;

@SuppressWarnings("SerializableHasSerializationMethods")
public class HttpServerException extends HttpException {
    @Serial
    private static final long serialVersionUID = 1L;
    public HttpServerException(int code, String method, String url) {
        super(code, "Server Error", method, url);
    }
}
