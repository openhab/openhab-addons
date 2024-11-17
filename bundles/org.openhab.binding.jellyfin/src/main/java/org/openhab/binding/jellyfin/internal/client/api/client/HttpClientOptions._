/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.jellyfin.internal.client.api.client;

import java.time.Duration;

/**
 * Configuration options for the Jellyfin API HTTP client
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class HttpClientOptions {

    private boolean followRedirects = true;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration requestTimeout = Duration.ofSeconds(60);
    private Duration socketTimeout = Duration.ofMinutes(5);

    /**
     * Creates a new HttpClientOptions with default values
     */
    public HttpClientOptions() {
    }

    /**
     * Check if the client should follow redirects
     *
     * @return True if redirects should be followed
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Set whether the client should follow redirects
     *
     * @param followRedirects True to follow redirects
     * @return This instance for method chaining
     */
    public HttpClientOptions setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    /**
     * Get the connection timeout duration
     *
     * @return The connection timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Set the connection timeout duration
     *
     * @param connectTimeout The connection timeout
     * @return This instance for method chaining
     */
    public HttpClientOptions setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Get the request timeout duration
     *
     * @return The request timeout
     */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Set the request timeout duration
     *
     * @param requestTimeout The request timeout
     * @return This instance for method chaining
     */
    public HttpClientOptions setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * Get the socket timeout duration
     *
     * @return The socket timeout
     */
    public Duration getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * Set the socket timeout duration
     *
     * @param socketTimeout The socket timeout
     * @return This instance for method chaining
     */
    public HttpClientOptions setSocketTimeout(Duration socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }
}
