package org.openhab.binding.supla.internal.http;

import static java.lang.String.format;

final class HttpException extends RuntimeException {
    HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    HttpException(Request request, Throwable e) {
        super(format("Got exception while doing request to \"%s\" with headers \"%s\"", request.getPath(), formatHeaders(request)), e);
    }

    private static String formatHeaders(Request request) {
        return request.getHeaders()
                .stream()
                .map(header -> format("%s=%s", header.getKey(), header.getValue()))
                .reduce("", (acc, h) -> acc + ", " + h);
    }
}
