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
package org.openhab.binding.jellyfin.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * JellyfinApiClient is a API client for interacting with the Jellyfin server.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.SINGLETON, configurationPid = "api.jellyfin", service = ApiClientFactory.class)
public class ApiClientFactory {
    private final HttpClientFactory httpClientFactory;

    @Activate
    public ApiClientFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Creates a new ApiClient instance configured with the HTTP client factory.
     * 
     * @param baseUrl The base URL for the Jellyfin server
     * @return A configured ApiClient instance
     */
    public ApiClient createApiClient() {
        ApiClient client = new ApiClient();
        // Configure the client with HTTP client factory and base URL
        // Add any additional configuration here
        return client;
    }
}
