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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Custom ApiClient that integrates with OpenHAB's HTTP client framework.
 * This client extends the generated ApiClient to use OpenHAB's JAX-RS ClientBuilder
 * instead of creating its own HTTP client, ensuring consistent configuration,
 * SSL handling, and connection management across the OpenHAB framework.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class ApiClient extends org.openhab.binding.jellyfin.internal.api.generated.ApiClient {

    private final ClientBuilder openhabClientBuilder;

    /**
     * Constructor that accepts OpenHAB's ClientBuilder for integration with the framework.
     * This leverages OpenHAB's HTTP client infrastructure for consistent configuration,
     * SSL handling, and connection management across the OpenHAB framework.
     * 
     * @param clientBuilder OpenHAB's JAX-RS ClientBuilder service (required)
     */
    public ApiClient(ClientBuilder clientBuilder) {
        super();
        this.openhabClientBuilder = clientBuilder;
        // Set the HTTP client immediately to avoid recreating it
        this.setHttpClient(buildHttpClient());
    }

    /**
     * Overrides the default HTTP client building to use OpenHAB's ClientBuilder.
     * This ensures that the created JAX-RS Client integrates with OpenHAB's HTTP client framework,
     * providing consistent SSL handling, connection pooling, and configuration management.
     * 
     * @return A JAX-RS Client configured with OpenHAB's framework
     */
    @Override
    protected Client buildHttpClient() {
        // Use OpenHAB's ClientBuilder with the same configuration as the parent class
        if (clientConfig == null) {
            clientConfig = getDefaultClientConfig();
        }

        ClientBuilder builder = openhabClientBuilder.withConfig(clientConfig);
        customizeClientBuilder(builder);
        return builder.build();
    }
}
