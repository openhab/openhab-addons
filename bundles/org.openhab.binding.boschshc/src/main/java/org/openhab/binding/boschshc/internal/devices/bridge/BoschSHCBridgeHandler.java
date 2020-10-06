/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.eclipse.jetty.http.HttpMethod.PUT;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.*;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.dto.JsonRestExceptionResponse;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Representation of a connection with a Bosch Smart Home Controller bridge.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Gerd Zanker - added HttpClient with pairing support
 * @author Christian Oeing - refactorings of e.g. server registration
 */
@NonNullByDefault
public class BoschSHCBridgeHandler extends BaseBridgeHandler {

    public BoschSHCBridgeHandler(Bridge bridge) {
        super(bridge);

        // Read configuration
        this.config = getConfigAs(BoschSHCBridgeConfiguration.class);
    }

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    private @Nullable BoschHttpClient httpClient;

    private BoschSHCBridgeConfiguration config;

    private final Gson gson = new Gson();

    @Override
    public void initialize() {
        if (this.config.ipAddress.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No IP address set");
            return;
        }

        if (this.config.password.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No system password set");
            return;
        }

        SslContextFactory factory;
        try {
            // prepare SSL key and certificates
            factory = new BoschSslUtil(config.password).getSslContextFactory();
        } catch (PairingFailedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-ssl");
            return;
        }

        // Instantiate HttpClient with the SslContextFactory
        BoschHttpClient httpClient = this.httpClient = new BoschHttpClient(config.ipAddress, config.password, factory);

        // Initialize bridge in the background.
        scheduler.execute(() -> {
            try {
                try {
                    httpClient.start();
                } catch (Exception e) {
                    logger.warn("Failed to start Bosch SHC Bridge", e);
                }

                logger.debug("Initializing Bosch SHC Bridge: {} - HTTP client is: {} - version: 2020-04-05",
                        config.ipAddress, this.httpClient);

                // check access and pair if necessary
                if (!httpClient.isAccessPossible()) {
                    // update status already if access is not possible
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                            "@text/offline.conf-error-pairing");
                    httpClient.checkAccessAndPairIfNecessary();
                }

                boolean thingReachable = true;
                thingReachable &= this.getRooms();
                thingReachable &= this.getDevices();

                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);

                    // Subscribe to state updates.
                    try {
                        String subscriptionId = this.subscribe(httpClient);
                        if (subscriptionId != null) {
                            scheduler.execute(() -> this.longPoll(httpClient, subscriptionId));
                        } else {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                    "Subscription failed");
                        }
                    } catch (TimeoutException | ExecutionException e) {
                        logger.error("Error on subscribe request", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "Subscription failed");
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "@text/offline.not-reachable");
                }
            } catch (PairingFailedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-pairing");
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "@text/offline.interrupted");
            }

        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handle command on bridge: {}", config.ipAddress);
    }

    /**
     * Get a list of connected devices from the Smart-Home Controller
     */
    private boolean getDevices() {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            return false;
        }

        try {
            logger.debug("Sending http request to Bosch to request clients: {}", config.ipAddress);
            String url = httpClient.createSmartHomeUrl("devices");
            ContentResponse contentResponse = httpClient.createRequest(url, GET).send();

            String content = contentResponse.getContentAsString();
            logger.debug("Response complete: {} - return code: {}", content, contentResponse.getStatus());

            Type collectionType = new TypeToken<ArrayList<Device>>() {
            }.getType();
            ArrayList<Device> devices = gson.fromJson(content, collectionType);

            if (devices != null) {
                for (Device d : devices) {
                    // Write found devices into openhab.log until we have implemented auto discovery
                    logger.info("Found device: name={} id={}", d.name, d.id);
                    if (d.deviceSerivceIDs != null) {
                        for (String s : d.deviceSerivceIDs) {
                            logger.info(".... service: {}", s);
                        }
                    }
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("HTTP request failed with exception {}", e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Subscribe to events and store the subscription ID needed for long polling
     *
     * Method is synchronous.
     * 
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * 
     * @return Subscription id
     */
    private String subscribe(BoschHttpClient httpClient)
            throws InterruptedException, TimeoutException, ExecutionException {
        String url = httpClient.createUrl("remote/json-rpc");
        JsonRpcRequest request = new JsonRpcRequest("2.0", "RE/subscribe",
                new String[] { "com/bosch/sh/remote/*", null });
        logger.debug("Subscribe: Sending request: {} - using httpClient {}", gson.toJson(request), httpClient);
        Request httpRequest = httpClient.createRequest(url, POST, request);
        SubscribeResult response = httpClient.sendRequest(httpRequest, SubscribeResult.class);

        logger.debug("Subscribe: Got subscription ID: {} {}", response.getResult(), response.getJsonrpc());
        String subscriptionId = response.getResult();
        return subscriptionId;
    }

    /**
     * Start long polling the home controller. Once a long poll resolves, a new one is started.
     */
    private void longPoll(BoschHttpClient httpClient, String subscriptionId) {
        logger.debug("Sending long poll request to Bosch");

        JsonRpcRequest requestContent = new JsonRpcRequest("2.0", "RE/longPoll", new String[] { subscriptionId, "20" });
        String url = httpClient.createUrl("remote/json-rpc");
        Request request = httpClient.createRequest(url, POST, requestContent);
        request.send(result -> {
            int delayTillNextRun = 0;
            try {
                Response response = result != null ? result.getResponse() : null;
                if (result != null && !result.isFailed() && response instanceof ContentResponse) {
                    ContentResponse contentResponse = (ContentResponse) response;
                    String content = contentResponse.getContentAsString();

                    logger.debug("Response complete: {} - return code: {}", content, result.getResponse().getStatus());

                    LongPollResult parsed = gson.fromJson(content, LongPollResult.class);
                    if (parsed.result != null) {
                        for (DeviceStatusUpdate update : parsed.result) {
                            if (update != null && update.state != null) {
                                logger.debug("Got update for {}", update.deviceId);

                                boolean handled = false;

                                Bridge bridge = this.getThing();
                                for (Thing childThing : bridge.getThings()) {
                                    // All children of this should implement BoschSHCHandler
                                    ThingHandler baseHandler = childThing.getHandler();
                                    if (baseHandler != null && baseHandler instanceof BoschSHCHandler) {
                                        BoschSHCHandler handler = (BoschSHCHandler) baseHandler;
                                        String deviceId = handler.getBoschID();

                                        handled = true;
                                        logger.debug("Registered device: {} - looking for {}", deviceId,
                                                update.deviceId);

                                        if (deviceId != null && update.deviceId.equals(deviceId)) {
                                            logger.debug("Found child: {} - calling processUpdate with {}", handler,
                                                    update.state);
                                            handler.processUpdate(update.id, update.state);
                                        }
                                    } else {
                                        logger.warn(
                                                "longPoll: child handler for {} does not implement Bosch SHC handler",
                                                baseHandler);
                                    }
                                }

                                if (!handled) {
                                    logger.debug("Could not find a thing for device ID: {}", update.deviceId);
                                }
                            }
                        }
                    } else {
                        logger.warn("Could not parse in onComplete: {}", content);

                        // Check if we got a proper result from the SHC
                        LongPollError parsedError = gson.fromJson(content, LongPollError.class);

                        if (parsedError.error != null) {
                            logger.warn("Got error from SHC: {}", parsedError.error.hashCode());

                            if (parsedError.error.code == LongPollError.SUBSCRIPTION_INVALID) {
                                logger.warn("Subscription became invalid, subscribing again");
                                this.subscribe(httpClient);
                                return;
                            }
                        }

                        // Wait some time before trying again.
                        delayTillNextRun = 10;
                    }
                } else if (result != null && result.isFailed()) {
                    logger.warn("Failed in onComplete");
                    logger.trace("Response failed: {}, failed={}, response={}", result, result.isFailed(), response);
                }
                // else not failed, but no response
            } catch (Exception e) {
                logger.warn("Exception in long polling", e);

                // Wait some time before trying again.
                delayTillNextRun = 5;
            }

            // Schedule next run.
            scheduler.schedule(() -> this.longPoll(httpClient, subscriptionId), delayTillNextRun, TimeUnit.SECONDS);
        });
    }

    /**
     * Get a list of rooms from the Smart-Home controller
     */
    private boolean getRooms() {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient != null) {
            try {
                logger.debug("Sending http request to Bosch to request rooms");
                String url = httpClient.createSmartHomeUrl("rooms");
                ContentResponse contentResponse = httpClient.createRequest(url, GET).send();

                String content = contentResponse.getContentAsString();
                logger.debug("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Type collectionType = new TypeToken<ArrayList<Room>>() {
                }.getType();

                ArrayList<Room> rooms = gson.fromJson(content, collectionType);

                if (rooms != null) {
                    for (Room r : rooms) {
                        logger.info("Found room: {}", r.name);
                    }
                }

                return true;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Query the Bosch Smart Home Controller for the state of the given thing.
     *
     * @param deviceId Id of device to get state for
     * @param stateName Name of the state to query
     * @param stateClass Class to convert the resulting JSON to
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws BoschSHCException
     */
    public <T extends BoschSHCServiceState> @Nullable T getState(String deviceId, String stateName, Class<T> stateClass)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            logger.warn("HttpClient not initialized");
            return null;
        }

        String url = httpClient.createServiceUrl(stateName, deviceId);
        Request request = httpClient.createRequest(url, GET).header("Accept", "application/json");

        logger.debug("refreshState: Requesting \"{}\" from Bosch: {} via {}", stateName, deviceId, url);

        ContentResponse contentResponse = request.send();

        String content = contentResponse.getContentAsString();
        logger.debug("refreshState: Request complete: [{}] - return code: {}", content, contentResponse.getStatus());

        int statusCode = contentResponse.getStatus();
        if (statusCode != 200) {
            JsonRestExceptionResponse errorResponse = gson.fromJson(content, JsonRestExceptionResponse.class);
            throw new BoschSHCException(String.format(
                    "State request for service {} of device {} failed with status code {} and error code {}", stateName,
                    deviceId, errorResponse.statusCode, errorResponse.errorCode));
        }

        T state = gson.fromJson(content, stateClass);
        return state;
    }

    /**
     * Sends a state change for a device to the controller
     * 
     * @param deviceId Id of device to change state for
     * @param serviceName Name of service of device to change state for
     * @param state New state data to set for service
     * 
     * @return Response of request
     */
    public <T extends BoschSHCServiceState> @Nullable Response putState(String deviceId, String serviceName, T state) {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            logger.warn("HttpClient not initialized");
            return null;
        }

        // Create request
        String url = httpClient.createServiceUrl(serviceName, deviceId);
        Request request = httpClient.createRequest(url, PUT, state);

        // Send request
        try {
            Response response = request.send();
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("HTTP request failed", e);
            return null;
        }
    }
}
