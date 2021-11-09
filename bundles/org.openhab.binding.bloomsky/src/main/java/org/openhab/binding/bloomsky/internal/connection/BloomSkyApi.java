/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.bloomsky.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link BloomSkyApi} Base class for BloomSky rest API requests used by the Handlers.
 *
 * @author Dave J Schoepel - Initial contribution
 */
@NonNullByDefault
public abstract class BloomSkyApi {
    private final HttpClient httpClient;
    protected final Gson gson;

    /**
     * Constructor for BloomSky rest API requests using Gson API's to parse JSON responses.
     *
     * @param httpClient - common client used for API GET requests
     */
    protected BloomSkyApi(HttpClient httpClient) {
        this.httpClient = httpClient;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    /**
     * Method to retrieve the BloomSky API base URL.
     *
     * @return BloomSky base URL for API get requests
     */
    protected abstract String getBaseUrl();

    /**
     * Method to retrieve the shared HTTP client used by the Handlers to retrieve device/observations from the API.
     *
     * @return Common shared HTTP client to use for API requests
     */
    protected HttpClient getHttpClient() {
        return httpClient;
    }
}
