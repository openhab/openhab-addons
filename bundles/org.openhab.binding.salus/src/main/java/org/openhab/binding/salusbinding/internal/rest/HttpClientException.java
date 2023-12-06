package org.openhab.binding.salusbinding.internal.rest;

import java.io.Serial;

@SuppressWarnings("SerializableHasSerializationMethods")
public class HttpClientException extends HttpException{
    @Serial
    private static final long serialVersionUID = 1L;
    public HttpClientException(int code, String method, String url) {
        super(code, "Client Error", method, url);
    }
}
