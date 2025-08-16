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
package org.openhab.binding.jellyfin.internal.client;

import org.openhab.binding.jellyfin.internal.client.api.client.ApiClient;
import org.openhab.binding.jellyfin.internal.client.api.client.HttpClientOptions;
import org.openhab.binding.jellyfin.internal.client.discovery.DiscoveryService;
import org.openhab.binding.jellyfin.internal.client.model.ClientInfo;
import org.openhab.binding.jellyfin.internal.client.model.DeviceInfo;
import org.openhab.binding.jellyfin.internal.client.model.ServerVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Jellyfin client class for interacting with Jellyfin servers
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class Jellyfin {

    private final Logger logger = LoggerFactory.getLogger(Jellyfin.class);
    private final JellyfinOptions options;

    /**
     * Create a new Jellyfin client instance
     *
     * @param options Configuration options for the client
     */
    public Jellyfin(JellyfinOptions options) {
        this.options = options;
    }

    /**
     * Create a new Jellyfin client instance
     *
     * @param optionsBuilder Builder for configuration options
     */
    public Jellyfin(JellyfinOptions.Builder optionsBuilder) {
        this(optionsBuilder.build());
    }

    /**
     * Get the device information
     *
     * @return Device information or null if not set
     */
    public DeviceInfo getDeviceInfo() {
        return options.getDeviceInfo();
    }

    /**
     * Get the client information
     *
     * @return Client information or null if not set
     */
    public ClientInfo getClientInfo() {
        return options.getClientInfo();
    }

    /**
     * Get the discovery service to help with normalizing server addresses and find servers in the local network
     *
     * @return The discovery service instance
     */
    public DiscoveryService getDiscovery() {
        // Note: In a real implementation, this would be lazily instantiated
        return new DiscoveryService(this);
    }

    /**
     * Create a new API client instance to use in API services.
     * The clientInfo and deviceInfo parameters are required when not passed as option in JellyfinOptions.
     * The baseUrl is only required when HTTP calls are made.
     *
     * @param baseUrl The base URL of the Jellyfin server
     * @param accessToken Access token for authentication
     * @param clientInfo Client information
     * @param deviceInfo Device information
     * @param httpClientOptions HTTP client options
     * @return A new API client instance
     * @throws IllegalArgumentException If required parameters are missing
     */
    public ApiClient createApi(String baseUrl, String accessToken, ClientInfo clientInfo, DeviceInfo deviceInfo,
            HttpClientOptions httpClientOptions) {

        if (clientInfo == null) {
            clientInfo = options.getClientInfo();
        }

        if (deviceInfo == null) {
            deviceInfo = options.getDeviceInfo();
        }

        if (clientInfo == null) {
            throw new IllegalArgumentException(
                    "ClientInfo needs to be set when calling createApi() or by providing it when constructing the Jellyfin instance");
        }

        if (deviceInfo == null) {
            throw new IllegalArgumentException(
                    "DeviceInfo needs to be set when calling createApi() or by providing it when constructing the Jellyfin instance");
        }

        return options.getApiClientFactory().create(baseUrl, accessToken, clientInfo, deviceInfo, httpClientOptions);
    }

    /**
     * Create a new API client with default HTTP options
     *
     * @param baseUrl The base URL of the Jellyfin server
     * @param accessToken Access token for authentication
     * @return A new API client instance
     */
    public ApiClient createApi(String baseUrl, String accessToken) {
        return createApi(baseUrl, accessToken, null, null, new HttpClientOptions());
    }

    /**
     * Get the minimum server version expected to work
     *
     * @return The minimum supported server version
     */
    public static ServerVersion getMinimumVersion() {
        return new ServerVersion(10, 10, 0, 0);
    }
}
