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
package org.openhab.binding.adorne.internal.hub;

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.adorne.internal.AdorneDeviceState;
import org.openhab.binding.adorne.internal.configuration.AdorneHubConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

/**
 * The {@link AdorneHubController} manages the interaction with the Adorne hub. The controller maintains a connection
 * with the Adorne Hub and listens to device changes and issues device commands. Interaction with the hub is performed
 * asynchronously through REST messages.
 *
 * @author Mark Theiding - Initial Contribution
 */
@NonNullByDefault
public class AdorneHubController {
    private Logger logger = LoggerFactory.getLogger(AdorneHubController.class);

    private static final int HUB_CONNECT_TIMEOUT = 10000;
    private static final int HUB_RECONNECT_SLEEP_MINIMUM = 1;
    private static final int HUB_RECONNECT_SLEEP_MAXIMUM = 15 * 60;

    // Hub rest commands
    private static final String HUB_REST_SET_ONOFF = "{\"ID\":%d,\"Service\":\"SetZoneProperties\",\"ZID\":%d,\"PropertyList\":{\"Power\":%b}}\0";
    private static final String HUB_REST_SET_BRIGHTNESS = "{\"ID\":%d,\"Service\":\"SetZoneProperties\",\"ZID\":%d,\"PropertyList\":{\"PowerLevel\":%d}}\0";
    private static final String HUB_REST_REQUEST_STATE = "{\"ID\":%d,\"Service\":\"ReportZoneProperties\",\"ZID\":%d}\0";
    private static final String HUB_REST_REQUEST_ZONES = "{\"ID\":%d,\"Service\":\"ListZones\"}\0";
    private static final String HUB_REST_REQUEST_MACADDRESS = "{\"ID\":%d,\"Service\":\"SystemInfo\"}\0";
    private static final String HUB_TOKEN_SERVICE = "Service";
    private static final String HUB_TOKEN_ZID = "ZID";
    private static final String HUB_TOKEN_PROPERTY_LIST = "PropertyList";
    private static final String HUB_TOKEN_DEVICE_TYPE = "DeviceType";
    private static final String HUB_TOKEN_SWITCH = "Switch";
    private static final String HUB_TOKEN_DIMMER = "Dimmer";
    private static final String HUB_TOKEN_NAME = "Name";
    private static final String HUB_TOKEN_POWER = "Power";
    private static final String HUB_TOKEN_POWER_LEVEL = "PowerLevel";
    private static final String HUB_TOKEN_MAC_ADDRESS = "MACAddress";
    private static final String HUB_TOKEN_ZONE_LIST = "ZoneList";
    private static final String HUB_SERVICE_REPORT_ZONE_PROPERTIES = "ReportZoneProperties";
    private static final String HUB_SERVICE_ZONE_PROPERTIES_CHANGED = "ZonePropertiesChanged";
    private static final String HUB_SERVICE_LIST_ZONE = "ListZones";
    private static final String HUB_SERVICE_SYSTEM_INFO = "SystemInfo";

    private @Nullable Future<?> hubController;
    private String hubHost;
    private int hubPort;
    private @Nullable Socket hubSocket;
    private @Nullable PrintStream hubOut;
    private @Nullable InputStreamReader hubInReader;
    private @Nullable JsonStreamParser hubIn;
    private CompletableFuture<@Nullable Void> hubControllerConnected;
    private int hubReconnectSleep; // Sleep time before we attempt re-connect
    private ScheduledExecutorService scheduler;

    private volatile boolean stop; // Stop the controller as soon as possible
    private volatile boolean stopWhenCommandsServed; // Stop the controller once all pending commands have been served
    private volatile long stopTimestamp = 0; // Stop the controller after this timestamp

    // When we submit commmands to the hub we don't correlate commands and responses. We simply use the first available
    // response that answers our question. For that we maintain lists of all pending commands.
    private Map<Integer, CompletableFuture<AdorneDeviceState>> stateCommands;
    private @Nullable CompletableFuture<List<Integer>> zoneCommand;
    private @Nullable CompletableFuture<String> macAddressCommand;
    private int commandId; // We assign increasing command ids to all rest commands to the hub for easier
                           // troubleshooting

    private volatile @Nullable AdorneHubChangeNotify changeListener;

