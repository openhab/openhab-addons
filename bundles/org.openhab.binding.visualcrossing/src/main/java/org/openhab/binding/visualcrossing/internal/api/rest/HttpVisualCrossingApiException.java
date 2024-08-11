package org.openhab.binding.visualcrossing.internal.api.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpResponseException;
import org.openhab.binding.visualcrossing.internal.api.VisualCrossingApiException;

import java.io.Serial;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class HttpVisualCrossingApiException extends VisualCrossingApiException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int code;
    private final String msg;

    public HttpVisualCrossingApiException(int code, String msg, HttpResponseException e) {
        super("HTTP Error %s: %s".formatted(code, msg), e);
        this.code = code;
        this.msg = msg;
    }

    public HttpVisualCrossingApiException(int code, String msg) {
        super("HTTP Error %s: %s".formatted(code, msg));
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
