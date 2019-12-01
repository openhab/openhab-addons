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
package org.openhab.binding.adorne.internal.hub;

import static org.openhab.binding.adorne.internal.AdorneBindingConstants.*;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.adorne.internal.AdorneDeviceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link AdorneHubController} manages the interaction with the Adorne hub via REST messages. The controller
 * maintains a connection with the Adorne Hub and listens to device changes and issues device commands. Interaction with
 * the hub is performed asynchronously through REST messages.
 *
 * @author Mark Theiding - Initial Contribution
 */
@NonNullByDefault
public class AdorneHubController implements Runnable {
    private Logger logger = LoggerFactory.getLogger(AdorneHubController.class);

    private static final int HUB_CONNECT_TIMEOUT = 15000;
    private static final int HUB_RECONNECT_SLEEP_MIN = 1000;
    private static final int HUB_RECONNECT_SLEEP_MAX = 15 * 60 * 1000;
    private static final String HUB_DEFAULT_HOST = "LCM1.local";
    private static final int HUB_DEFAULT_PORT = 2112;

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
    private @Nullable Scanner hubIn;
    private @Nullable CompletableFuture<@Nullable Void> hubControllerConnected;
    private int hubReconnectSleep; // Sleep time before we attempt re-connect
    private ScheduledExecutorService scheduler;

    private AtomicBoolean stop; // Stop the controller as soon as possible
    private AtomicBoolean stopWhenCommandsServed; // Stop the controller once all pending commands have been served
    private long stopTimestamp = 0; // Stop the controller after this timestamp

    // When we submit commmands to the hub we don't correlate commands and responses. We simply use the first available
    // response that answers our question. For that we maintain lists of all pending commands.
    private List<AdorneHubController.ZoneIDFutureRecord> stateCommands;
    private List<CompletableFuture<List<Integer>>> zoneCommands;
    private List<CompletableFuture<String>> macAddressCommands;
    private int commandId; // We assign increasing command ids to all rest commands to the hub for easier
                           // troubleshooting

    private @Nullable AdorneHubChangeNotify changeListener;
    private Gson gson;

    private static class ZoneIDFutureRecord {
        public int zoneId;
        public CompletableFuture<AdorneDeviceState> future;

        ZoneIDFutureRecord(int zoneId, CompletableFuture<AdorneDeviceState> future) {
            this.zoneId = zoneId;
            this.future = future;
        }
    }

    public AdorneHubController(@Nullable String hubHost, @Nullable Integer hubPort,
            ScheduledExecutorService scheduler) {
        hubController = null;
        this.hubHost = (hubHost == null) ? HUB_DEFAULT_HOST : hubHost;
        this.hubPort = hubPort == null ? HUB_DEFAULT_PORT : hubPort;
        hubSocket = null;
        hubOut = null;
        hubIn = null;
        hubControllerConnected = null;
        this.scheduler = scheduler;

        stop = new AtomicBoolean(false);
        stopWhenCommandsServed = new AtomicBoolean(false);
        stopTimestamp = 0;

        stateCommands = new ArrayList<>();
        zoneCommands = new ArrayList<>();
        macAddressCommands = new ArrayList<>();
        commandId = 0;

        changeListener = null;
        gson = new Gson();
    }

    /**
     * Start the hub controller. Call only once.
     *
     * @return Future to inform the caller that the hub controller is ready for receiving commands
     */
    public CompletableFuture<@Nullable Void> start() {
        stop.set(false);
        CompletableFuture<@Nullable Void> hubControllerConnected = this.hubControllerConnected = new CompletableFuture<>();
        hubController = scheduler.submit(this);
        return (hubControllerConnected);
    }

    /**
     * Stops the hub controller. Can't restart afterwards.
     */
    public void stop() {
        // Need to use 3 different mechanisms to cover all of our bases of stopping the controller quickly
        stop.set(true); // 1) Stop the controller message loop

        synchronized (this) {
            if (hubSocket != null) {
                try {
                    hubSocket.shutdownInput(); // 2) Stop the input stream in case controller is waiting on input
                } catch (IOException e) {
                    logger.warn("Couldn't shutdown hub socket");
                }
            }
        }

        if (hubController != null) {
            hubController.cancel(true); // 3) Interrupt the controller in case it is sleeping
        }
    }

