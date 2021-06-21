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
package org.openhab.binding.bloomsky.internal.bridge;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bloomsky.internal.connection.BloomSkyCommunicationException;
import org.openhab.binding.bloomsky.internal.connection.BloomSkyConnectApi;
import org.openhab.binding.bloomsky.internal.dto.BloomSkyJsonSensorData;

/**
 * The {@link BloomSkyBridge} allows the communication to the BloomSky rest API
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
@NonNullByDefault
public class BloomSkyBridge {
    private final String apiKey;
    private final String displayUnits;

    private final BloomSkyConnectApi bloomSkyApi;

    /**
     * Constructor used by the Handlers to communicate with the BloomSky API.
     *
     * @param apiKey to be used to request BloomSky device information and observations
     * @param displayUnits to be set in URL used to query API for observations
     * @param httpClient common client for GET request calls to BloomSky API
     * @param scheduler for background jobs to discover/refresh devices/observations
     */
    public BloomSkyBridge(String apiKey, String displayUnits, HttpClient httpClient,
            ScheduledExecutorService scheduler) {
        super();
        this.apiKey = apiKey;
        this.displayUnits = displayUnits;
        this.bloomSkyApi = new BloomSkyConnectApi(httpClient);
    }

    /**
     * This method returns the response details from a call to the BloomSky rest API in the form of an
     * array of devices defined by the Model (DTO) called BloomSkyJsonSensorData.
     *
     * @return A result containing a list of sky devices that are available for the current user
     * @throws BloomSkyCommunicationException In case the query cannot be executed successfully
     */
    public BloomSkyJsonSensorData[] getBloomSkyDevices() throws BloomSkyCommunicationException {
        return bloomSkyApi.getSkyDeviceData(apiKey, displayUnits);
    }
}
