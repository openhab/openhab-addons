/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.model;

import org.eclipse.smarthome.core.types.State;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * A class describing configuration for the HTTP request to make when fetching {@link State}.
 *
 * @author Brian J. Tarricone
 */
public class StateRequest {
    private final URL url;
    private final Duration refreshInterval;
    private final Optional<Transform> responseTransform;

    public StateRequest(final URL url, final Duration refreshInterval, final Optional<Transform> responseTransform) {
        this.url = url;
        this.refreshInterval = refreshInterval;
        this.responseTransform = responseTransform;
    }

    public URL getUrl() {
        return url;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public Optional<Transform> getResponseTransform() {
        return responseTransform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateRequest that = (StateRequest) o;
        return getUrl().equals(that.getUrl()) &&
                getRefreshInterval().equals(that.getRefreshInterval()) &&
                getResponseTransform().equals(that.getResponseTransform());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl(), getRefreshInterval(), getResponseTransform());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StateRequest.class.getSimpleName() + "[", "]")
                .add("url=" + url)
                .add("refreshInterval=" + refreshInterval)
                .add("responseTransform=" + responseTransform)
                .toString();
    }
}