    /**
     * Stops the hub controller once all in-flight commands have been executed.
     */
    public void stopWhenCommandsServed() {
        stopWhenCommandsServed.set(true);
    }

    /**
     * Stops the hub controller after <code>stopTimestamp</code>.
     */
    public void stopBy(long stopTimestamp) {
        logger.debug("Stopping hub controller");
        synchronized (this) {
            this.stopTimestamp = stopTimestamp;
        }
    }

    /**
     * Turns the device with zone ID <code>zoneID</code> on or off.
     */
    public void setOnOff(int zoneID, boolean on) {
        sendRestCmd(String.format(HUB_REST_SET_ONOFF, getNextCommandId(), zoneID, on));
    }

    /**
     * Sets the brightness for the device with zone ID <code>zoneID</code>. Applies only to dimmer devices.
     *
     * @param level A value from 1-100. Note that in particular value 0 is not supported, i.e. you can't use this method
     *            to turn off a dimmer.
     */
    public void setBrightness(int zoneID, int level) {
        if (level < 1 || level > 100) {
            throw new IllegalArgumentException();
        }
        sendRestCmd(String.format(HUB_REST_SET_BRIGHTNESS, getNextCommandId(), zoneID, level));
    }

    /**
     * Gets the state for the device with zone ID <code>zoneID</code>.
     */
    public CompletableFuture<AdorneDeviceState> getState(int zoneID) {
        CompletableFuture<AdorneDeviceState> stateCommand = new CompletableFuture<>();
        synchronized (this) {
            stateCommands.add(new AdorneHubController.ZoneIDFutureRecord(zoneID, stateCommand));
        }
        sendRestCmd(String.format(HUB_REST_REQUEST_STATE, getNextCommandId(), zoneID));
        return stateCommand;
    }

    /**
     * Gets all zone IDs that are in use on the hub.
     */
    public CompletableFuture<List<Integer>> getZones() {
        CompletableFuture<List<Integer>> zoneCommand = new CompletableFuture<>();
        synchronized (this) {
            zoneCommands.add(zoneCommand);
        }
        sendRestCmd(String.format(HUB_REST_REQUEST_ZONES, getNextCommandId()));
        return zoneCommand;
    }

    /**
     * Gets the MAC address of the hub.
     */
    public CompletableFuture<String> getMACAddress() {
        CompletableFuture<String> macAddressCommand = new CompletableFuture<>();
        synchronized (this) {
            macAddressCommands.add(macAddressCommand);
        }
        sendRestCmd(String.format(HUB_REST_REQUEST_MACADDRESS, getNextCommandId()));
        return macAddressCommand;
    }

    private void sendRestCmd(String cmd) {
        logger.debug("Sending command {}", cmd);
        synchronized (this) {
            if (hubOut != null) {
                hubOut.print(cmd);
            } else {
                throw new IllegalStateException("Can't send command. Adorne Hub connection is not available");
            }
        }
    }

