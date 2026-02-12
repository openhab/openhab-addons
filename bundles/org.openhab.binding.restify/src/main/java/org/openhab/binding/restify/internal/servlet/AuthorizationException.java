package org.openhab.binding.restify.internal.servlet;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class AuthorizationException extends UserRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(401, message);
    }
}
