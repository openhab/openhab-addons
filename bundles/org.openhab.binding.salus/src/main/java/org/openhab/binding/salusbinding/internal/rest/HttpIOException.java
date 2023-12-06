package org.openhab.binding.salusbinding.internal.rest;

import java.io.IOException;
import java.io.Serial;
import java.io.UncheckedIOException;

@SuppressWarnings("SerializableHasSerializationMethods")
final class HttpIOException extends UncheckedIOException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HttpIOException(String method, String url, IOException cause) {
        super("Exception occurred when querying URL " + method + " " + url + "!", cause);
    }
}
