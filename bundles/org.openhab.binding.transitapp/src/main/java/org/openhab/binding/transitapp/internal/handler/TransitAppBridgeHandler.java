/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transitapp.internal.handler;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.transitapp.internal.config.TransitAppBridgeConfiguration;
import org.openhab.binding.transitapp.internal.net.TransitApiClient;
import org.openhab.binding.transitapp.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transitapp.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transitapp.internal.net.dto.TripDetailsResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppBridgeHandler.class);
    private final HttpClient httpClient;
    private final TransitApiClient apiClient;

    public TransitAppBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.apiClient = new TransitApiClient(httpClient);
    }

    @Override
    public void initialize() {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        String apiKey = config.apiKey;

        if (apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key is missing");
            return;
        }

        logger.debug("API Key loaded successfully. Verifying connection...");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Verifying API Key...");

        scheduler.submit(() -> {
            try {
                ContentResponse response = httpClient
                        .newRequest("https://external.transitapp.com/v4/public/nearby_stops?lat=0.0&lon=0.0")
                        .method(HttpMethod.GET).header("apiKey", apiKey).timeout(10, TimeUnit.SECONDS).send();
                int statusCode = response.getStatus();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Transit API connection verified successfully! Status code: {}.", statusCode);
                    updateStatus(ThingStatus.ONLINE);
                } else if (statusCode == 401 || statusCode == 403) {
                    logger.error("API Authentication failed with status code {}.", statusCode);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "API Authentication Failed (Status: " + statusCode + ")");
                } else {
                    logger.error("Transit API verification failed with unexpected status {}.", statusCode);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unexpected API Response (Status: " + statusCode + ")");
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
                logger.error("Failed to connect to Transit API: {}", errorMessage, e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Connection Failed: " + errorMessage);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public StopDeparturesResult getStopDepartures(String globalStopId) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.getStopDepartures(config.apiKey, globalStopId);
    }

    public RouteDetailsResult getRouteDetails(String routeId) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.getRouteDetails(config.apiKey, routeId);
    }

    public TripDetailsResult getTripDetails(String tripId) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.getTripDetails(config.apiKey, tripId);
    }

    public String fetchNearbyStops(double lat, double lon) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.fetchNearbyStops(config.apiKey, lat, lon);
    }

    public String fetchStopDeparturesRaw(String globalStopId) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.fetchStopDepartures(config.apiKey, globalStopId);
    }
}
