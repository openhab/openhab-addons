package org.openhab.binding.tado.swagger.codegen.api;

import org.eclipse.jetty.client.api.ContentResponse;

public class ApiException extends Exception {
    private int code = 0;
    private String responseBody = null;

    public ApiException() {
    }

    public ApiException(Throwable throwable) {
        super(throwable);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(ContentResponse response, String message, Throwable throwable) {
        super(message, throwable);
        this.code = response.getStatus();
        this.responseBody = response.getContentAsString();
    }

    public ApiException(ContentResponse response, String message) {
        this(response, message, null);
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
