package org.openhab.binding.salus.internal.rest;

import java.io.Serial;

@SuppressWarnings("SerializableHasSerializationMethods")
public class HttpUnknownException extends HttpException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HttpUnknownException(int code, String method, String url) {
        super(code, "Unknown", method, url);
    }
}
