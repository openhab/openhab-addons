/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

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
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.openhab.binding.boschshc.internal.services.JsonRestExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    }

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    @NonNullByDefault({})
    private BoschHttpClient httpClient;

    private @Nullable String subscriptionId;

    @NonNullByDefault({})
    private BoschSHCBridgeConfiguration config;

    @Override
    public void initialize() {
        // Read configuration
        this.config = getConfigAs(BoschSHCBridgeConfiguration.class);

        // Instantiate HttpClient with the SslContextFactory
        this.httpClient = new BoschHttpClient(config.ipAddress, config.password,
                // prepare SSL key and certificates
                new BoschSslUtil(config.password).getSslContextFactory());

        // Initialize bridge in the background.
        scheduler.execute(() -> {

            logger.info("Starting Bosch SHC Bridge");

            try {
                try {
                    httpClient.start();
                } catch (Exception e) {
                    logger.warn("Failed to start Bosch SHC Bridge: {}", e);
                }

                logger.debug("Initializing Bosch SHC Bridge: {} - HTTP client is: {} - version: 2020-04-05",
                        config.ipAddress, this.httpClient);

                // check access and pair if necessary
                if (!this.httpClient.isAccessPossible()) {
                    // update status already if access is not possible
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                            "@text/offline.conf-error-pairing");
                    this.httpClient.checkAccessAndPairIfNecessary();
                }

                Boolean thingReachable = true;
                thingReachable &= this.getRooms();
                thingReachable &= this.getDevices();

                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);

                    // Start long polling to receive updates from Bosch SHC.
                    this.longPoll();

                } else {
                    // TODO add offline conf-error description
                    updateStatus(ThingStatus.OFFLINE);
                }

            } catch (PairingFailedException e) {
                logger.debug("Failed pairing with Bosch Smart Home Controller: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-pairing");
            } catch (Exception e) {
                logger.warn("Failed to initialize Bosch SHC: {}", e);
                // TODO add offline conf-error description
                updateStatus(ThingStatus.OFFLINE);
            }

        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command on bridge: {}", config.ipAddress);
    }

    /**
     * Get a list of connected devices from the Smart-Home Controller
     */
    private Boolean getDevices() {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.debug("Sending http request to Bosch to request clients: {}", config.ipAddress);
                contentResponse = httpClient.newRequest("https://" + config.ipAddress + ":8444/smarthome/devices")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.debug("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
                Type collectionType = new TypeToken<ArrayList<Device>>() {
                }.getType();
                ArrayList<Device> devices = gson.fromJson(content, collectionType);

                if (devices != null) {
                    for (Device d : devices) {
                        // Write found devices into openhab.log until we have implemented auto discovery
                        logger.info("Found device: name={} id={}", d.name, d.id);
                        if (d.deviceSerivceIDs != null) {
                            for (String s : d.deviceSerivceIDs) {
                                logger.info(".... service: " + s);
                            }
                        }
                    }
                }

                return true;

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
                return false;
            }
        } else {

            return false;
        }
    }

    /**
     * Subscribe to events and store the subscription ID needed for long polling
     *
     * Method is synchronous.
     */
    private void subscribe() {
        logger.debug("Sending subscribe request to Bosch");

        String[] params = { "com/bosch/sh/remote/*", null }; // TODO Not sure about the tailing null, copied
                                                             // from NodeJs
        JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/subscribe", params);

        Gson gson = new Gson();
        String str_content = gson.toJson(r);

        // XXX Maybe we should use a different httpClient here, to avoid a race with
        // concurrent use from other
        // functions.
        logger.debug("Subscribe: Sending content: {} - using httpClient {}", str_content, this.httpClient);

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

                byte[] responseContent = getContent();
                String content = new String(responseContent);

                logger.debug("Subscribe: response complete: {} - return code: {}", content,
                        result.getResponse().getStatus());

                SubscribeResult subscribeResult = gson.fromJson(content, SubscribeResult.class);
                logger.debug("Subscribe: Got subscription ID: {} {}", subscribeResult.getResult(),
                        subscribeResult.getJsonrpc());

                bridgeHandler.subscriptionId = subscribeResult.getResult();
                longPoll();
            }
        }

        this.httpClient.newRequest("https://" + config.ipAddress + ":8444/remote/json-rpc")
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST) // TODO What's this Gateway ID
                .content(new StringContentProvider(str_content)).send(new SubscribeListener(this));
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
    private void longPoll() {
        /*
         * // TODO Change hard-coded Gateway ID // TODO Change hard-coded port
         */

        String subscriptionId = this.subscriptionId;
        if (subscriptionId == null) {

            logger.debug("longPoll: Subscription outdated, requesting .. ");
            this.subscribe();
            return;
        }

        logger.debug("Sending long poll request to Bosch");

        String[] params = { subscriptionId, "20" };
        JsonRpcRequest r = new JsonRpcRequest("2.0", "RE/longPoll", params);

        Gson gson = new Gson();
        String str_content = gson.toJson(r);

        logger.debug("Sending content: {}", str_content);

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

                String content = null;

                try {
                    if (result != null && !result.isFailed()) {

                        byte[] responseContent = getContent();
                        content = new String(responseContent);

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
                                        if (baseHandler != null && BoschSHCHandler.class.isInstance(baseHandler)) {
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

                    logger.warn("Execption in long polling - error: {}", e);

                    // Timeout before retry
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException sleepError) {
                        logger.warn("Failed to sleep in longRun()");
                    }
                }

                // TODO Is this call okay? Should we use scheduler.execute instead?
                bridgeHandler.longPoll();
            }
        }

        this.httpClient.newRequest("https://" + config.ipAddress + ":8444/remote/json-rpc")
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .header("Gateway-ID", "64-DA-A0-02-14-9B").method(POST).content(new StringContentProvider(str_content))
                .send(new LongPollListener(this));
    }

    /**
     * Get a list of rooms from the Smart-Home controller
     */
    private Boolean getRooms() {
        BoschHttpClient httpClient = this.httpClient;
        if (httpClient != null) {

            ContentResponse contentResponse;
            try {
                logger.debug("Sending http request to Bosch to request rooms");
                contentResponse = httpClient.newRequest("https://" + config.ipAddress + ":8444/smarthome/rooms")
                        .header("Content-Type", "application/json").header("Accept", "application/json").method(GET)
                        .send();

                String content = contentResponse.getContentAsString();
                logger.debug("Response complete: {} - return code: {}", content, contentResponse.getStatus());

                Gson gson = new GsonBuilder().create();
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
                logger.warn("HTTP request failed: {}", e);
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
     * @param classOfT Class to convert the resulting JSON to
     */
    public <T extends @NonNull Object> @Nullable T getState(String deviceId, String stateName, Class<T> stateClass) {
        ContentResponse contentResponse;
        try {
            String url = this.createServiceUrl(stateName, deviceId);
            Request request = this.createRequest(httpClient, url, GET).header("Accept", "application/json");

            logger.debug("refreshState: Requesting \"{}\" from Bosch: {} via {}", stateName, deviceId, url);

            contentResponse = request.send();

            String content = contentResponse.getContentAsString();
            logger.debug("refreshState: Request complete: [{}] - return code: {}", content,
                    contentResponse.getStatus());

            Gson gson = new GsonBuilder().create();

            int statusCode = contentResponse.getStatus();
            if (statusCode != 200) {
                JsonRestExceptionResponse errorResponse = gson.fromJson(content, JsonRestExceptionResponse.class);
                throw new Error(MessageFormatter.arrayFormat(
                        "State request for service {} of device {} failed with status code {} and error code {}",
                        new Object[] { stateName, deviceId, errorResponse.statusCode, errorResponse.errorCode })
                        .getMessage());
            }

            T state = gson.fromJson(content, stateClass);
            return state;

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("refreshState: HTTP request failed: {}", e);
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
        // Create request
        String url = this.createServiceUrl(serviceName, deviceId);
        Request request = this.createRequest(httpClient, url, PUT, state);

        // Send request
        try {
            Response response = request.send();
            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("HTTP request failed: {}", e);
            return null;
        }
    }

    /*
     * TODO: The only place from which we currently send updates is the PowerSwitch,
     * might have to extend this over time if we want to enable the alarm system
     * etc.
     */
    public void updateSwitchState(Thing thing, String command) {

        BoschSHCHandler handler = (BoschSHCHandler) thing.getHandler();
        if (handler != null) {
            ContentResponse contentResponse;
            try {

                String boschID = handler.getBoschID();
                logger.debug("Sending update request to Bosch device {}: update: {}", boschID, command);

                // PUT request
                // ----------------------------------------------------------------------------------

                // From:
                // https://github.com/philbuettner/bosch-shc-api-docs/blob/90913cc8a6fe5f322c0d819d269566e8e3708080/postman/Bosch%20Smart%20Home%20v0.3.postman_collection.json#L949
                // TODO This should be different for other kinds of devices.
                PowerSwitchStateUpdate state = new PowerSwitchStateUpdate("powerSwitchState", command);

                Gson gson = new Gson();
                String str_content = gson.toJson(state);

                // hdm:HomeMaticIP:3014F711A0001916D859A8A9
                logger.warn("Sending content: {}", str_content);

                // TODO Path should be different for other kinds of device updates
                contentResponse = httpClient
                        .newRequest("https://" + config.ipAddress + ":8444/smarthome/devices/" + boschID
                                + "/services/PowerSwitch/state")
                        .header("Content-Type", "application/json").header("Accept", "application/json")
                        .header("Gateway-ID", "64-DA-A0-02-14-9B").method(PUT)
                        .content(new StringContentProvider(str_content)).send();

                String content = contentResponse.getContentAsString();
                logger.debug("Response complete: [{}] - return code: {}", content, contentResponse.getStatus());

            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("HTTP request failed: {}", e);
            }
        }
    }

    private String createServiceUrl(String serviceName, String deviceId) {
        return "https://" + config.ipAddress + ":8444/smarthome/devices/" + deviceId + "/services/" + serviceName
                + "/state";
    }

    private Request createRequest(BoschHttpClient httpClient, String url, HttpMethod method) {
        return this.createRequest(httpClient, url, method, null);
    }

    private Request createRequest(BoschHttpClient httpClient, String url, HttpMethod method, @Nullable Object content) {
        Gson gson = new Gson();
        Request request = httpClient.newRequest(url).method(method).header("Content-Type", "application/json");
        if (content != null) {
            String body = gson.toJson(content);
            request = request.content(new StringContentProvider(body));
        }
        return request;
    }
}
