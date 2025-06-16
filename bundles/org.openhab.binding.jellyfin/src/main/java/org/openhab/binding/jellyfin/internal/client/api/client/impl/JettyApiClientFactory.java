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
package org.openhab.binding.jellyfin.internal.client.api.client.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.jellyfin.internal.client.api.client.ApiClient;
import org.openhab.binding.jellyfin.internal.client.api.client.ApiClientFactory;
import org.openhab.binding.jellyfin.internal.client.api.client.HttpClientOptions;
import org.openhab.binding.jellyfin.internal.client.model.ClientInfo;
import org.openhab.binding.jellyfin.internal.client.model.DeviceInfo;

/**
 * Factory for creating Jetty-based API clients
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
@NonNullByDefault
public class JettyApiClientFactory implements ApiClientFactory {

    private final HttpClient httpClient;

    /**
     * Create a new Jetty API client factory
     *
     * @param httpClient The Jetty HTTP client
     */
    public JettyApiClientFactory(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public ApiClient create(String baseUrl, String accessToken, ClientInfo clientInfo, DeviceInfo deviceInfo,
            HttpClientOptions httpClientOptions) {
        return new JettyApiClient(httpClient, baseUrl, accessToken, clientInfo, deviceInfo, httpClientOptions);
    }
}
