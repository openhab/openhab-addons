package org.openhab.binding.restify.internal;

import java.io.Serial;

public class NotFoundException extends UserRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String path) {
        super(404, "Path not found: " + path);
    }
}
