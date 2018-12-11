/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.model;

import org.eclipse.smarthome.core.types.State;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * A class describing configuration for the HTTP request to make when fetching {@link State}.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
public class StateRequest {
    private final URL url;
    private final Optional<String> username;
    private final Optional<String> password;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final Duration refreshInterval;
    private final Optional<Transform> responseTransform;

    StateRequest(final URL url,
                 final Optional<String> username,
                 final Optional<String> password,
                 final Duration connectTimeout,
                 final Duration requestTimeout,
                 final Duration refreshInterval,
                 final Optional<Transform> responseTransform)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        this.refreshInterval = refreshInterval;
        this.responseTransform = responseTransform;
    }

    public URL getUrl() {
        return url;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getPassword() {
        return password;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public Optional<Transform> getResponseTransform() {
        return responseTransform;
    }
}
