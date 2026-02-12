package org.openhab.binding.restify.internal.servlet;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class ParameterException extends UserRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ParameterException(String parameterName) {
        super(460, "Could not parse parameter " + parameterName);
    }
}