    private final Object stopLock = new Object();
    private final Object hubOutLock = new Object();
    private final Object stateCommandsLock = new Object();
    private final Object macAddressCommandLock = new Object();
    private final Object zoneCommandLock = new Object();

    public AdorneHubController(AdorneHubConfiguration config, ScheduledExecutorService scheduler) {
        hubController = null;
        hubHost = config.host;
        hubPort = config.port;
        hubSocket = null;
        hubOut = null;
        hubInReader = null;
        hubIn = null;
        hubControllerConnected = new CompletableFuture<>();
        this.scheduler = scheduler;
        hubReconnectSleep = HUB_RECONNECT_SLEEP_MINIMUM;

        stop = false;
        stopWhenCommandsServed = false;
        stopTimestamp = 0;

        stateCommands = new HashMap<>();
        zoneCommand = null;
        macAddressCommand = null;
        commandId = 0;

        changeListener = null;
    }

    /**
     * Start the hub controller. Call only once.
     *
     * @return Future to inform the caller that the hub controller is ready for receiving commands
     */
    public CompletableFuture<@Nullable Void> start() {
        logger.info("Starting hub controller");
        hubController = scheduler.submit(() -> {
            msgLoop();
        });
        return (hubControllerConnected);
    }

    /**
     * Stops the hub controller. Can't restart afterwards.
     */
    public void stop() {
        // Need to use 3 different mechanisms to cover all of our bases of stopping the controller quickly
        synchronized (stopLock) {
            // 1) Stop the controller message loop
            stop = true;

            // 2) Cancel the controller in case it has scheduled a re-connect
            Future<?> hubController = this.hubController;
            if (hubController != null) {
                hubController.cancel(true);
            }
        }

        // 3) Stop the input stream in case controller is waiting on input
        // Note this is best effort. If we are unlucky the hub can still enter waiting on input just after our stop
        // here. Because waiting on input is long-running we can't just synchronize it with the stop check as case 2
        // above. But that is ok as waiting on input has a timeout and will honor stop after that.
        Socket hubSocket = this.hubSocket;
        if (hubSocket != null) {
            try {
                hubSocket.shutdownInput();
            } catch (IOException e) {
                logger.debug("Couldn't shutdown hub socket");
            }
        }
    }

    /**
     * Stops the hub controller once all in-flight commands have been executed.
     */
    public void stopWhenCommandsServed() {
        stopWhenCommandsServed = true;
    }

