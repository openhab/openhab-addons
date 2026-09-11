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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.transitapp.internal.action.TransitBridgeActions;
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
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class TransitAppBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppBridgeHandler.class);
    private final HttpClient httpClient;
    private volatile TransitApiClient apiClient;
    private @Nullable ScheduledFuture<?> verificationTask;

    public TransitAppBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.apiClient = new TransitApiClient(httpClient);
    }

    @Override
    public void initialize() {
        cancelVerificationTask();

        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        String apiKey = config.apiKey;

        if (apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key is missing");
            return;
        }

        TransitApiClient newApiClient = new TransitApiClient(httpClient, config.cacheTimeMs, config.retryAfterSeconds);
        this.apiClient = newApiClient;

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Verifying API Key...");

        verificationTask = scheduler.schedule(() -> {
            try {
                ContentResponse response = httpClient
                        .newRequest("https://external.transitapp.com/v4/public/nearby_stops?lat=0.0&lon=0.0")
                        .method(HttpMethod.GET).header("apiKey", apiKey).timeout(10, TimeUnit.SECONDS).send();

                int statusCode = response.getStatus();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.debug("API key verified successfully");
                    updateStatus(ThingStatus.ONLINE);
                } else if (statusCode == 401 || statusCode == 403) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "API Authentication Failed (Status: " + statusCode + ")");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unexpected API Response (Status: " + statusCode + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Connection Failed: " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public StopDeparturesResult getStopDepartures(String globalStopId) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        int maxDepartures = Math.max(1, Math.min(config.maxDepartures, 10));
        return apiClient.getStopDepartures(config.apiKey, globalStopId, maxDepartures);
    }

    public RouteDetailsResult getRouteDetails(String routeId) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.getRouteDetails(config.apiKey, routeId);
    }

    public TripDetailsResult getTripDetails(String tripSearchKey) throws Exception {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        if (config.apiKey.isBlank()) {
            throw new IllegalStateException("API Key missing");
        }
        return apiClient.getTripDetails(config.apiKey, tripSearchKey);
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
        int maxDepartures = Math.max(1, Math.min(config.maxDepartures, 10));
        return apiClient.fetchStopDepartures(config.apiKey, globalStopId, maxDepartures);
    }

    public int getMaxDepartures() {
        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        return config.maxDepartures;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.<Class<? extends ThingHandlerService>> singleton(TransitBridgeActions.class);
    }

    private void cancelVerificationTask() {
        ScheduledFuture<?> task = verificationTask;
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            verificationTask = null;
        }
    }

    @Override
    public void dispose() {
        cancelVerificationTask();
        super.dispose();
    }
}
