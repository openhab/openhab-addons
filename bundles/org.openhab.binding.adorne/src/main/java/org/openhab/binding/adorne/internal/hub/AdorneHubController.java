/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.adorne.internal.AdorneDeviceState;
import org.openhab.binding.adorne.internal.configuration.AdorneHubConfiguration;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * The {@link AdorneHubController} manages the interaction with the Adorne hub. The controller maintains a connection
 * with the Adorne Hub and listens to device changes and issues device commands. Interaction with the hub is performed
 * asynchronously through REST messages.
 *
 * @author Mark Theiding - Initial Contribution
 */
@NonNullByDefault
public class AdorneHubController {
    private final Logger logger = LoggerFactory.getLogger(AdorneHubController.class);

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
    private final String hubHost;
    private int hubPort;
    private @Nullable AdorneHubConnection hubConnection;
    private final CompletableFuture<@Nullable Void> hubControllerConnected;
    private int hubReconnectSleep; // Sleep time before we attempt re-connect
    private final ScheduledExecutorService scheduler;

    private volatile boolean stopWhenCommandsServed; // Stop the controller once all pending commands have been served

    // When we submit commmands to the hub we don't correlate commands and responses. We simply use the first available
    // response that answers our question. For that we store all pending commands.
    // Note that for optimal resiliency we send a new request for each command even if a request is already pending
    private final Map<Integer, CompletableFuture<AdorneDeviceState>> stateCommands;
    private @Nullable CompletableFuture<List<Integer>> zoneCommand;
    private @Nullable CompletableFuture<String> macAddressCommand;
    private final AtomicInteger commandId; // We assign increasing command ids to all REST commands to the hub for
                                           // easier troubleshooting

    private final AdorneHubChangeNotify changeListener;

    private final Object stopLock;
    private final Object hubConnectionLock;
    private final Object macAddressCommandLock;
    private final Object zoneCommandLock;

    public AdorneHubController(AdorneHubConfiguration config, ScheduledExecutorService scheduler,
            AdorneHubChangeNotify changeListener) {
        hubHost = config.host;
        hubPort = config.port;
        this.scheduler = scheduler;
        this.changeListener = changeListener;
        hubController = null;
        hubConnection = null;
        hubControllerConnected = new CompletableFuture<>();
        hubReconnectSleep = HUB_RECONNECT_SLEEP_MINIMUM;

        stopWhenCommandsServed = false;

        stopLock = new Object();
        hubConnectionLock = new Object();
        macAddressCommandLock = new Object();
        zoneCommandLock = new Object();

        stateCommands = new HashMap<>();
        zoneCommand = null;
        macAddressCommand = null;
        commandId = new AtomicInteger(0);
    }

    /**
     * Start the hub controller. Call only once.
     *
     * @return Future to inform the caller that the hub controller is ready for receiving commands
     */
    public CompletableFuture<@Nullable Void> start() {
        logger.info("Starting hub controller");
        hubController = scheduler.submit(this::msgLoop);
        return hubControllerConnected;
    }

    /**
     * Stops the hub controller. Can't restart afterwards. If called before start nothing happens.
     */
    public void stop() {
        logger.info("Stopping hub controller");
        synchronized (stopLock) {
            // Canceling the controller tells the message loop to stop and also cancels recreation of the message loop
            // if that is pending after a disconnect.
            Future<?> hubController = this.hubController;
            if (hubController != null) {
                hubController.cancel(true);
            }
        }

        // Stop the input stream in case controller is waiting on input
        // Note this is best effort. If we are unlucky the hub can still enter waiting on input just after our stop
        // here. Because waiting on input is long-running we can't just synchronize it with the stop check as case 2
        // above. But that is ok as waiting on input has a timeout and will honor stop after that.
        synchronized (hubConnectionLock) {
            AdorneHubConnection hubConnection = this.hubConnection;
            if (hubConnection != null) {
                hubConnection.cancel();
            }
        }

        cancelCommands();
    }

    /**
     * Stops the hub controller once all in-flight commands have been executed.
     */
    public void stopWhenCommandsServed() {
        stopWhenCommandsServed = true;
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
        // Note that we send the REST command for resiliency even if there is a pending command
        sendRestCmd(String.format(HUB_REST_REQUEST_STATE, getNextCommandId(), zoneId));

        CompletableFuture<AdorneDeviceState> stateCommand;
        synchronized (stateCommands) {
            stateCommand = stateCommands.get(zoneId);
            if (stateCommand == null) {
                stateCommand = new CompletableFuture<>();
                stateCommands.put(zoneId, stateCommand);
            }
        }
        return stateCommand;
    }

