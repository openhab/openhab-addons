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

import static org.openhab.binding.transitapp.internal.TransitAppBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
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

@NonNullByDefault
public class TransitAppBridgeHandler extends BaseBridgeHandler {

    private final HttpClient httpClient;
    private volatile TransitApiClient apiClient;
    private TransitAppBridgeConfiguration config = new TransitAppBridgeConfiguration();
    private @Nullable ScheduledFuture<?> verificationTask;

    public TransitAppBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.apiClient = new TransitApiClient(httpClient);
    }

    @Override
    public void initialize() {
        cancelVerificationTask();

        config = getConfigAs(TransitAppBridgeConfiguration.class);
        String apiKey = config.apiKey;

        if (apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-api-key");
            return;
        }

        apiClient = new TransitApiClient(httpClient, config.cacheTimeMs, config.retryAfterSeconds);

        updateStatus(ThingStatus.UNKNOWN);

        verificationTask = scheduler.schedule(() -> {
            try {
                ContentResponse response = httpClient.newRequest(API_BASE_URL + "nearby_stops?lat=0.0&lon=0.0")
                        .method(HttpMethod.GET).header(API_KEY_HEADER, apiKey)
                        .timeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

                int statusCode = response.getStatus();
                if (HttpStatus.isSuccess(statusCode)) {
                    updateStatus(ThingStatus.ONLINE);
                } else if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.comm-error-auth-failed [\"" + statusCode + "\"]");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-unexpected-response [\"" + statusCode + "\"]");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-timeout");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        cause != null ? cause.getMessage() : e.getMessage());
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public StopDeparturesResult getStopDepartures(String globalStopId) throws IOException, InterruptedException {
        return apiClient.getStopDepartures(config.apiKey, globalStopId, getMaxDepartures());
    }

    public RouteDetailsResult getRouteDetails(String routeId) throws IOException, InterruptedException {
        return apiClient.getRouteDetails(config.apiKey, routeId);
    }

    public TripDetailsResult getTripDetails(String tripSearchKey) throws IOException, InterruptedException {
        return apiClient.getTripDetails(config.apiKey, tripSearchKey);
    }

    public String fetchNearbyStops(double lat, double lon) throws IOException, InterruptedException {
        return apiClient.fetchNearbyStops(config.apiKey, lat, lon);
    }

    public String fetchStopDeparturesRaw(String globalStopId) throws IOException, InterruptedException {
        return apiClient.fetchStopDepartures(config.apiKey, globalStopId, getMaxDepartures());
    }

    public int getMaxDepartures() {
        return Math.max(1, Math.min(config.maxDepartures, MAX_CHANNEL_GROUPS));
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
