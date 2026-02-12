package org.openhab.binding.restify.internal.config;

import java.io.Serial;

public class ConfigParseException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