    /**
     * Gets asynchronously all zone IDs that are in use on the hub.
     *
     * @return a future for the list of zone IDs
     */
    public CompletableFuture<List<Integer>> getZones() {
        // Note that we send the REST command for resiliency even if there is a pending command
        sendRestCmd(String.format(HUB_REST_REQUEST_ZONES, getNextCommandId()));

        CompletableFuture<List<Integer>> zoneCommand;
        synchronized (zoneCommandLock) {
            zoneCommand = this.zoneCommand;
            if (zoneCommand == null) {
                this.zoneCommand = zoneCommand = new CompletableFuture<>();
            }
        }
        return zoneCommand;
    }

    /**
     * Gets asynchronously the MAC address of the hub.
     *
     * @return a future for the MAC address
     */
    public CompletableFuture<String> getMACAddress() {
        // Note that we send the REST command for resiliency even if there is a pending command
        sendRestCmd(String.format(HUB_REST_REQUEST_MACADDRESS, getNextCommandId()));

        CompletableFuture<String> macAddressCommand;
        synchronized (macAddressCommandLock) {
            macAddressCommand = this.macAddressCommand;
            if (macAddressCommand == null) {
                this.macAddressCommand = macAddressCommand = new CompletableFuture<>();
            }
        }
        return macAddressCommand;
    }

    private void sendRestCmd(String cmd) {
        logger.debug("Sending command {}", cmd);
        synchronized (hubConnectionLock) {
            AdorneHubConnection hubConnection = this.hubConnection;
            if (hubConnection != null) {
                hubConnection.putMsg(cmd);
            } else {
                throw new IllegalStateException("Can't send command. Adorne Hub connection is not available.");
            }
        }
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

                hubMsg = null;
                try {
                    AdorneHubConnection hubConnection = this.hubConnection;
                    if (hubConnection != null) {
                        hubMsg = hubConnection.getMsg();
                    }
                } catch (JsonParseException e) {
                    logger.debug("Failed to read valid message {}", e.getMessage());
                    disconnect(); // Disconnect so we can recover
                }
                if (hubMsg == null) {
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
            logger.warn("Hub controller failed", e);
        }

        // Shut down
        disconnect();

        cancelCommands();
        hubControllerConnected.cancel(false);
        logger.info("Exiting hub controller");
    }

    private boolean shouldStop() {
        boolean stateCommandsIsEmpty;
        synchronized (stateCommands) {
            stateCommandsIsEmpty = stateCommands.isEmpty();
        }
        boolean commandsServed = stopWhenCommandsServed && stateCommandsIsEmpty && (zoneCommand == null)
                && (macAddressCommand == null);

        return isCancelled() || commandsServed;
    }

    private boolean isCancelled() {
        Future<?> hubController = this.hubController;
        return hubController == null || hubController.isCancelled();
    }

    private boolean connect() {
        try {
            if (hubConnection == null) {
                hubConnection = new AdorneHubConnection(hubHost, hubPort, HUB_CONNECT_TIMEOUT);
                logger.debug("Hub connection established");

                // Working around an Adorne Hub bug: the first command sent from a new connection intermittently
                // gets lost in the hub. We are requesting the MAC address here simply to get this fragile first
                // command out of the way. Requesting the MAC address and ignoring the result doesn't do any harm.
                getMACAddress();

                hubControllerConnected.complete(null);

                changeListener.connectionChangeNotify(true);
            }
            return true;
        } catch (IOException e) {
            logger.debug("Couldn't establish hub connection ({}).", e.getMessage());
            return false;
        }
    }

    private void disconnect() {
        hubReconnectSleep = HUB_RECONNECT_SLEEP_MINIMUM; // Reset our reconnect sleep time
        synchronized (hubConnectionLock) {
            AdorneHubConnection hubConnection = this.hubConnection;
            if (hubConnection != null) {
                hubConnection.close();
                this.hubConnection = null;
            }
        }

        changeListener.connectionChangeNotify(false);
    }

    private void cancelCommands() {
        // If there are still pending commands we need to cancel them
        synchronized (stateCommands) {
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
        logger.debug("Cancelled commands");
    }

    private void restartMsgLoop(int sleep) {
        synchronized (stopLock) {
            if (!isCancelled()) {
                this.hubController = scheduler.schedule(this::msgLoop, sleep, TimeUnit.SECONDS);
            }
        }
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

        synchronized (stateCommands) {
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
        changeListener.stateChangeNotify(zoneId, onOff, brightness);
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
        IntUnaryOperator op = commandId -> {
            int newCommandId = commandId;
            if (commandId == Integer.MAX_VALUE) {
                newCommandId = 0;
            }
            return ++newCommandId;
        };

        return commandId.updateAndGet(op);
    }
}
