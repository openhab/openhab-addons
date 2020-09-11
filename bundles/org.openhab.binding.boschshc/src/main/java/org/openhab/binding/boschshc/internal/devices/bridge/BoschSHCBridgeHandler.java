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
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.*;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.openhab.binding.boschshc.internal.services.JsonRestExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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

    private @Nullable String subscriptionId;

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

        // Instantiate HttpClient with the SslContextFactory
        BoschHttpClient httpClient = this.httpClient = new BoschHttpClient(config.ipAddress, config.password,
                // prepare SSL key and certificates
                new BoschSslUtil(config.password).getSslContextFactory());

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

                    // Start long polling to receive updates from Bosch SHC.
                    this.longPoll(httpClient);

                } else {
                    // TODO add offline conf-error description
                    updateStatus(ThingStatus.OFFLINE);
                }

            } catch (PairingFailedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-pairing");
            } catch (InterruptedException e) {
                // TODO add offline conf-error description
                updateStatus(ThingStatus.OFFLINE);
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
     */
    private void subscribe(BoschHttpClient httpClient) {
        logger.debug("Sending subscribe request to Bosch");

        String[] params = { "com/bosch/sh/remote/*", null }; // TODO Not sure about the tailing null, copied
                                                             // from NodeJs
        JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/subscribe", params);

        // XXX Maybe we should use a different httpClient here, to avoid a race with
        // concurrent use from other
        // functions.
        logger.debug("Subscribe: Sending content: {} - using httpClient {}", gson.toJson(r), httpClient);

        class SubscribeListener extends BufferingResponseListener {
            private BoschSHCBridgeHandler bridgeHandler;

            public SubscribeListener(BoschSHCBridgeHandler bridgeHandler) {

                super();
                this.bridgeHandler = bridgeHandler;
            }

            @Override
            public void onComplete(@Nullable Result result) {
                if (result == null) {
                    logger.error("Subscribe: Received no result on completion");
                    return;
                }

                // Seems like this should yield something like:
                // content: [ [ '{"result":"e71k823d0-16","jsonrpc":"2.0"}\n' ] ]

                // The key can then be used later for longPoll like this:
                // body: [ [
                // '{"jsonrpc":"2.0","method":"RE/longPoll","params":["e71k823d0-16",20]}' ] ]

                String content = getContentAsString();

                logger.debug("Subscribe: response complete: {} - return code: {}", content,
                        result.getResponse().getStatus());

                SubscribeResult subscribeResult = gson.fromJson(content, SubscribeResult.class);
                logger.debug("Subscribe: Got subscription ID: {} {}", subscribeResult.getResult(),
                        subscribeResult.getJsonrpc());

                bridgeHandler.subscriptionId = subscribeResult.getResult();
                longPoll(httpClient);
            }
        }

        String url = httpClient.createUrl("remote/json-rpc");
        httpClient.createRequest(url, POST, r).send(new SubscribeListener(this));
    }

    /**
     * Long polling
     *
     * TODO Do we need to protect against concurrent execution of this method via
     * locks etc?
     *
     * If no subscription ID is present, this function will first try to acquire
     * one. If that fails, it will attempt to retry after a small timeout.
     *
     * Return whether to retry getting a new subscription and restart polling.
     */
    private void longPoll(BoschHttpClient httpClient) {
        /*
         * // TODO Change hard-coded Gateway ID // TODO Change hard-coded port
         */

        String subscriptionId = this.subscriptionId;
        if (subscriptionId == null) {

            logger.debug("longPoll: Subscription outdated, requesting .. ");
            this.subscribe(httpClient);
            return;
        }

        logger.debug("Sending long poll request to Bosch");

        String[] params = { subscriptionId, "20" };
        JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/longPoll", params);

        /**
         * TODO Move this to separate file?
         */
        class LongPollListener extends BufferingResponseListener {

            private BoschSHCBridgeHandler bridgeHandler;

            public LongPollListener(BoschSHCBridgeHandler bridgeHandler) {

                super();
                this.bridgeHandler = bridgeHandler;
            }

            @Override
            public void onComplete(@Nullable Result result) {

                try {
                    if (result != null && !result.isFailed()) {
                        String content = getContentAsString();

                        logger.debug("Response complete: {} - return code: {}", content,
                                result.getResponse().getStatus());

                        LongPollResult parsed = gson.fromJson(content, LongPollResult.class);
                        if (parsed.result != null) {
                            for (DeviceStatusUpdate update : parsed.result) {

                                if (update != null && update.state != null) {

                                    logger.debug("Got update for {}", update.deviceId);

                                    Bridge bridge = bridgeHandler.getThing();
                                    boolean handled = false;

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

                                    bridgeHandler.subscriptionId = null;
                                    logger.warn("Invalidating subscription ID!");
                                }
                            }

                            // Timeout before retry
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException sleepError) {
                                logger.warn("Failed to sleep in longRun()");
                            }
                        }

                    } else {
                        logger.warn("Failed in onComplete");
                    }

                } catch (Exception e) {

                    logger.warn("Execption in long polling", e);

                    // Timeout before retry
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException sleepError) {
                        logger.warn("Failed to sleep in longRun()");
                    }
                }

                // TODO Is this call okay? Should we use scheduler.execute instead?
                bridgeHandler.longPoll(httpClient);
            }
        }

        String url = httpClient.createUrl("remote/json-rpc");
        httpClient.createRequest(url, POST, r).send(new LongPollListener(this));
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
     * @param thing Thing to query the device state for
     * @param stateName Name of the state to query
     * @param classOfT Class to convert the resulting JSON to
     */
    public <T extends @NonNull Object> @Nullable T refreshState(Thing thing, String stateName,
            Class<@NonNull T> classOfT) {
        BoschSHCHandler handler = (BoschSHCHandler) thing.getHandler();
        if (handler != null) {
            String deviceId = handler.getBoschID();
            if (deviceId != null) {
                return this.getState(deviceId, stateName, classOfT);
            }
        }
        return null;
    }

    /**
     * Query the Bosch Smart Home Controller for the state of the given thing.
     *
     * @param deviceId Id of device to get state for
     * @param stateName Name of the state to query
     * @param stateClass Class to convert the resulting JSON to
     */
    public <T extends @NonNull Object> @Nullable T getState(String deviceId, String stateName, Class<T> stateClass) {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            logger.warn("HttpClient not initialized");
            return null;
        }

        try {
            String url = httpClient.createServiceUrl(stateName, deviceId);
            Request request = httpClient.createRequest(url, GET).header("Accept", "application/json");

            logger.debug("refreshState: Requesting \"{}\" from Bosch: {} via {}", stateName, deviceId, url);

            ContentResponse contentResponse = request.send();

            String content = contentResponse.getContentAsString();
            logger.debug("refreshState: Request complete: [{}] - return code: {}", content,
                    contentResponse.getStatus());

            int statusCode = contentResponse.getStatus();
            if (statusCode != 200) {
                JsonRestExceptionResponse errorResponse = gson.fromJson(content, JsonRestExceptionResponse.class);
                throw new BoschSHCException(MessageFormatter.arrayFormat(
                        "State request for service {} of device {} failed with status code {} and error code {}",
                        new Object[] { stateName, deviceId, errorResponse.statusCode, errorResponse.errorCode })
                        .getMessage());
            }

            T state = gson.fromJson(content, stateClass);
            return state;

        } catch (InterruptedException | TimeoutException | ExecutionException | BoschSHCException e) {
            logger.warn("refreshState: HTTP request failed", e);
            return null;
        }
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
    public <T extends @NonNull Object> @Nullable Response putState(String deviceId, String serviceName, T state) {
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

    /*
     * TODO: The only place from which we currently send updates is the PowerSwitch,
     * might have to extend this over time if we want to enable the alarm system
     * etc.
     */
    public void updateSwitchState(Thing thing, String command) {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            logger.warn("HttpClient not initialized");
            return;
        }

        BoschSHCHandler handler = (BoschSHCHandler) thing.getHandler();
        if (handler != null) {
            ContentResponse contentResponse;
            try {

                String boschID = handler.getBoschID();
                if (boschID == null) {
                    logger.warn("PowerSwitch has no device id");
                    return;
                }

                logger.debug("Sending update request to Bosch device {}: update: {}", boschID, command);

                // PUT request
                // ----------------------------------------------------------------------------------

                // From:
                // https://github.com/philbuettner/bosch-shc-api-docs/blob/90913cc8a6fe5f322c0d819d269566e8e3708080/postman/Bosch%20Smart%20Home%20v0.3.postman_collection.json#L949
                // TODO This should be different for other kinds of devices.
                PowerSwitchStateUpdate state = new PowerSwitchStateUpdate("powerSwitchState", command);

                // TODO Path should be different for other kinds of device updates
                String url = httpClient.createServiceUrl("PowerSwitch", boschID);
                contentResponse = httpClient.createRequest(url, PUT, state).send();

                String responseContent = contentResponse.getContentAsString();
                logger.debug("Response complete: [{}] - return code: {}", responseContent, contentResponse.getStatus());

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed", e);
            }
        }
    }
}
