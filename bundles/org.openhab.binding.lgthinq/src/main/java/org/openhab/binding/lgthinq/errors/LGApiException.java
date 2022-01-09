package org.openhab.binding.lgthinq.errors;

public class LGApiException extends Exception{
    public LGApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public LGApiException(String message) {
        super(message);
    }
}
