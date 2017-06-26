package org.openhab.binding.supla.internal.server.http;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.ImmutableList.of;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public final class Request {
    private final String path;
    private final List<Header> headers;

    public Request(String path, List<Header> headers) {
        this.path = checkNotNull(path);
        checkArgument(!path.isEmpty());
        checkArgument(path.startsWith("/"), "Path should starts with '/', was " + path + ".");
        this.headers = ImmutableList.copyOf(headers);
    }

    public Request(String path) {
        this(path, new ArrayList<Header>(0));
    }

    public Request(String path, Header header) {
        this(path, of(header));
    }

    public String getPath() {
        return path;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Request)) {
            return false;
        }

        Request request = (Request) o;

        if (!path.equals(request.path)) {
            return false;
        }
        return headers.equals(request.headers);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "Request{" + "path='" + path + '\'' + ", headers=" + headers + '}';
    }
}
