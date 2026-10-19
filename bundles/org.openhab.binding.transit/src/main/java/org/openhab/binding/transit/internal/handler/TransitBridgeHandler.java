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
package org.openhab.binding.transit.internal.handler;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.transit.internal.action.TransitBridgeActions;
import org.openhab.binding.transit.internal.config.TransitBridgeConfiguration;
import org.openhab.binding.transit.internal.net.TransitApiClient;
import org.openhab.binding.transit.internal.net.dto.RouteDetailsResult;
import org.openhab.binding.transit.internal.net.dto.StopDeparturesResult;
import org.openhab.binding.transit.internal.net.dto.TripDetailsResult;
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
public class TransitBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitBridgeHandler.class);
    private final HttpClient httpClient;
    private volatile TransitApiClient apiClient;
    private TransitBridgeConfiguration config = new TransitBridgeConfiguration();
    private @Nullable ScheduledFuture<?> verificationTask;

    public TransitBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.apiClient = new TransitApiClient(httpClient);
    }

    @Override
    public void initialize() {
        cancelVerificationTask();

        config = getConfigAs(TransitBridgeConfiguration.class);

        if (config.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-api-key");
            return;
        }

        apiClient = new TransitApiClient(httpClient, config.cacheTimeMs, config.retryAfterSeconds);

        updateStatus(ThingStatus.UNKNOWN);
        scheduleVerification(0);
    }

    private void verifyApiKey() {
        try {
            ContentResponse response = httpClient.newRequest(API_BASE_URL + "nearby_stops?lat=0.0&lon=0.0")
                    .method(HttpMethod.GET).header(API_KEY_HEADER, config.apiKey)
                    .timeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();

            if (Thread.currentThread().isInterrupted()) {
                // cancelled by dispose() or a new initialize(), do not publish an outdated status
                return;
            }

            int statusCode = response.getStatus();
            if (HttpStatus.isSuccess(statusCode)) {
                updateStatus(ThingStatus.ONLINE);
            } else if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
                // invalid API key: no retry, the user has to fix the configuration
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.comm-error-auth-failed [\"" + statusCode + "\"]");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-unexpected-response [\"" + statusCode + "\"]");
                long delay = statusCode == HttpStatus.TOO_MANY_REQUESTS_429 ? getRetryAfterSeconds(response)
                        : config.retryAfterSeconds;
                scheduleVerification(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-timeout");
            scheduleVerification(config.retryAfterSeconds);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    cause != null ? cause.getMessage() : e.getMessage());
            scheduleVerification(config.retryAfterSeconds);
        }
    }

    private long getRetryAfterSeconds(ContentResponse response) {
        String headerValue = response.getHeaders().get(HttpHeader.RETRY_AFTER);
        if (headerValue != null) {
            try {
                return Math.max(1, Long.parseLong(headerValue.trim()));
            } catch (NumberFormatException e) {
                logger.debug("Invalid Retry-After header value: {}, using configured default", headerValue);
            }
        }
        return config.retryAfterSeconds;
    }

    private synchronized void scheduleVerification(long delaySeconds) {
        if (Thread.currentThread().isInterrupted()) {
            // the running verification was cancelled, do not schedule a new one
            return;
        }
        if (delaySeconds > 0) {
            logger.debug("Retrying Transit API verification in {} seconds", delaySeconds);
        }
        verificationTask = scheduler.schedule(this::verifyApiKey, delaySeconds, TimeUnit.SECONDS);
    }

    private synchronized void cancelVerificationTask() {
        ScheduledFuture<?> task = verificationTask;
        if (task != null) {
            task.cancel(true);
            verificationTask = null;
        }
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

    @Override
    public void dispose() {
        cancelVerificationTask();
        super.dispose();
    }
}