    /**
     * Sets a listener to be notified on hub changes.
     */
    public synchronized void setChangeListener(AdorneHubChangeNotify changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Runs the controller that is interacting with the Adorne Hub by sending commands and listening for updates
     */
    @Override
    public void run() {
        logger.info("Starting hub controller");
        try {
            Map<String, Object> hubMsg;
            String service;
            hubReconnectSleep = HUB_RECONNECT_SLEEP_MIN;

            // Main message loop listening for updates from the hub
            while (!shouldStop()) {
                if (!connect()) {
                    continue;
                }

                if ((hubMsg = getHubMsg()) == null) {
                    continue;
                }

                // Process message based on service type
                if ((service = getMsgService(hubMsg)) == null) {
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
        } catch (Exception e) {
            logger.error("Hub controller failed ({})", e.getMessage());
        } finally {
            synchronized (this) {
                // If there are still pending requests we need to cancel them
                for (AdorneHubController.ZoneIDFutureRecord stateCommand : stateCommands) {
                    stateCommand.future.cancel(false);
                }
                for (CompletableFuture<List<Integer>> zoneCommand : zoneCommands) {
                    zoneCommand.cancel(false);
                }
                for (CompletableFuture<String> macAddressCommand : macAddressCommands) {
                    macAddressCommand.cancel(false);
                }
                if (hubControllerConnected != null) {
                    hubControllerConnected.cancel(false);
                }
            }
            disconnect();
            logger.info("Exiting hub controller");
        }
    }

    private synchronized boolean shouldStop() {
        boolean timeoutExceeded = stopTimestamp > 0 && System.currentTimeMillis() > stopTimestamp;
        boolean commandsServed = stopWhenCommandsServed.get() && stateCommands.isEmpty() && zoneCommands.isEmpty()
                && macAddressCommands.isEmpty();

        return (stop.get() || timeoutExceeded || commandsServed);
    }

    private boolean connect() {
        try {
            synchronized (this) {
                if (hubSocket == null) {
                    hubSocket = new Socket(hubHost, hubPort);
                    hubSocket.setSoTimeout(HUB_CONNECT_TIMEOUT);
                    if (hubSocket != null) {
                        hubOut = new PrintStream(hubSocket.getOutputStream());
                    }
                    if (hubSocket != null) {
                        hubIn = new Scanner(hubSocket.getInputStream()).useDelimiter("\0");
                    }
                    if (hubControllerConnected != null) {
                        hubControllerConnected.complete(null);
                    }
                    if (changeListener != null) {
                        changeListener.statusChangeNotify(true);
                    }
                    logger.debug("Hub connection established");
                }
            }
            return true;
        } catch (IOException e) {
            logger.debug("Couldn't establish hub connection ({})", e.getMessage());
            synchronized (this) {
                if (changeListener != null) {
                    changeListener.statusChangeNotify(false);
                }
            }
            try {
                logger.debug("Sleep {} ms before re-trying hub connection", hubReconnectSleep);
                Thread.sleep(hubReconnectSleep);
                if (hubReconnectSleep < HUB_RECONNECT_SLEEP_MAX) {
                    hubReconnectSleep = hubReconnectSleep * 2;
                }
            } catch (InterruptedException exc) {
                // We got interrupted. Nothing to do other than moving on (and exiting our message loop).
            }
        }
        return false;
    }

    private void disconnect() {
        hubReconnectSleep = HUB_RECONNECT_SLEEP_MIN; // Reset our reconnect sleep time
        synchronized (this) {
            try {
                if (hubIn != null) {
                    hubIn.close(); // closes underlying input stream as well
                }
            } catch (Exception e) {
                logger.warn("Closing hub controller input stream failed ({})", e.getMessage());
            }
            ;
            try {
                if (hubOut != null) {
                    hubOut.close();// closes underlying output stream as well
                }
            } catch (Exception e) {
                logger.warn("Closing hub controller output stream failed ({})", e.getMessage());
            }
            ;
            try {
                if (hubSocket != null) {
                    hubSocket.close();
                }
            } catch (Exception e) {
                logger.warn("Closing hub controller socket failed ({})", e.getMessage());
            }
            hubSocket = null;
            hubIn = null;
            hubOut = null;
        }
    }

    private @Nullable Map<String, Object> getHubMsg() {
        String hubMsgStr = null;
        Map<String, Object> hubMsg = null;
        try {
            if (hubIn != null) {
                hubMsgStr = hubIn.next();
            }
        } catch (NoSuchElementException e) {
            logger.debug("Failed to read hub message");
            disconnect();
            return null;
        }

        logger.debug("Received message {}", hubMsgStr);

        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        try {
            hubMsg = gson.fromJson(hubMsgStr, type);
        } catch (JsonSyntaxException e) {
            logger.debug("Message is not valid JSON format. Ignoring it.");
        }

        return hubMsg;
    }

    private @Nullable String getMsgService(Map<String, @Nullable Object> hubMsg) {
        String service = (String) hubMsg.get(HUB_TOKEN_SERVICE);
        if (service == null) {
            logger.debug("Message has no service specified");
        } else {
            logger.debug("Message service is {}", service);
        }
        return service;
    }

    /**
     * The hub sent zone properties in response to a command.
     */
    private void processMsgReportZoneProperties(Map<String, Object> hubMsg) {
        int zoneId = ((Double) hubMsg.get(HUB_TOKEN_ZID)).intValue();
        logger.debug("Reporting zone properties for zone ID {} ", zoneId);
        List<AdorneHubController.ZoneIDFutureRecord> removeStateCommands = new ArrayList<>();
        synchronized (this) {
            for (AdorneHubController.ZoneIDFutureRecord stateCommand : stateCommands) {
                if (stateCommand.zoneId == zoneId) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> hubMsgPropertyList = (Map<String, Object>) hubMsg.get(HUB_TOKEN_PROPERTY_LIST);
                    ThingTypeUID deviceType;
                    String deviceTypeStr = (String) hubMsgPropertyList.get(HUB_TOKEN_DEVICE_TYPE);
                    if (deviceTypeStr.equals(HUB_TOKEN_SWITCH)) {
                        deviceType = THING_TYPE_SWITCH;
                    } else if (deviceTypeStr.equals(HUB_TOKEN_DIMMER)) {
                        deviceType = THING_TYPE_DIMMER;
                    } else {
                        logger.debug("Unsupported device type {}", deviceTypeStr);
                        continue;
                    }
                    AdorneDeviceState state = new AdorneDeviceState(zoneId,
                            (String) hubMsgPropertyList.get(HUB_TOKEN_NAME), deviceType,
                            (Boolean) hubMsgPropertyList.get(HUB_TOKEN_POWER),
                            ((Double) hubMsgPropertyList.get(HUB_TOKEN_POWER_LEVEL)).intValue());
                    stateCommand.future.complete(state);
                    removeStateCommands.add(stateCommand);
                }
            }
            for (AdorneHubController.ZoneIDFutureRecord stateCommand : removeStateCommands) {
                stateCommands.remove(stateCommand);
            }
        }
    }

    /**
     * The hub informs us about a zone's change in properties.
     */
    private void processMsgZonePropertiesChanged(Map<String, Object> hubMsg) {
        int zoneId = ((Double) hubMsg.get(HUB_TOKEN_ZID)).intValue();
        logger.debug("Zone properties changed for zone ID {} ", zoneId);
        @SuppressWarnings("unchecked")
        Map<String, Object> hubMsgPropertyList = (Map<String, Object>) hubMsg.get(HUB_TOKEN_PROPERTY_LIST);
        boolean onOff = (Boolean) hubMsgPropertyList.get(HUB_TOKEN_POWER);
        int brightness = ((Double) hubMsgPropertyList.get(HUB_TOKEN_POWER_LEVEL)).intValue();
        synchronized (this) {
            if (changeListener != null) {
                changeListener.stateChangeNotify(zoneId, onOff, brightness);
            }
        }
    }

    /**
     * The hub sent a list of zones in response to a command.
     */
    private void processMsgListZone(Map<String, Object> hubMsg) {
        List<Integer> zones = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Double>> hubMsgZoneIdMapList = (List<Map<String, Double>>) hubMsg.get(HUB_TOKEN_ZONE_LIST);
        for (Map<String, Double> zoneIdMap : hubMsgZoneIdMapList) {
            zones.add(zoneIdMap.get(HUB_TOKEN_ZID).intValue());
        }
        synchronized (this) {
            for (CompletableFuture<List<Integer>> zoneCommand : zoneCommands) {
                zoneCommand.complete(zones);
            }
            zoneCommands.clear();
        }
    }

    /**
     * The hub sent system info in response to a command.
     */
    private void processMsgSystemInfo(Map<String, Object> hubMsg) {
        synchronized (this) {
            for (CompletableFuture<String> macAddressCommand : macAddressCommands) {
                macAddressCommand.complete((String) hubMsg.get(HUB_TOKEN_MAC_ADDRESS));
            }
            macAddressCommands.clear();
        }
    }

    private int getNextCommandId() {
        if (commandId == Integer.MAX_VALUE) {
            commandId = 0;
        }
        return (++commandId);
    }
}
