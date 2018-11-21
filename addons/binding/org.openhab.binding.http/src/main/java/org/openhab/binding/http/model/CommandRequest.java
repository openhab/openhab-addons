/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.model;

import org.eclipse.smarthome.core.types.Command;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * A class describing configuration for the HTTP request to make when sending a {@link Command}.
 *
 * @author Brian J. Tarricone
 */
public class CommandRequest {
    /**
     * Enumeration describing the HTTP method.
     */
    public enum Method {
        POST, GET
    }

    private final Method method;
    private final URL url;
    private final String contentType;
    private final Optional<Transform> requestTransform;
    private final Optional<Transform> responseTransform;

    public CommandRequest(final Method method, final URL url, final String contentType, final Optional<Transform> requestTransform, final Optional<Transform> responseTransform) {
        this.method = method;
        this.url = url;
        this.contentType = contentType;
        this.requestTransform = requestTransform;
        this.responseTransform = responseTransform;
    }

    public Method getMethod() {
        return method;
    }

    public URL getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public Optional<Transform> getRequestTransform() {
        return requestTransform;
    }

    public Optional<Transform> getResponseTransform() {
        return responseTransform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandRequest that = (CommandRequest) o;
        return getMethod() == that.getMethod() &&
                getUrl().equals(that.getUrl()) &&
                getContentType().equals(that.getContentType()) &&
                getRequestTransform().equals(that.getRequestTransform()) &&
                getResponseTransform().equals(that.getResponseTransform());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(), getUrl(), getContentType(), getRequestTransform(), getResponseTransform());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CommandRequest.class.getSimpleName() + "[", "]")
                .add("method=" + method)
                .add("url=" + url)
                .add("contentType='" + contentType + "'")
                .add("requestTransform=" + requestTransform)
                .add("responseTransform=" + responseTransform)
                .toString();
    }
}
