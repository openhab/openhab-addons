package org.openhab.binding.rotelra1x.internal;

public class ConfigurationError extends Exception {

    /**
     * Exception to indicate a failure due to incorrect configuration.
     */
    private static final long serialVersionUID = 1L;

    public ConfigurationError(String message) {
        super(message);
    }

}
