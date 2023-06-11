/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Actions;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ActionsCollection;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceCollection;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Light;
import org.openhab.binding.mielecloud.internal.webservice.api.json.MieleSyntaxException;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceInitializationException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceTransientException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;
import org.openhab.binding.mielecloud.internal.webservice.request.RequestFactory;
import org.openhab.binding.mielecloud.internal.webservice.request.RequestFactoryImpl;
import org.openhab.binding.mielecloud.internal.webservice.retry.AuthorizationFailedRetryStrategy;
import org.openhab.binding.mielecloud.internal.webservice.retry.NTimesRetryStrategy;
import org.openhab.binding.mielecloud.internal.webservice.retry.RetryStrategy;
import org.openhab.binding.mielecloud.internal.webservice.retry.RetryStrategyCombiner;
import org.openhab.binding.mielecloud.internal.webservice.sse.ServerSentEvent;
import org.openhab.binding.mielecloud.internal.webservice.sse.SseConnection;
import org.openhab.binding.mielecloud.internal.webservice.sse.SseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Default implementation of the {@link MieleWebservice}.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class DefaultMieleWebservice implements MieleWebservice, SseListener {
    private static final String SERVER_ADDRESS = "https://api.mcs3.miele.com";
    public static final String THIRD_PARTY_ENDPOINTS_BASENAME = SERVER_ADDRESS + "/thirdparty";
    private static final String ENDPOINT_DEVICES = SERVER_ADDRESS + "/v1/devices/";
    private static final String ENDPOINT_ACTIONS = ENDPOINT_DEVICES + "%s" + "/actions";
    private static final String ENDPOINT_LOGOUT = THIRD_PARTY_ENDPOINTS_BASENAME + "/logout";
    private static final String ENDPOINT_ALL_SSE_EVENTS = ENDPOINT_DEVICES + "all/events";

    private static final String SSE_EVENT_TYPE_DEVICES = "devices";
    public static final String SSE_EVENT_TYPE_ACTIONS = "actions";

    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(DefaultMieleWebservice.class);

    private Optional<String> accessToken = Optional.empty();
    private final RequestFactory requestFactory;

    private final DeviceStateDispatcher deviceStateDispatcher;
    private final List<ConnectionStatusListener> connectionStatusListeners = new ArrayList<>();

    private final RetryStrategy retryStrategy;

    private final SseConnection sseConnection;

    /**
     * Creates a new {@link DefaultMieleWebservice} with default retry configuration which is to retry failed operations
     * once on a transient error. In case an authorization error occurs, a new access token is requested and a retry of
     * the failed request is executed.
     *
     * @param configuration The configuration holding all parameters for constructing the instance.
     * @throws MieleWebserviceInitializationException if initializing the HTTP client fails.
     */
    public DefaultMieleWebservice(MieleWebserviceConfiguration configuration) {
        this(new RequestFactoryImpl(configuration.getHttpClientFactory(), configuration.getLanguageProvider()),
                new RetryStrategyCombiner(new NTimesRetryStrategy(1),
                        new AuthorizationFailedRetryStrategy(configuration.getTokenRefresher(),
                                configuration.getServiceHandle())),
                new DeviceStateDispatcher(), configuration.getScheduler());
    }

    /**
     * This constructor only exists for testing.
     */
    DefaultMieleWebservice(RequestFactory requestFactory, RetryStrategy retryStrategy,
            DeviceStateDispatcher deviceStateDispatcher, ScheduledExecutorService scheduler) {
        this.requestFactory = requestFactory;
        this.retryStrategy = retryStrategy;
        this.deviceStateDispatcher = deviceStateDispatcher;
        this.sseConnection = new SseConnection(ENDPOINT_ALL_SSE_EVENTS, this::createSseRequest, scheduler);
        this.sseConnection.addSseListener(this);
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = Optional.of(accessToken);
    }

    @Override
    public boolean hasAccessToken() {
        return accessToken.isPresent();
    }

    @Override
    public synchronized void connectSse() {
        sseConnection.connect();
    }

    @Override
    public synchronized void disconnectSse() {
        sseConnection.disconnect();
    }

    @Nullable
    private Request createSseRequest(String endpoint) {
        Optional<String> accessToken = this.accessToken;
        if (!accessToken.isPresent()) {
            logger.warn("No access token present.");
            return null;
        }

        return requestFactory.createSseRequest(endpoint, accessToken.get());
    }

    @Override
    public void onServerSentEvent(ServerSentEvent event) {
        fireConnectionAlive();

        try {
            switch (event.getEvent()) {
                case SSE_EVENT_TYPE_ACTIONS:
                    // We could use the actions payload here directly BUT as of March 2022 there is a bug in the cloud
                    // that makes the payload differ from the actual values. The /actions endpoint delivers the correct
                    // data. Thus, receiving an actions update via SSE is used as a trigger to fetch the actions state
                    // from the /actions endpoint as a workaround. See
                    // https://github.com/openhab/openhab-addons/issues/12500
                    for (String deviceIdentifier : ActionsCollection.fromJson(event.getData()).getDeviceIdentifiers()) {
                        try {
                            fetchActions(deviceIdentifier);
                        } catch (MieleWebserviceException e) {
                            logger.warn("Failed to fetch action state for device {}: {} - {}", deviceIdentifier,
                                    e.getConnectionError(), e.getMessage());
                        } catch (AuthorizationFailedException e) {
                            logger.warn("Failed to fetch action state for device {}: {}", deviceIdentifier,
                                    e.getMessage());
                            onConnectionError(ConnectionError.AUTHORIZATION_FAILED, 0);
                            break;
                        } catch (TooManyRequestsException e) {
                            logger.warn("Failed to fetch action state for device {}: {}", deviceIdentifier,
                                    e.getMessage());
                            break;
                        }
                    }
                    break;

                case SSE_EVENT_TYPE_DEVICES:
                    deviceStateDispatcher.dispatchDeviceStateUpdates(DeviceCollection.fromJson(event.getData()));
                    break;
            }
        } catch (MieleSyntaxException e) {
            logger.warn("SSE payload is not valid Json: {}", event.getData());
        }
    }

    private void fireConnectionAlive() {
        connectionStatusListeners.forEach(ConnectionStatusListener::onConnectionAlive);
    }

    @Override
    public void onConnectionError(ConnectionError connectionError, int failedReconnectAttempts) {
        connectionStatusListeners.forEach(l -> l.onConnectionError(connectionError, failedReconnectAttempts));
    }

    @Override
    public void fetchActions(String deviceId) {
        Actions actions = retryStrategy.performRetryableOperation(() -> getActions(deviceId),
                e -> logger.warn("Cannot poll action state: {}. Retrying...", e.getMessage()));
        if (actions != null) {
            deviceStateDispatcher.dispatchActionStateUpdates(deviceId, actions);
        } else {
            logger.warn("Cannot poll action state. Response is missing actions.");
        }
    }

    @Override
    public void putProcessAction(String deviceId, ProcessAction processAction) {
        if (processAction.equals(ProcessAction.UNKNOWN)) {
            throw new IllegalArgumentException("Process action must not be UNKNOWN.");
        }

        String formattedProcessAction = GSON.toJson(processAction, ProcessAction.class);
        formattedProcessAction = formattedProcessAction.substring(1, formattedProcessAction.length() - 1);
        String json = "{\"processAction\":" + formattedProcessAction + "}";

        logger.debug("Activate process action {} of Miele device {}", processAction.toString(), deviceId);
        putActions(deviceId, json);
    }

    @Override
    public void putLight(String deviceId, boolean enabled) {
        Light light = enabled ? Light.ENABLE : Light.DISABLE;
        String json = "{\"light\":" + light.format() + "}";

        logger.debug("Set light of Miele device {} to {}", deviceId, enabled);
        putActions(deviceId, json);
    }

    @Override
    public void putPowerState(String deviceId, boolean enabled) {
        String action = enabled ? "powerOn" : "powerOff";
        String json = "{\"" + action + "\":true}";

        logger.debug("Set power state of Miele device {} to {}", deviceId, action);
        putActions(deviceId, json);
    }

    @Override
    public void putProgram(String deviceId, long programId) {
        String json = "{\"programId\":" + programId + "}";

        logger.debug("Activate program with ID {} of Miele device {}", programId, deviceId);
        putActions(deviceId, json);
    }

    @Override
    public void logout() {
        Optional<String> accessToken = this.accessToken;
        if (!accessToken.isPresent()) {
            logger.debug("No access token present.");
            return;
        }

        try {
            logger.debug("Invalidating Miele webservice access token.");
            Request request = requestFactory.createPostRequest(ENDPOINT_LOGOUT, accessToken.get());
            this.accessToken = Optional.empty();
            sendRequest(request);
        } catch (MieleWebserviceTransientException e) {
            throw new MieleWebserviceException("Transient error occurred during logout.", e, e.getConnectionError());
        }
    }

    /**
     * Sends the given request and wraps the possible exceptions in Miele exception types.
     *
     * @param request The {@link Request} to send.
     * @return The obtained {@link ContentResponse}.
     * @throws MieleWebserviceException if an irrecoverable error occurred.
     * @throws MieleWebserviceTransientException if a recoverable error occurred.
     */
    private ContentResponse sendRequest(Request request) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Send {} request to Miele webservice on uri {}",
                        Optional.ofNullable(request).map(Request::getMethod).orElse("null"),
                        Optional.ofNullable(request).map(Request::getURI).map(URI::toString).orElse("null"));
            }

            ContentResponse response = request.send();
            logger.debug("Received response with status code {}", response.getStatus());
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MieleWebserviceException("Interrupted.", e, ConnectionError.REQUEST_INTERRUPTED);
        } catch (TimeoutException e) {
            throw new MieleWebserviceTransientException("Request timed out.", e, ConnectionError.TIMEOUT);
        } catch (ExecutionException e) {
            throw new MieleWebserviceException("Request execution failed.", e,
                    ConnectionError.REQUEST_EXECUTION_FAILED);
        }
    }

    /**
     * Gets all available device actions.
     *
     * @param deviceId The unique device ID.
     *
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws MieleWebserviceTransientException if an error occurs during webservice requests or content parsing that
     *             is recoverable by retrying the operation.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    private Actions getActions(String deviceId) {
        Optional<String> accessToken = this.accessToken;
        if (!accessToken.isPresent()) {
            throw new MieleWebserviceException("Missing access token.", ConnectionError.AUTHORIZATION_FAILED);
        }

        try {
            logger.debug("Fetch action state description for Miele device {}", deviceId);
            Request request = requestFactory.createGetRequest(String.format(ENDPOINT_ACTIONS, deviceId),
                    accessToken.get());
            ContentResponse response = sendRequest(request);
            HttpUtil.checkHttpSuccess(response);
            Actions actions = GSON.fromJson(response.getContentAsString(), Actions.class);
            if (actions == null) {
                throw new MieleWebserviceTransientException("Failed to parse response message.",
                        ConnectionError.RESPONSE_MALFORMED);
            }
            return actions;
        } catch (JsonSyntaxException e) {
            throw new MieleWebserviceTransientException("Failed to parse response message.", e,
                    ConnectionError.RESPONSE_MALFORMED);
        }
    }

    /**
     * Performs a PUT request to the actions endpoint for the specified device.
     *
     * @param deviceId The ID of the device to PUT for.
     * @param json The Json body to send with the request.
     * @throws MieleWebserviceException if an error occurs during webservice requests or content parsing.
     * @throws MieleWebserviceTransientException if an error occurs during webservice requests or content parsing that
     *             is recoverable by retrying the operation.
     * @throws AuthorizationFailedException if the authorization against the webservice failed.
     * @throws TooManyRequestsException if too many requests have been made against the webservice recently.
     */
    private void putActions(String deviceId, String json) {
        retryStrategy.performRetryableOperation(() -> {
            Optional<String> accessToken = this.accessToken;
            if (!accessToken.isPresent()) {
                throw new MieleWebserviceException("Missing access token.", ConnectionError.AUTHORIZATION_FAILED);
            }

            Request request = requestFactory.createPutRequest(String.format(ENDPOINT_ACTIONS, deviceId),
                    accessToken.get(), json);
            ContentResponse response = sendRequest(request);
            HttpUtil.checkHttpSuccess(response);
        }, e -> {
            logger.warn("Failed to perform PUT request: {}. Retrying...", e.getMessage());
        });
    }

    @Override
    public void dispatchDeviceState(String deviceIdentifier) {
        deviceStateDispatcher.dispatchDeviceState(deviceIdentifier);
    }

    @Override
    public void addDeviceStateListener(DeviceStateListener listener) {
        deviceStateDispatcher.addListener(listener);
    }

    @Override
    public void removeDeviceStateListener(DeviceStateListener listener) {
        deviceStateDispatcher.removeListener(listener);
    }

    @Override
    public void addConnectionStatusListener(ConnectionStatusListener listener) {
        connectionStatusListeners.add(listener);
    }

    @Override
    public void removeConnectionStatusListener(ConnectionStatusListener listener) {
        connectionStatusListeners.remove(listener);
    }

    @Override
    public void close() throws Exception {
        requestFactory.close();
    }
}
