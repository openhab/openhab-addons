package org.openhab.binding.restify.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.restify.internal.RequestProcessor.Method;

public record Response(String path, Method method, @Nullable Authorization authorization, Schema schema) {
    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Response response))
            return false;

        return path.equals(response.path) && method.equals(response.method);
    }

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + method.hashCode();
        return result;
    }
}
