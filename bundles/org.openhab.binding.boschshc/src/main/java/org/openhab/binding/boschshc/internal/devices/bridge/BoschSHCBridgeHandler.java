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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpMethod.PUT;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
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
import org.openhab.binding.boschshc.internal.exceptions.LongPollingFailedException;
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

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    /**
     * gson instance to convert a class to json string and back.
     */
    private final Gson gson = new Gson();

    /**
     * Handler to do long polling.
     */
    private final LongPolling longPolling;

    private @Nullable BoschHttpClient httpClient;

    private @Nullable ScheduledFuture<?> scheduledPairing;

    public BoschSHCBridgeHandler(Bridge bridge) {
        super(bridge);

        this.longPolling = new LongPolling(this.scheduler, this::handleLongPollResult, this::handleLongPollFailure);
    }

    @Override
    public void initialize() {
        // Read configuration
        BoschSHCBridgeConfiguration config = getConfigAs(BoschSHCBridgeConfiguration.class);

        if (config.ipAddress.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No IP address set");
            return;
        }

        if (config.password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No system password set");
            return;
        }

        SslContextFactory factory;
        try {
            // prepare SSL key and certificates
            factory = new BoschSslUtil(config.password).getSslContextFactory();
        } catch (PairingFailedException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-ssl");
            return;
        }

        // Instantiate HttpClient with the SslContextFactory
        BoschHttpClient httpClient = this.httpClient = new BoschHttpClient(config.ipAddress, config.password, factory);

        // Start http client
        try {
            httpClient.start();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Could not create http connection to controller: %s", e.getMessage()));
            return;
        }

        // Initialize bridge in the background.
        // Start initial access the first time
        scheduleInitialAccess(httpClient);
    }

    @Override
    public void dispose() {
        // Cancel scheduled pairing.
        ScheduledFuture<?> scheduledPairing = this.scheduledPairing;
        if (scheduledPairing != null) {
            scheduledPairing.cancel(true);
            this.scheduledPairing = null;
        }

        // Stop long polling.
        this.longPolling.stop();

        BoschHttpClient httpClient = this.httpClient;
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                logger.debug("HttpClient failed on bridge disposal: {}", e.getMessage());
            }
            this.httpClient = null;
        }

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Schedule the initial access.
     * Use a delay if pairing fails and next retry is scheduled.
     */
    private void scheduleInitialAccess(BoschHttpClient httpClient) {
        this.scheduledPairing = scheduler.schedule(() -> initialAccess(httpClient), 15, TimeUnit.SECONDS);
    }

    /**
     * Execute the initial access.
     * Uses the HTTP Bosch SHC client
     * to check if access if possible
     * pairs this Bosch SHC Bridge with the SHC if necessary
     * and starts the first log poll.
     */
    private void initialAccess(BoschHttpClient httpClient) {
        logger.debug("Initializing Bosch SHC Bridge: {} - HTTP client is: {} - version: 2020-04-05", this, httpClient);

        try {
            // check access and pair if necessary
            if (!httpClient.isAccessPossible()) {
                // update status already if access is not possible
                this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                        "@text/offline.conf-error-pairing");
                if (!httpClient.doPairing()) {
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-pairing");
                }
                // restart initial access - needed also in case of successful pairing to check access again
                scheduleInitialAccess(httpClient);
            } else {
                // print rooms and devices if things are reachable
                boolean thingReachable = true;
                thingReachable &= this.getRooms();
                thingReachable &= this.getDevices();

                if (thingReachable) {
                    this.updateStatus(ThingStatus.ONLINE);

                    // Start long polling
                    try {
                        this.longPolling.start(httpClient);
                    } catch (LongPollingFailedException e) {
                        this.handleLongPollFailure(e);
                    }
                } else {
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "@text/offline.not-reachable");
                    // restart initial access
                    scheduleInitialAccess(httpClient);
                }
            }
        } catch (InterruptedException e) {
            this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                    String.format("Pairing was interrupted: %s", e.getMessage()));
        }
    }

    /**
     * Get a list of connected devices from the Smart-Home Controller
     * 
     * @throws InterruptedException
     */
    private boolean getDevices() throws InterruptedException {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            return false;
        }

        try {
            logger.debug("Sending http request to Bosch to request clients: {}", httpClient);
            String url = httpClient.getBoschSmartHomeUrl("devices");
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
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("HTTP request failed with exception {}", e.getMessage());
            return false;
        }

        return true;
    }

    private void handleLongPollResult(LongPollResult result) {
        for (DeviceStatusUpdate update : result.result) {
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
                        logger.debug("Registered device: {} - looking for {}", deviceId, update.deviceId);

                        if (deviceId != null && update.deviceId.equals(deviceId)) {
                            logger.debug("Found child: {} - calling processUpdate with {}", handler, update.state);
                            handler.processUpdate(update.id, update.state);
                        }
                    } else {
                        logger.warn("longPoll: child handler for {} does not implement Bosch SHC handler", baseHandler);
                    }
                }

                if (!handled) {
                    logger.debug("Could not find a thing for device ID: {}", update.deviceId);
                }
            }
        }
    }

    private void handleLongPollFailure(Throwable e) {
        logger.warn("Long polling failed", e);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Long polling failed");
    }

    /**
     * Get a list of rooms from the Smart-Home controller
     * 
     * @throws InterruptedException
     */
    private boolean getRooms() throws InterruptedException {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient != null) {
            try {
                logger.debug("Sending http request to Bosch to request rooms");
                String url = httpClient.getBoschSmartHomeUrl("rooms");
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
            } catch (TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e.getMessage());
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

        String url = httpClient.getServiceUrl(stateName, deviceId);
        Request request = httpClient.createRequest(url, GET).header("Accept", "application/json");

        logger.debug("refreshState: Requesting \"{}\" from Bosch: {} via {}", stateName, deviceId, url);

        ContentResponse contentResponse = request.send();

        String content = contentResponse.getContentAsString();
        logger.debug("refreshState: Request complete: [{}] - return code: {}", content, contentResponse.getStatus());

        int statusCode = contentResponse.getStatus();
        if (statusCode != 200) {
            JsonRestExceptionResponse errorResponse = gson.fromJson(content, JsonRestExceptionResponse.class);
            if (errorResponse != null) {
                throw new BoschSHCException(String.format(
                        "State request for service %s of device %s failed with status code %d and error code %s",
                        stateName, deviceId, errorResponse.statusCode, errorResponse.errorCode));
            } else {
                throw new BoschSHCException(
                        String.format("State request for service %s of device %s failed with status code %d", stateName,
                                deviceId, statusCode));
            }
        }

        @Nullable
        T state = gson.fromJson(content, stateClass);
        if (state == null) {
            throw new BoschSHCException(String.format("Received invalid, expected type %s", stateClass.getName()));
        }
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
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public <T extends BoschSHCServiceState> @Nullable Response putState(String deviceId, String serviceName, T state)
            throws InterruptedException, TimeoutException, ExecutionException {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            logger.warn("HttpClient not initialized");
            return null;
        }

        // Create request
        String url = httpClient.getServiceUrl(serviceName, deviceId);
        Request request = httpClient.createRequest(url, PUT, state);

        // Send request
        Response response = request.send();
        return response;
    }
}
