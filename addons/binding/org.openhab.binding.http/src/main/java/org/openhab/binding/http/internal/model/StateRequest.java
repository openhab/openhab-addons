/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.model;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.http.internal.HttpBindingConstants;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * A class describing configuration for the HTTP request to make when fetching {@link State}.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
public class StateRequest extends ChannelRequest {
    private final Duration refreshInterval;

    StateRequest(final URL url,
                 final Optional<String> username,
                 final Optional<String> password,
                 final Duration connectTimeout,
                 final Duration requestTimeout,
                 final Duration refreshInterval,
                 final Optional<Transform> responseTransform)
    {
        super(HttpMethod.GET, url, username, password, connectTimeout, requestTimeout, HttpBindingConstants.DEFAULT_CONTENT_TYPE, Optional.empty(), responseTransform);
        this.refreshInterval = refreshInterval;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }
}
