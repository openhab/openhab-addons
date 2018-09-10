package org.openhab.binding.benqprojector.internal;

import java.io.IOException;

public interface BenqProjectorSerialInterface {

    public static class Response {
        public final boolean success;
        public final String value;
        public final String error;

        public Response(boolean success, String valueOrError) {
            this.success = success;
            if (success) {
                value = valueOrError;
                error = "";
            } else {
                value = "";
                error = valueOrError;
            }
        }
    }

    Response get(String key) throws IOException;

    Response put(String key, String value) throws IOException;

    boolean check();

    boolean reset();

    void close();
}
