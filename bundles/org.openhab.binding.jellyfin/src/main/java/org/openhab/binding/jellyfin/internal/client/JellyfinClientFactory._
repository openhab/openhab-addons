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

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.jellyfin.internal.client.api.client.impl.JettyApiClientFactory;
import org.openhab.binding.jellyfin.internal.client.model.ClientInfo;
import org.openhab.binding.jellyfin.internal.client.model.DeviceInfo;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating Jellyfin client instances
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
@Component(service = JellyfinClientFactory.class)
@NonNullByDefault
public class JellyfinClientFactory {

    private final Logger logger = LoggerFactory.getLogger(JellyfinClientFactory.class);

    private static final String CLIENT_NAME = "openHAB";
    private static final String CLIENT_VERSION = "1.0.0";
    private static final String DEVICE_NAME = "openHAB Binding";

    private final HttpClient httpClient;

    @Activate
    public JellyfinClientFactory(@Reference HttpClientFactory httpClientFactory) {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        this.httpClient = httpClientFactory.getCommonHttpClient();

        try {
            if (!httpClient.isStarted()) {
                httpClient.start();
            }
        } catch (Exception e) {
            logger.error("Failed to start HTTP client: {}", e.getMessage());
        }
    }

    /**
     * Create a new Jellyfin client instance
     *
     * @param deviceId The device ID to use, or null to generate a random one
     * @return A new Jellyfin client instance
     */
    public Jellyfin createJellyfinClient(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            deviceId = UUID.randomUUID().toString();
        }

        ClientInfo clientInfo = new ClientInfo(CLIENT_NAME, CLIENT_VERSION);
        DeviceInfo deviceInfo = new DeviceInfo(deviceId, DEVICE_NAME);

        JettyApiClientFactory apiClientFactory = new JettyApiClientFactory(httpClient);

        JellyfinOptions options = new JellyfinOptions.Builder().setClientInfo(clientInfo).setDeviceInfo(deviceInfo)
                .setApiClientFactory(apiClientFactory).build();

        return new Jellyfin(options);
    }

    /**
     * Create a new Jellyfin client instance with a random device ID
     *
     * @return A new Jellyfin client instance
     */
    public Jellyfin createJellyfinClient() {
        return createJellyfinClient(null);
    }

    /**
     * Clean up resources
     */
    public void dispose() {
        try {
            if (httpClient.isStarted()) {
                httpClient.stop();
            }
        } catch (Exception e) {
            logger.error("Failed to stop HTTP client: {}", e.getMessage());
        }
    }
}
