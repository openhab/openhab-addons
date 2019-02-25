/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;

import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_COMMAND_METHOD;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_CONNECT_TIMEOUT;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_CONTENT_TYPE;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_REQUEST_TIMEOUT;
import static org.openhab.binding.http.internal.HttpBindingConstants.DEFAULT_STATE_REFRESH_INTERVAL;

/**
 * A class describing configuration for the HTTP Endpoint Thing.
 *
 * @author Brian J. Tarricone - Initial contribution
 */
@NonNullByDefault
public class HttpEndpointConfig {
    @SuppressWarnings("unused")
    private @Nullable String baseUrl;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT.toMillis();
    private long requestTimeout = DEFAULT_REQUEST_TIMEOUT.toMillis();
    @SuppressWarnings("unused")
    private @Nullable String username;
    @SuppressWarnings("unused")
    private @Nullable String password;

    private long stateRefreshInterval = DEFAULT_STATE_REFRESH_INTERVAL.toMillis();

    private HttpMethod commandMethod = DEFAULT_COMMAND_METHOD;
    private String commandContentType = DEFAULT_CONTENT_TYPE;

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public long getStateRefreshInterval() {
        return stateRefreshInterval;
    }

    public HttpMethod getCommandMethod() {
        return commandMethod;
    }

    public String getCommandContentType() {
        return commandContentType;
    }
}
