package org.openhab.binding.salus.internal.rest;

import java.io.Serial;

@SuppressWarnings("SerializableHasSerializationMethods")
public class HttpUnauthorizedException extends HttpException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HttpUnauthorizedException(String method, String url) {
        super(401, "Unauthorized", method, url);
    }
}
