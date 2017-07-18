/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.server.http;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.of;

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
