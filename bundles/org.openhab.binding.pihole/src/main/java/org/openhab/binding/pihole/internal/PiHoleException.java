package org.openhab.binding.pihole.internal;

import java.io.Serial;

public class PiHoleException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public PiHoleException(String message) {
        super(message);
    }

    public PiHoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
