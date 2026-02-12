package org.openhab.binding.restify.internal.servlet;

import java.io.Serial;

public class NotFoundException extends UserRequestException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String path, DispatcherServlet.Method method) {
        super(404, "Endpoint not found: %s %s".formatted(method, path));
    }
}
