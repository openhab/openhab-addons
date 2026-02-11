package org.openhab.binding.restify.internal;

import java.io.Serial;

public class NotFoundException extends UserRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String path, RequestProcessor.Method method) {
        super(404, "Endpoint not found: %s %s".formatted(method, path));
    }
}
