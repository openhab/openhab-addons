package org.openhab.binding.salus.internal.rest;

import java.io.Serial;

public class HttpForbiddenException extends HttpException{
    @Serial
    private static final long serialVersionUID = 1L;

    public HttpForbiddenException(String method, String url) {
        super(403, "Forbidden", method, url);
    }
}
