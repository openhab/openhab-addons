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

import org.openhab.binding.jellyfin.internal.client.model.ClientInfo;
import org.openhab.binding.jellyfin.internal.client.model.DeviceInfo;

/**
 * Factory interface for creating API clients
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public interface ApiClientFactory {

    /**
     * Create a new API client
     *
     * @param baseUrl The base URL of the Jellyfin server
     * @param accessToken Access token for authentication
     * @param clientInfo Client information
     * @param deviceInfo Device information
     * @param httpClientOptions HTTP client options
     * @return A new API client
     */
    ApiClient create(String baseUrl, String accessToken, ClientInfo clientInfo, DeviceInfo deviceInfo,
            HttpClientOptions httpClientOptions);
}
