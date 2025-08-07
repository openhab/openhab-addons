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

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Factory for creating Jellyfin API clients that integrate with the OpenHAB HTTP client framework.
 * This factory provides ApiClient instances that leverage OpenHAB's common HTTP client infrastructure
 * for consistent configuration, SSL handling, and connection management.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.SINGLETON, configurationPid = "api.jellyfin", service = ApiClientFactory.class)
public class ApiClientFactory {
    private final ClientBuilder clientBuilder;

    @Activate
    public ApiClientFactory(@Reference final ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    /**
     * Creates a new ApiClient instance configured with OpenHAB's HTTP client framework.
     * The created client integrates with OpenHAB's common HTTP client infrastructure,
     * providing consistent SSL handling, connection pooling, and configuration management.
     * 
     * @return A configured ApiClient instance ready for use with the Jellyfin API
     */
    public ApiClient createApiClient() {
        return new ApiClient(clientBuilder);
    }

    /**
     * Creates a new ApiClient instance with a specific base URL.
     * 
     * @param baseUrl The base URL for the Jellyfin server
     * @return A configured ApiClient instance
     */
    public ApiClient createApiClient(String baseUrl) {
        ApiClient client = new ApiClient(clientBuilder);
        client.setBasePath(baseUrl);
        return client;
    }
}