    /**
     * Stops the hub controller after a certain point in time.
     *
     * @param stopTimestamp timestamp to stop after
     */
    public void stopBy(long stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    /**
     * Turns device on or off.
     *
     * @param zoneId the device's zone ID
     * @param on true to turn on the device
     */
    public void setOnOff(int zoneId, boolean on) {
        sendRestCmd(String.format(HUB_REST_SET_ONOFF, getNextCommandId(), zoneId, on));
    }

    /**
     * Sets the brightness for a device. Applies only to dimmer devices.
     *
     * @param zoneId the device's zone ID
     * @param level A value from 1-100. Note that in particular value 0 is not supported, which means this method can't
     *            be used to turn off a dimmer.
     */
    public void setBrightness(int zoneId, int level) {
        if (level < 1 || level > 100) {
            throw new IllegalArgumentException();
        }
        sendRestCmd(String.format(HUB_REST_SET_BRIGHTNESS, getNextCommandId(), zoneId, level));
    }

    /**
     * Gets asynchronously the state for a device.
     *
     * @param zoneId the device's zone ID
     * @return a future for the {@link AdorneDeviceState}
     */
    public CompletableFuture<AdorneDeviceState> getState(int zoneId) {
        sendRestCmd(String.format(HUB_REST_REQUEST_STATE, getNextCommandId(), zoneId));
        synchronized (stateCommandsLock) {
            CompletableFuture<AdorneDeviceState> stateCommand = stateCommands.get(zoneId);
            if (stateCommand != null) {
                return (stateCommand);
            } else {
                stateCommand = new CompletableFuture<>();
                stateCommands.put(zoneId, stateCommand);
                return (stateCommand);
            }
        }
    }

    /**
     * Gets asynchronously all zone IDs that are in use on the hub.
     *
     * @return a future for the list of zone IDs
     */
    public CompletableFuture<List<Integer>> getZones() {
        sendRestCmd(String.format(HUB_REST_REQUEST_ZONES, getNextCommandId()));

        CompletableFuture<List<Integer>> zoneCommandRet;
        synchronized (zoneCommandLock) {
            CompletableFuture<List<Integer>> zoneCommand = this.zoneCommand;
            if (zoneCommand != null) {
                zoneCommandRet = zoneCommand;
            } else {
                this.zoneCommand = zoneCommandRet = new CompletableFuture<>();
            }
        }
        return (zoneCommandRet);
    }

    /**
     * Gets asynchronously the MAC address of the hub.
     *
     * @return a future for the MAC address
     */
    public CompletableFuture<String> getMACAddress() {
        sendRestCmd(String.format(HUB_REST_REQUEST_MACADDRESS, getNextCommandId()));

        CompletableFuture<String> macAddressCommandRet;
        synchronized (macAddressCommandLock) {
            CompletableFuture<String> macAddressCommand = this.macAddressCommand;
            if (macAddressCommand != null) {
                macAddressCommandRet = macAddressCommand;
            } else {
                this.macAddressCommand = macAddressCommandRet = new CompletableFuture<>();
            }
        }
        return (macAddressCommandRet);
    }

    private void sendRestCmd(String cmd) {
        logger.debug("Sending command {}", cmd);
        synchronized (hubOutLock) {
            PrintStream hubOut = this.hubOut;
            if (hubOut != null) {
                hubOut.print(cmd);
            } else {
                throw new IllegalStateException("Can't send command. Adorne Hub connection is not available.");
            }
        }
    }

    /**
     * Sets a listener to be notified on hub changes.
     *
     * @param changeListener instance to be notified
     */
    public void setChangeListener(AdorneHubChangeNotify changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Runs the controller message loop that is interacting with the Adorne Hub by sending commands and listening for
     * updates
     */
    private void msgLoop() {
        try {
            JsonObject hubMsg;
            JsonPrimitive jsonService;
            String service;

            // Main message loop listening for updates from the hub
            logger.debug("Starting message loop");
            while (!shouldStop()) {
                if (!connect()) {
                    int sleep = hubReconnectSleep;
                    logger.debug("Waiting {} seconds before re-attempting to connect.", sleep);
                    if (hubReconnectSleep < HUB_RECONNECT_SLEEP_MAXIMUM) {
                        hubReconnectSleep = hubReconnectSleep * 2; // Increase sleep time exponentially
                    }
                    restartMsgLoop(sleep);
                    return;
                } else {
                    hubReconnectSleep = HUB_RECONNECT_SLEEP_MINIMUM; // Reset
                }

                if ((hubMsg = getHubMsg()) == null) {
                    continue;
                }

                // Process message based on service type
                if ((jsonService = hubMsg.getAsJsonPrimitive(HUB_TOKEN_SERVICE)) != null) {
                    service = jsonService.getAsString();
                } else {
                    continue; // Ignore messages that don't have a service specified
                }

                if (service.equals(HUB_SERVICE_REPORT_ZONE_PROPERTIES)) {
                    processMsgReportZoneProperties(hubMsg);
                } else if (service.equals(HUB_SERVICE_ZONE_PROPERTIES_CHANGED)) {
                    processMsgZonePropertiesChanged(hubMsg);
                } else if (service.equals(HUB_SERVICE_LIST_ZONE)) {
                    processMsgListZone(hubMsg);
                } else if (service.equals(HUB_SERVICE_SYSTEM_INFO)) {
                    processMsgSystemInfo(hubMsg);
                }
            }
        } catch (RuntimeException e) {
            logger.error("Hub controller failed", e);
        }

        // Shut down
        disconnect();

        // If there are still pending commands we need to cancel them
        synchronized (stateCommandsLock)

        {
            stateCommands.forEach((zoneId, stateCommand) -> stateCommand.cancel(false));
            stateCommands.clear();
        }
        synchronized (zoneCommandLock) {
            CompletableFuture<List<Integer>> zoneCommand = this.zoneCommand;
            if (zoneCommand != null) {
                zoneCommand.cancel(false);
                this.zoneCommand = null;
            }
        }
        synchronized (macAddressCommandLock) {
            CompletableFuture<String> macAddressCommand = this.macAddressCommand;
            if (macAddressCommand != null) {
                macAddressCommand.cancel(false);
                this.macAddressCommand = null;
            }
        }
        hubControllerConnected.cancel(false);

        logger.info("Exiting hub controller");
    }

    private boolean shouldStop() {
        long stopTimestamp = this.stopTimestamp;
        boolean timeoutExceeded = stopTimestamp > 0 && System.currentTimeMillis() > stopTimestamp;
        boolean stateCommandsIsEmpty;
        synchronized (stateCommandsLock) {
            stateCommandsIsEmpty = stateCommands.isEmpty();
        }
        boolean commandsServed = stopWhenCommandsServed && stateCommandsIsEmpty && (zoneCommand == null)
                && (macAddressCommand == null);

        return (stop || timeoutExceeded || commandsServed);
    }

    private boolean connect() {
        try {
            if (hubSocket == null) {
                Socket hubSocket;
                hubSocket = new Socket(hubHost, hubPort);
                this.hubSocket = hubSocket;
                hubSocket.setSoTimeout(HUB_CONNECT_TIMEOUT);
                synchronized (hubOutLock) {
                    hubOut = new PrintStream(hubSocket.getOutputStream());
                }
                hubInReader = new InputStreamReader(hubSocket.getInputStream());
                JsonStreamParser hubIn = new JsonStreamParser(hubInReader);
                this.hubIn = hubIn;
                logger.debug("Hub connection established");

                // Working around an Adorne Hub bug: the first command sent from a new connection intermittently
                // gets lost in the hub. We are requesting the MAC address here simply to get this fragile first
                // command out of the way. Requesting the MAC address and ignoring the result doesn't do any harm.
                getMACAddress();

                hubControllerConnected.complete(null);

                AdorneHubChangeNotify changeListener = this.changeListener;
                if (changeListener != null) {
                    changeListener.connectionChangeNotify(true);
                }
            }
            return true;
        } catch (IOException e) {
            logger.debug("Couldn't establish hub connection ({}).", e.getMessage());
            return false;
        }
    }

    private void disconnect() {
        hubReconnectSleep = HUB_RECONNECT_SLEEP_MINIMUM; // Reset our reconnect sleep time

        InputStreamReader hubInReader = this.hubInReader;
        if (hubInReader != null) {
            try {
                hubInReader.close(); // Closes underlying input stream as well
            } catch (IOException e) {
                logger.warn("Closing hub input reader failed ({})", e.getMessage());
            }
            hubInReader = null;
        }

        synchronized (hubOutLock) {
            PrintStream hubOut = this.hubOut;
            if (hubOut != null) {
                hubOut.close(); // Closes underlying output stream as well
            }
            hubOut = null;
        }

        try {
            Socket hubSocket = this.hubSocket;
            if (hubSocket != null) {
                hubSocket.close();
            }
        } catch (IOException e) {
            logger.warn("Closing hub controller socket failed ({})", e.getMessage());
        }
        hubSocket = null;

        AdorneHubChangeNotify changeListener = this.changeListener;
        if (changeListener != null) {
            changeListener.connectionChangeNotify(false);
        }
    }

    private void restartMsgLoop(int sleep) {
        // Synchronizing this so stop() is guaranteed to cancel a pending schedule
        synchronized (stopLock) {
            if (!stop) {
                hubController = scheduler.schedule(() -> {
                    msgLoop();
                }, sleep, TimeUnit.SECONDS);
            }
        }
    }

    private @Nullable JsonObject getHubMsg() {
        JsonElement hubMsg = null;
        JsonObject hubMsgJsonObject = null;
        JsonStreamParser hubIn = this.hubIn;

        if (hubIn != null && !stop) {
            // Note that the stop check is only best effort. With worst case timing we can still enter next and will
            // honor stop after HUB_CONNECT_TIMEOUT, which is fine.
            try {
                hubMsg = hubIn.next();
            } catch (JsonParseException e) {
                logger.debug("Failed to read valid message {}", e.getMessage());
                disconnect(); // Disconnect so we can recover
            }
            if (hubMsg instanceof JsonObject) {
                hubMsgJsonObject = (JsonObject) hubMsg;
            }
        }

        if (hubMsg == null || (hubMsg instanceof JsonPrimitive && hubMsg.getAsCharacter() == 0)) {
            return null; // We have nothing further to log
        }
        logger.debug("Received message {}", hubMsg);
        if (hubMsgJsonObject == null) {
            logger.debug("Received message is not valid JSON object. Ignoring it.");
            return null;
        }

        return hubMsgJsonObject;
    }

    /**
     * The hub sent zone properties in response to a command.
     */
    private void processMsgReportZoneProperties(JsonObject hubMsg) {
        int zoneId = hubMsg.getAsJsonPrimitive(HUB_TOKEN_ZID).getAsInt();
        logger.debug("Reporting zone properties for zone ID {} ", zoneId);

        JsonObject jsonPropertyList = hubMsg.getAsJsonObject(HUB_TOKEN_PROPERTY_LIST);
        String deviceTypeStr = jsonPropertyList.getAsJsonPrimitive(HUB_TOKEN_DEVICE_TYPE).getAsString();
        ThingTypeUID deviceType;
        if (deviceTypeStr.equals(HUB_TOKEN_SWITCH)) {
            deviceType = THING_TYPE_SWITCH;
        } else if (deviceTypeStr.equals(HUB_TOKEN_DIMMER)) {
            deviceType = THING_TYPE_DIMMER;
        } else {
            logger.debug("Unsupported device type {}", deviceTypeStr);
            return;
        }
        AdorneDeviceState state = new AdorneDeviceState(zoneId,
                jsonPropertyList.getAsJsonPrimitive(HUB_TOKEN_NAME).getAsString(), deviceType,
                jsonPropertyList.getAsJsonPrimitive(HUB_TOKEN_POWER).getAsBoolean(),
                jsonPropertyList.getAsJsonPrimitive(HUB_TOKEN_POWER_LEVEL).getAsInt());

        synchronized (stateCommandsLock) {
            CompletableFuture<AdorneDeviceState> stateCommand = stateCommands.get(zoneId);
            if (stateCommand != null) {
                stateCommand.complete(state);
                stateCommands.remove(zoneId);
            }
        }
    }

    /**
     * The hub informs us about a zone's change in properties.
     */
    private void processMsgZonePropertiesChanged(JsonObject hubMsg) {
        int zoneId = hubMsg.getAsJsonPrimitive(HUB_TOKEN_ZID).getAsInt();
        logger.debug("Zone properties changed for zone ID {} ", zoneId);

        JsonObject jsonPropertyList = hubMsg.getAsJsonObject(HUB_TOKEN_PROPERTY_LIST);
        boolean onOff = jsonPropertyList.getAsJsonPrimitive(HUB_TOKEN_POWER).getAsBoolean();
        int brightness = jsonPropertyList.getAsJsonPrimitive(HUB_TOKEN_POWER_LEVEL).getAsInt();
        AdorneHubChangeNotify changeListener = this.changeListener;
        if (changeListener != null) {
            changeListener.stateChangeNotify(zoneId, onOff, brightness);
        }
    }

    /**
     * The hub sent a list of zones in response to a command.
     */
    private void processMsgListZone(JsonObject hubMsg) {
        List<Integer> zones = new ArrayList<>();
        JsonArray jsonZoneList;

        jsonZoneList = hubMsg.getAsJsonArray(HUB_TOKEN_ZONE_LIST);
        jsonZoneList.forEach(jsonZoneId -> {
            JsonPrimitive jsonZoneIdValue = ((JsonObject) jsonZoneId).getAsJsonPrimitive(HUB_TOKEN_ZID);
            zones.add(jsonZoneIdValue.getAsInt());
        });

        synchronized (zoneCommandLock) {
            CompletableFuture<List<Integer>> zoneCommand = this.zoneCommand;
            if (zoneCommand != null) {
                zoneCommand.complete(zones);
                this.zoneCommand = null;
            }
        }
    }

    /**
     * The hub sent system info in response to a command.
     */
    private void processMsgSystemInfo(JsonObject hubMsg) {
        synchronized (macAddressCommandLock) {
            CompletableFuture<String> macAddressCommand = this.macAddressCommand;
            if (macAddressCommand != null) {
                macAddressCommand.complete(hubMsg.getAsJsonPrimitive(HUB_TOKEN_MAC_ADDRESS).getAsString());
                this.macAddressCommand = null;
            }
        }
    }

    private int getNextCommandId() {
        if (commandId == Integer.MAX_VALUE) {
            commandId = 0;
        }
        return (++commandId);
    }
}
