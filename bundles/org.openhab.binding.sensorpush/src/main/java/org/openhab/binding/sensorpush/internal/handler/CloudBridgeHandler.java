/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sensorpush.internal.handler;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.sensorpush.internal.SensorPushDiscoveryService;
import org.openhab.binding.sensorpush.internal.config.CloudBridgeConfiguration;
import org.openhab.binding.sensorpush.internal.protocol.AccessTokenRequest;
import org.openhab.binding.sensorpush.internal.protocol.AccessTokenResponse;
import org.openhab.binding.sensorpush.internal.protocol.AuthorizationRequest;
import org.openhab.binding.sensorpush.internal.protocol.AuthorizationResponse;
import org.openhab.binding.sensorpush.internal.protocol.Endpoint;
import org.openhab.binding.sensorpush.internal.protocol.InvalidResponseException;
import org.openhab.binding.sensorpush.internal.protocol.JwtInfo;
import org.openhab.binding.sensorpush.internal.protocol.Sample;
import org.openhab.binding.sensorpush.internal.protocol.SamplesRequest;
import org.openhab.binding.sensorpush.internal.protocol.SamplesResponse;
import org.openhab.binding.sensorpush.internal.protocol.Sensor;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link CloudBridgeHandler} is responsible for communicating with the SensorPush cloud.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class CloudBridgeHandler extends BaseBridgeHandler {

    private static final String AGENT = "openHAB SensorPush Binding";
    private static final String API_URL = "https://api.sensorpush.com";
    private static final int POLL_MIN = 2;
    private static final int POLL_MAX = 60;
    private static final int TIMEOUT_MIN = 1;
    private static final int TIMEOUT_MAX = 120;

    private final Logger logger = LoggerFactory.getLogger(CloudBridgeHandler.class);

    private final HttpClient httpClient;
    private final Gson gson;

    private @Nullable SensorPushDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> pollingJob;
    private CloudBridgeConfiguration config = new CloudBridgeConfiguration();

    private @Nullable String accessToken;
    private @Nullable JwtInfo accessTokenInfo;

    private boolean pollSensorsRun = true;

    public CloudBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        gson = new GsonBuilder().create();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SensorPushDiscoveryService.class);
    }

    public void setDiscoveryService(SensorPushDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void initialize() {
        config = getConfigAs(CloudBridgeConfiguration.class);

        if (config.user.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "user/password not configured");
            return;
        }
        logger.trace("CloudBridge for user {} initializing.", config.user);

        if (config.poll < POLL_MIN) {
            config.poll = POLL_MIN;
        } else if (config.poll > POLL_MAX) {
            config.poll = POLL_MAX;
        }

        if (config.timeout < TIMEOUT_MIN) {
            config.timeout = TIMEOUT_MIN;
        } else if (config.poll > TIMEOUT_MAX) {
            config.timeout = TIMEOUT_MAX;
        }

        updateStatus(ThingStatus.UNKNOWN); // asyncInitialize will set final status

        scheduler.submit(this::asyncInitialize);
    }

    /**
     * Perform the init tasks that take a while here because initialize() has to return quickly.
     */
    private synchronized void asyncInitialize() {
        authorize();

        if (accessToken != null) {
            updateStatus(ThingStatus.ONLINE);

            pollSensors();
            pollSamples();
        }

        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob == null || pollingJob.isDone()) {
            logger.debug("Starting polling job with interval {} minutes", config.poll);
            this.pollingJob = scheduler.scheduleWithFixedDelay(this::doPolling, config.poll, config.poll,
                    TimeUnit.MINUTES);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels defined. Do nothing.
    }

    /**
     * Post a request to the provided URI with the provided content and optional authToken.
     *
     * @param uri String containing the URI
     * @param content String containing the content
     * @param authToken String containing the authorization token to use or null if none
     * @return ContentResponse object
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private @Nullable ContentResponse sendRequest(String uri, String content, @Nullable String authToken)
            throws TimeoutException, InterruptedException, ExecutionException {
        ContentResponse response = null;
        ContentProvider contentProvider = new StringContentProvider(content);
        logger.trace("Using authorization token: {}", authToken);

        Request request = httpClient.POST(uri);
        request.agent(AGENT);
        request.accept("application/json");
        request.header(HttpHeader.AUTHORIZATION, authToken);
        request.timeout(config.timeout, TimeUnit.SECONDS);
        request.content(contentProvider, "application/json");
        logger.debug("Sending request: {}", request.toString());
        response = request.send();

        if (response != null) {
            logger.debug("Response received: {} : {}", response.getStatus(), response.getReason());
            logger.trace("Response content: {}", response.getContentAsString());
        }
        return response;
    }

    /**
     * Returns true if the HTTP status is considered good.
     */
    private static boolean goodStatus(int status) {
        return (status >= 200 && status < 300);
    }

    /**
     * Perform authorization with the cloud service. Send username/password to get an authorization token, then send the
     * authorization token to get an access token. Both are JWT tokens, so we decode the access token to get its
     * expiration time.
     */
    private void authorize() {
        AuthorizationResponse authResponse;
        AccessTokenResponse tokenResponse;

        try {
            // Send user/password and get authorization token
            logger.debug("Getting new authorization token");
            AuthorizationRequest authRequest = new AuthorizationRequest(config.user, config.password);
            ContentResponse authContentResponse = sendRequest(API_URL + Endpoint.AUTHORIZE, gson.toJson(authRequest),
                    null);

            if (authContentResponse == null) {
                throw new InvalidResponseException("Auth content response is null");
            }
            int status = authContentResponse.getStatus();
            if (status == 403) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid username/password");
                return;
            }
            if (!goodStatus(authContentResponse.getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Auth request returned status " + Integer.valueOf(status));
                return;
            }
            authResponse = gson.fromJson(authContentResponse.getContentAsString(), AuthorizationResponse.class);
            if (authResponse == null) {
                throw new InvalidResponseException("Auth response is null");
            }
            String authorizationToken = authResponse.authorization;
            if (authorizationToken == null) {
                throw new InvalidResponseException("Auth token is null");
            }

            // Send authorization token and get access token
            logger.debug("Getting new access token");
            String requestContent = gson.toJson(new AccessTokenRequest(authorizationToken));
            ContentResponse accessContentResponse = sendRequest(API_URL + Endpoint.ACCESSTOKEN, requestContent, null);
            if (accessContentResponse == null) {
                throw new InvalidResponseException("Access token response is null");
            }
            if (!goodStatus(accessContentResponse.getStatus())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Access request returned status " + Integer.valueOf(status));
                return;
            }

            tokenResponse = gson.fromJson(accessContentResponse.getContentAsString(), AccessTokenResponse.class);
            if (tokenResponse == null) {
                throw new InvalidResponseException("Access token object is null");
            }
            String accessToken = tokenResponse.accessToken;
            logger.trace("Received access token: {}", accessToken);

            JwtInfo jwtInfo;
            try {
                jwtInfo = new JwtInfo(accessToken);
            } catch (IllegalArgumentException e) {
                throw new InvalidResponseException("Invalid JWT token");
            }
            this.accessToken = accessToken;
            accessTokenInfo = jwtInfo;
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Auth request timeout");
            return;
        } catch (InterruptedException e) {
            logger.debug("Interrupted sending authorization request");
            return;
        } catch (ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Auth request execution exception");
            return;
        } catch (JsonSyntaxException | InvalidResponseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error parsing auth response");
            return;
        }
    }

    /**
     * Returns true if the JWT token represented by jwtInfo is expired or will expire in the next 60 seconds.
     */
    private boolean tokenExpired(@Nullable JwtInfo jwtInfo) {
        if (jwtInfo == null) {
            return true;
        } else {
            return (jwtInfo.expires.minusSeconds(60).isBefore(Instant.now()));
        }
    }

    /**
     * Polling routine. Polls for samples every run, but sensors only every other run. This reduces the polling load on
     * the cloud service.
     */
    private void doPolling() {
        if (accessToken == null || accessTokenInfo == null || tokenExpired(accessTokenInfo)) {
            authorize();
        }

        if (pollSensorsRun) {
            pollSensors();
            pollSensorsRun = false;
        } else {
            pollSensorsRun = true;
        }

        pollSamples();
    }

    /**
     * Poll for sensor info. Also supplies sensor list to discovery.
     */
    public void pollSensors() {
        logger.trace("Polling sensors");

        try {
            ContentResponse response = sendRequest(API_URL + Endpoint.SENSORS, "{}\r\n", accessToken);
            if (response == null) {
                throw new InvalidResponseException("Sensor response is null");
            }
            if (!goodStatus(response.getStatus())) {
                throw new InvalidResponseException(
                        "Sensors request returned status " + Integer.valueOf(response.getStatus()));
            }

            String content = response.getContentAsString();
            Map<String, Sensor> sensorMap = gson.fromJson(content, new TypeToken<Map<String, Sensor>>() {
            }.getType());
            if (sensorMap == null) {
                throw new InvalidResponseException("Sensor map is null");
            }
            logger.trace("Received map of {} sensors", sensorMap.size());

            for (Sensor sensor : sensorMap.values()) {
                String deviceId = sensor.deviceId;
                logger.trace("Sensor device id: {}", deviceId);
                if (deviceId != null) {
                    notifyChildHandlers(deviceId, null, sensor);
                    handleDiscovery(sensorMap);
                }
            }
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            logger.debug("Exception requesting sensor data: {}", e.getMessage());
        } catch (InvalidResponseException | JsonParseException e) {
            logger.debug("Exception receiving sensor data: {}", e.getMessage());
        }
    }

    /**
     * Poll for sample data
     */
    private void pollSamples() {
        logger.trace("Polling samples");
        SamplesRequest requestDTO = new SamplesRequest();
        requestDTO.limit = 1;
        requestDTO.measures = SamplesRequest.ALL_MEASUREMENTS;

        try {
            ContentResponse response = sendRequest(API_URL + Endpoint.SAMPLES, gson.toJson(requestDTO), accessToken);
            if (response == null) {
                throw new InvalidResponseException("Sample response is null");
            }
            if (!goodStatus(response.getStatus())) {
                throw new InvalidResponseException(
                        "Sample request returned status " + Integer.valueOf(response.getStatus()));
            }

            String content = response.getContentAsString();
            SamplesResponse samples = gson.fromJson(content, SamplesResponse.class);
            if (samples == null) {
                throw new InvalidResponseException("Samples is null");
            }
            logger.trace("Received {} samples.", samples.totalSamples);

            Map<String, Sample[]> sensors = samples.sensors;
            if (sensors != null) {
                for (Map.Entry<String, Sample[]> entry : sensors.entrySet()) {
                    logger.trace("Sample: id: {} time: {}", entry.getKey(), entry.getValue()[0].observed);
                    notifyChildHandlers(getDeviceId(entry.getKey()), entry.getValue()[0], null);
                }
            }
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            logger.debug("Exception requesting sample data: {}", e.getMessage());
        } catch (InvalidResponseException | JsonParseException e) {
            logger.debug("Exception receiving sample data: {}", e.getMessage());
        }
    }

    private void notifyChildHandlers(String deviceId, @Nullable Sample sample, @Nullable Sensor sensor) {
        for (Thing thing : getThing().getThings()) {
            SensorHandler handler = (SensorHandler) thing.getHandler();
            if (handler != null) {
                handler.handleUpdate(deviceId, sample, sensor);
            }
        }
    }

    private void handleDiscovery(Map<String, Sensor> sensorMap) {
        SensorPushDiscoveryService discoveryService = this.discoveryService;
        for (Sensor sensor : sensorMap.values()) {
            if (discoveryService != null) {
                discoveryService.processSensor(sensor);
            }
        }
    }

    /**
     * Extracts a short deviceId from a long id string. Returns an empty string if the format of id is invalid.
     */
    private String getDeviceId(String id) {
        String[] parts = id.split("\\.");
        if (parts.length == 2) {
            return parts[0];
        } else {
            logger.debug("Invalid ID string format: {}", id);
            return "";
        }
    }

    @Override
    public synchronized void dispose() {
        logger.trace("Dispose called");
        // Stop polling job
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        accessToken = null;
        accessTokenInfo = null;
        super.dispose();
    }
}
