package org.openhab.binding.restify.internal;

import java.io.Serial;

public class AuthorizationException extends UserRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(401, message);
    }
}
