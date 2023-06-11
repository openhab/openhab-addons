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
package org.openhab.binding.ambientweather.internal.handler;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.openhab.binding.ambientweather.internal.model.DeviceJson;
import org.openhab.binding.ambientweather.internal.model.EventDataGenericJson;
import org.openhab.binding.ambientweather.internal.model.EventSubscribedJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * The {@link AmbientWeatherEventListener} is responsible for establishing
 * a socket.io connection with ambientweather.net, subscribing/unsubscribing
 * to data events, receiving data events through the real-time socket.io API,
 * and for routing the data events to a weather station thing handler for processing.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AmbientWeatherEventListener {
    // URL used to get the realtime event stream
    private static final String REALTIME_URL = "https://api.ambientweather.net/?api=1&applicationKey=%APPKEY%";

    // JSON used to subscribe or unsubscribe from weather data events
    private static final String SUB_UNSUB_JSON = "{ apiKeys: [ '%APIKEY%' ] }";

    private final Logger logger = LoggerFactory.getLogger(AmbientWeatherEventListener.class);

    // Maintain mapping of handler and weather station MAC address
    private final Map<AmbientWeatherStationHandler, String> handlers = new ConcurrentHashMap<>();

    private String apiKey;

    private String applicationKey;

    // Socket.io socket used to access Ambient Weather real-time API
    private Socket socket;

    // Identifies if connected to real-time API
    private boolean isConnected;

    private Gson gson;

    private AmbientWeatherBridgeHandler bridgeHandler;

    public AmbientWeatherEventListener(AmbientWeatherBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
    }

    /*
     * Update the list of handlers to include 'handler' at MAC address 'macAddress'
     */
    public void addHandler(AmbientWeatherStationHandler handler, String macAddress) {
        logger.debug("Listener: Add station handler to list: {}", handler.getThing().getUID());
        handlers.put(handler, macAddress);
    }

    /*
     * Update the list of handlers to remove 'handler' at MAC address 'macAddress'
     */
    public void removeHandler(AmbientWeatherStationHandler handler, String macAddress) {
        logger.debug("Listener: Remove station handler from list: {}", handler.getThing().getUID());
        handlers.remove(handler);
    }

    /*
     * Send weather station information (station name and location) to the
     * thing handler associated with the MAC address
     */
    private void sendStationInfoToHandler(String macAddress, String name, String location) {
        AmbientWeatherStationHandler handler = getHandler(macAddress);
        if (handler != null) {
            handler.handleInfoEvent(macAddress, name, location);
        }
    }

    /*
     * Send an Ambient Weather data event to the station thing handler associated
     * with the MAC address
     */
    private void sendWeatherDataToHandler(String macAddress, String jsonData) {
        AmbientWeatherStationHandler handler = getHandler(macAddress);
        if (handler != null) {
            handler.handleWeatherDataEvent(jsonData);
        }
    }

    private AmbientWeatherStationHandler getHandler(String macAddress) {
        logger.debug("Listener: Search for MAC {} in handlers list with {} entries: {}", macAddress, handlers.size(),
                Arrays.asList(handlers.values()));
        for (Map.Entry<AmbientWeatherStationHandler, String> device : handlers.entrySet()) {
            AmbientWeatherStationHandler handler = device.getKey();
            String mac = device.getValue();
            if (mac.equalsIgnoreCase(macAddress)) {
                logger.debug("Listener: Found handler for {} with MAC {}", handler.getThing().getUID(), macAddress);
                return handler;
            }
        }
        logger.debug("Listener: No handler available for event for station with MAC {}", macAddress);
        return null;
    }

    /*
     * Start the event listener for the Ambient Weather real-time API
     */
    public void start(String applicationKey, String apiKey, Gson gson) {
        logger.debug("Listener: Event listener starting");
        this.applicationKey = applicationKey;
        this.apiKey = apiKey;
        this.gson = gson;
        connectToService();
    }

    /*
     * Stop the event listener for the Ambient Weather real-time API.
     */
    public void stop() {
        logger.debug("Listener: Event listener stopping");
        sendUnsubscribe();
        disconnectFromService();
        handlers.clear();
    }

    /*
     * Initiate the connection to the Ambient Weather real-time API
     */
    private synchronized void connectToService() {
        final String url = REALTIME_URL.replace("%APPKEY%", applicationKey);
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.transports = new String[] { "websocket" };
            socket = IO.socket(url, options);
        } catch (URISyntaxException e) {
            logger.info("Listener: URISyntaxException getting IO socket: {}", e.getMessage());
            return;
        }
        socket.on(Socket.EVENT_CONNECT, onEventConnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onEventConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onEventConnectTimeout);
        socket.on(Socket.EVENT_DISCONNECT, onEventDisconnect);
        socket.on(Socket.EVENT_RECONNECT, onEventReconnect);
        socket.on("data", onData);
        socket.on("subscribed", onSubscribed);

        logger.debug("Listener: Opening connection to ambient weather service with socket {}", socket.toString());
        socket.connect();
    }

    /*
     * Initiate a disconnect from the Ambient Weather real-time API
     */
    private void disconnectFromService() {
        if (socket != null) {
            logger.debug("Listener: Disconnecting socket and removing event listeners for {}", socket.toString());
            socket.disconnect();
            socket.off();
            socket = null;
        }
    }

    /*
     * Attempt to reconnect to the Ambient Weather real-time API
     */
    private void reconnectToService() {
        logger.debug("Listener: Attempting to reconnect to service");
        disconnectFromService();
        connectToService();
    }

    /*
     * Socket.io event callbacks
     */
    private Emitter.Listener onEventConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            logger.debug("Listener: Connected! Subscribe to weather data events");
            isConnected = true;
            bridgeHandler.markBridgeOnline();
            sendSubscribe();
        }
    };

    private Emitter.Listener onEventDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            logger.debug("Listener: Disconnected from the ambient weather service)");
            handleError(Socket.EVENT_DISCONNECT, args);
            isConnected = false;
        }
    };

    private Emitter.Listener onEventConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handleError(Socket.EVENT_CONNECT_ERROR, args);
        }
    };

    private Emitter.Listener onEventConnectTimeout = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handleError(Socket.EVENT_CONNECT_TIMEOUT, args);
        }
    };

    private Emitter.Listener onEventReconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            logger.debug("Listener: Received reconnect event from service");
            reconnectToService();
        }
    };

    private Emitter.Listener onSubscribed = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            logger.debug("Listener: Received SUBSCRIBED event");
            // Got a response to a subscribe or unsubscribe command
            if (args.length > 0) {
                handleSubscribed(((JSONObject) args[0]).toString());
            }
        }
    };

    private Emitter.Listener onData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            logger.debug("Listener: Received DATA event");
            // Got a weather data event from ambientweather.net
            if (args.length > 0) {
                handleData(((JSONObject) args[0]).toString());
            }
        }
    };

    /*
     * Handlers for events
     */
    private void handleError(String event, Object... args) {
        String reason = "Unknown";
        if (args.length > 0) {
            if (args[0] instanceof String) {
                reason = (String) args[0];
            } else if (args[0] instanceof Exception) {
                reason = String.format("Exception=%s Message=%s", args[0].getClass(),
                        ((Exception) args[0]).getMessage());
            }
        }
        logger.debug("Listener: Received socket event: {}, Reason: {}", event, reason);
        bridgeHandler.markBridgeOffline(reason);
    }

    /*
     * Parse the subscribed event, then tell the handler to update the station info channels.
     */
    private void handleSubscribed(String jsonData) {
        logger.debug("Listener: subscribed={}", jsonData);
        // Extract the station names and locations and give to handlers
        try {
            EventSubscribedJson subscribed = gson.fromJson(jsonData, EventSubscribedJson.class);
            if (subscribed.invalidApiKeys != null) {
                logger.info("Listener: Invalid keys!! invalidApiKeys={}", subscribed.invalidApiKeys);
                bridgeHandler.markBridgeOffline("Invalid API keys");
                return;
            }

            if (subscribed.devices != null && subscribed.devices instanceof ArrayList) {
                // Convert the ArrayList back to JSON, then parse it
                String innerJson = gson.toJson(subscribed.devices);
                DeviceJson[] stations = gson.fromJson(innerJson, DeviceJson[].class);

                // Inform handlers of their name and location
                for (DeviceJson station : stations) {
                    logger.debug("Listener: Subscribed event has station: name = {}, location = {}, MAC = {}",
                            station.info.name, station.info.location, station.macAddress);
                    sendStationInfoToHandler(station.macAddress, station.info.name, station.info.location);
                }
            }
            if (subscribed.isMethodSubscribe()) {
                logger.debug("Listener: Subscribed to data events. Waiting for data...");
            } else if (subscribed.isMethodUnsubscribe()) {
                logger.debug("Listener: Unsubscribed from data events");
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Listener: Exception parsing subscribed.devices: {}", e.getMessage());
        }
    }

    /*
     * Parse the weather data event, then send to handler to update the channels
     */
    private synchronized void handleData(String jsonData) {
        logger.debug("Listener: Data: {}", jsonData);
        try {
            EventDataGenericJson data = gson.fromJson(jsonData, EventDataGenericJson.class);
            String macAddress = data == null ? null : data.macAddress;
            if (macAddress != null && !macAddress.isEmpty()) {
                sendWeatherDataToHandler(macAddress, jsonData);
            }
        } catch (JsonSyntaxException e) {
            logger.info("Listener: Exception parsing subscribed event: {}", e.getMessage());
        }
    }

    /*
     * Subscribe to weather data events for stations associated with the API key
     */
    private void sendSubscribe() {
        if (apiKey == null) {
            return;
        }
        final String sub = SUB_UNSUB_JSON.replace("%APIKEY%", apiKey);
        if (isConnected && socket != null) {
            logger.debug("Listener: Sending subscribe request");
            socket.emit("subscribe", new JSONObject(sub));
        }
    }

    /*
     * Unsubscribe from weather data events for stations associated with the API key
     */
    private void sendUnsubscribe() {
        if (apiKey == null) {
            return;
        }
        final String unsub = SUB_UNSUB_JSON.replace("%APIKEY%", apiKey);
        if (isConnected && socket != null) {
            logger.debug("Listener: Sending unsubscribe request");
            socket.emit("unsubscribe", new JSONObject(unsub));
        }
    }

    /*
     * Resubscribe when a handler is initialized
     */
    public synchronized void resubscribe() {
        sendUnsubscribe();
        sendSubscribe();
    }
}
