package org.openhab.binding.restify.internal.config;

import java.io.Serial;

public class ConfigException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
