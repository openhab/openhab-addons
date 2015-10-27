/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.handler;

import static org.openhab.binding.dscalarm.DSCAlarmBindingConstants.BRIDGE_RESET;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dscalarm.config.DSCAlarmPartitionConfiguration;
import org.openhab.binding.dscalarm.config.DSCAlarmZoneConfiguration;
import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageType;
import org.openhab.binding.dscalarm.internal.discovery.DSCAlarmDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for a DSC Alarm Bridge Handler.
 *
 * @author Russell Stephens - Initial Contribution
 */
public abstract class DSCAlarmBaseBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(DSCAlarmBaseBridgeHandler.class);

    /** The DSC Alarm bridge type. */
    private DSCAlarmBridgeType dscAlarmBridgeType = null;

    /** The DSC Alarm Discovery Service. */
    private DSCAlarmDiscoveryService dscAlarmDiscoveryService = null;

    /** The Panel Thing handler for the bridge. */
    private DSCAlarmBaseThingHandler panelThingHandler = null;

    /** Connection status for the bridge. */
    private boolean connected = false;

    /** Determines if a thing has changed. */
    private boolean thingsHaveChanged = false;

    /** Determines if all things have been refreshed. */
    private boolean allThingsRefreshed = false;

    /** Password for bridge connection authentication. */
    private String password = null;

    /** User Code for some DSC Alarm commands. */
    private String userCode = null;

    // Polling variables
    public int pollPeriod = 0;
    private long pollElapsedTime = 0;
    private long pollStartTime = 0;
    private long refreshInterval = 5000;

    private ScheduledFuture<?> pollingTask;

    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            polling();
        }
    };

    /**
     * Constructor.
     *
     * @param bridge
     * @param dscAlarmBridgeType
     */
    DSCAlarmBaseBridgeHandler(Bridge bridge, DSCAlarmBridgeType dscAlarmBridgeType) {
        super(bridge);
        this.dscAlarmBridgeType = dscAlarmBridgeType;
    }

    /**
     * Returns the bridge type.
     */
    public DSCAlarmBridgeType getBridgeType() {
        return dscAlarmBridgeType;
    }

    /**
     * Sets the bridge type.
     *
     * @param dscAlarmBridgeType
     */
    public void setBridgeType(DSCAlarmBridgeType dscAlarmBridgeType) {
        this.dscAlarmBridgeType = dscAlarmBridgeType;
    }

    /**
     * Register the Discovery Service.
     *
     * @param discoveryService
     */
    public void registerDiscoveryService(DSCAlarmDiscoveryService discoveryService) {
        if (discoveryService == null) {
            throw new IllegalArgumentException("registerDiscoveryService(): Illegal Argument. Not allowed to be Null!");
        } else {
            this.dscAlarmDiscoveryService = discoveryService;
            logger.trace("registerDiscoveryService(): Discovery Service Registered!");
        }
    }

    /**
     * Unregister the Discovery Service.
     */
    public void unregisterDiscoveryService() {
        dscAlarmDiscoveryService = null;
        logger.trace("unregisterDiscoveryService(): Discovery Service Unregistered!");
    }

    /**
     * Connect The Bridge.
     */
    private void connect() {
        onDisconnected();

        openConnection();

        if (isConnected()) {
            if (sendCommand(DSCAlarmCode.NetworkLogin)) {
                onConnected();
            } else {
                closeConnection();
            }
        }
    }

    /**
     * Disconnect The Bridge.
     */
    private void disconnect() {

        closeConnection();

        if (!isConnected()) {
            onDisconnected();
        }
    }

    /**
     * Returns connection status.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set connection status.
     *
     * @param connected
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Set channel 'bridge_connection'.
     *
     * @param connected
     */
    public void setBridgeConnection(boolean connected) {
        logger.debug("setBridgeConnection(): Set Bridge to {}", connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), BRIDGE_RESET);

        setConnected(connected);

        updateState(channelUID, connected ? OnOffType.ON : OnOffType.OFF);
        updateStatus(connected ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    /**
     * Runs when connected.
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");

        setBridgeConnection(true);

        // Inform thing handlers of connection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            DSCAlarmBaseThingHandler thingHandler = (DSCAlarmBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeConnected(this);
                logger.trace("onConnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Runs when disconnected.
     */
    public void onDisconnected() {
        logger.debug("onDisconnected(): Bridge Disconnected!");

        setBridgeConnection(false);

        // Inform thing handlers of disconnection
        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {
            DSCAlarmBaseThingHandler thingHandler = (DSCAlarmBaseThingHandler) thing.getHandler();

            if (thingHandler != null) {
                thingHandler.onBridgeDisconnected(this);
                logger.trace("onDisconnected(): Bridge - {}, Thing - {}, Thing Handler - {}", thing.getBridgeUID(), thing.getUID(), thingHandler);
            }
        }
    }

    /**
     * Method for opening a connection to DSC Alarm.
     */
    abstract void openConnection();

    /**
     * Method for closing a connection to DSC Alarm.
     */
    abstract void closeConnection();

    /**
     * Method for writing to an open DSC Alarm connection.
     *
     * @param writeString
     */
    public abstract void write(String writeString);

    /**
     * Method for reading from an open DSC Alarm connection.
     */
    public abstract String read();

    /**
     * Get Bridge Password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set Bridge Password.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get Panel User Code.
     */
    public String getUserCode() {
        return this.userCode;
    }

    /**
     * Set Panel User Code.
     *
     * @param userCode
     */
    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    /**
     * Method to start the polling task.
     */
    public void startPolling() {
        logger.debug("Starting DSC Alarm Polling Task.");
        if (pollingTask == null || pollingTask.isCancelled()) {
            pollingTask = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Method to stop the polling task.
     */
    public void stopPolling() {
        logger.debug("Stopping DSC Alarm Polling Task.");
        if (pollingTask != null && !pollingTask.isCancelled()) {
            pollingTask.cancel(true);
            pollingTask = null;
        }
    }

    /**
     * Method for polling the DSC Alarm System.
     */
    public synchronized void polling() {
        logger.debug("DSC Alarm Polling Task - '{}'", getThing().getUID());

        if (isConnected()) {

            if (pollStartTime == 0) {
                pollStartTime = System.currentTimeMillis();
            }

            pollElapsedTime = ((System.currentTimeMillis() - pollStartTime) / 1000) / 60;

            // Send Poll command to the DSC Alarm if idle for 'pollPeriod' minutes
            if (pollElapsedTime >= pollPeriod) {
                sendCommand(DSCAlarmCode.Poll);
                pollStartTime = 0;
            }

            checkThings();

            if (thingsHaveChanged) {
                if (allThingsRefreshed) {
                    this.setBridgeConnection(isConnected());
                    thingsHaveChanged = false;
                    // Get a status report from API.
                    sendCommand(DSCAlarmCode.StatusReport);
                }
            }
        } else {
            logger.error("Not Connected to the DSC Alarm!");
            connect();
        }
    }

    /**
     * Check if things have changed.
     */
    public void checkThings() {
        logger.trace("Checking Things!");

        allThingsRefreshed = true;

        List<Thing> things = getThing().getThings();

        for (Thing thing : things) {

            DSCAlarmBaseThingHandler handler = (DSCAlarmBaseThingHandler) thing.getHandler();

            if (handler != null) {
                logger.debug("***Checking '{}' - Status: {}, Refreshed: {}", thing.getUID(), thing.getStatus(), handler.isThingRefreshed());

                if (!handler.isThingRefreshed()) {

                    handler.onBridgeConnected(this);

                    if (handler.isThingRefreshed()) {
                        thingsHaveChanged = true;
                    }

                    if (handler.getDSCAlarmThingType().equals(DSCAlarmThingType.PANEL)) {
                        if (panelThingHandler == null)
                            panelThingHandler = handler;
                    }

                    allThingsRefreshed = false;
                }

            } else {
                logger.error("checkThings(): Thing handler not found!");
            }
        }
    }

    /**
     * Find a Thing.
     *
     * @param dscAlarmThingType
     * @param partitionId
     * @param zoneId
     * @return thing
     */
    public Thing findThing(DSCAlarmThingType dscAlarmThingType, int partitionId, int zoneId) {
        List<Thing> things = getThing().getThings();

        Thing thing = null;

        for (Thing t : things) {

            try {
                Configuration config = t.getConfiguration();
                DSCAlarmBaseThingHandler handler = (DSCAlarmBaseThingHandler) t.getHandler();

                if (handler != null) {
                    DSCAlarmThingType handlerDSCAlarmThingType = handler.getDSCAlarmThingType();

                    if (handlerDSCAlarmThingType != null) {
                        if (handlerDSCAlarmThingType.equals(dscAlarmThingType)) {
                            switch (handlerDSCAlarmThingType) {
                                case PANEL:
                                case KEYPAD:
                                    thing = t;
                                    logger.debug("findThing(): Thing Found - {}, {}, {}", t, handler, handlerDSCAlarmThingType);
                                    return thing;
                                case PARTITION:
                                    BigDecimal partitionNumber = (BigDecimal) config.get(DSCAlarmPartitionConfiguration.PARTITION_NUMBER);
                                    if (partitionId == partitionNumber.intValue()) {
                                        thing = t;
                                        logger.debug("findThing(): Thing Found - {}, {}, {}", t, handler, handlerDSCAlarmThingType);
                                        return thing;
                                    }
                                    break;
                                case ZONE:
                                    BigDecimal zoneNumber = (BigDecimal) config.get(DSCAlarmZoneConfiguration.ZONE_NUMBER);
                                    if (zoneId == zoneNumber.intValue()) {
                                        thing = t;
                                        logger.debug("findThing(): Thing Found - {}, {}, {}", t, handler, handlerDSCAlarmThingType);
                                        return thing;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("findThing(): Error Seaching Thing - {}", e);
            }
        }

        return thing;
    }

    /**
     * Handles an incoming message from the DSC Alarm System.
     *
     * @param incomingMessage
     */
    public synchronized void handleIncomingMessage(String incomingMessage) {
        if (incomingMessage != null && incomingMessage != "") {
            DSCAlarmMessage apiMessage = new DSCAlarmMessage(incomingMessage);
            DSCAlarmMessageType apiMessageType = apiMessage.getDSCAlarmMessageType();

            logger.debug("handleIncomingMessage(): Message received: {} - {}", incomingMessage, apiMessage.toString());

            DSCAlarmEvent event = new DSCAlarmEvent(this);
            event.dscAlarmEventMessage(apiMessage);
            DSCAlarmThingType dscAlarmThingType = null;
            int partitionId = 0;
            int zoneId = 0;

            DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(apiMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
            if (apiCode == DSCAlarmCode.CommandAcknowledge) {
                String apiData = apiMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);
                if (apiData.equals("000")) {
                    setBridgeConnection(true);
                }
            }

            switch (apiMessageType) {
                case PANEL_EVENT:
                    dscAlarmThingType = DSCAlarmThingType.PANEL;
                    break;
                case PARTITION_EVENT:
                    dscAlarmThingType = DSCAlarmThingType.PARTITION;
                    partitionId = Integer.parseInt(event.getDSCAlarmMessage().getMessageInfo(DSCAlarmMessageInfoType.PARTITION));
                    break;
                case ZONE_EVENT:
                    dscAlarmThingType = DSCAlarmThingType.ZONE;
                    zoneId = Integer.parseInt(event.getDSCAlarmMessage().getMessageInfo(DSCAlarmMessageInfoType.ZONE));
                    break;
                case KEYPAD_EVENT:
                    dscAlarmThingType = DSCAlarmThingType.KEYPAD;
                    break;
                default:
                    break;
            }

            if (dscAlarmThingType != null) {

                Thing thing = findThing(dscAlarmThingType, partitionId, zoneId);

                logger.debug("handleIncomingMessage(): Thing Search - '{}'", thing);

                if (thing != null) {
                    DSCAlarmBaseThingHandler thingHandler = (DSCAlarmBaseThingHandler) thing.getHandler();

                    if (thingHandler != null) {
                        thingHandler.dscAlarmEventReceived(event, thing);

                        if (panelThingHandler != null) {
                            if (!thingHandler.equals(panelThingHandler)) {
                                panelThingHandler.dscAlarmEventReceived(event, thing);
                            }
                        }
                    }
                } else {
                    logger.debug("handleIncomingMessage(): Thing Not Found! Send to Discovery Service!");

                    if (dscAlarmDiscoveryService != null) {
                        dscAlarmDiscoveryService.addThing(getThing(), dscAlarmThingType, event);
                    }
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("No bridge commands defined.");
        if (isConnected()) {
            switch (channelUID.getId()) {
                case BRIDGE_RESET:
                    if (command == OnOffType.OFF) {
                        disconnect();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Send an API command to the DSC Alarm system.
     *
     * @param dscAlarmCode
     * @param dscAlarmData
     * @return successful
     */
    public boolean sendCommand(DSCAlarmCode dscAlarmCode, String... dscAlarmData) {
        boolean successful = false;
        boolean validCommand = false;

        String command = dscAlarmCode.getCode();
        String data = "";

        switch (dscAlarmCode) {
            case Poll: /* 000 */
            case StatusReport: /* 001 */
                validCommand = true;
                break;
            case LabelsRequest: /* 002 */
                if (!dscAlarmBridgeType.equals(DSCAlarmBridgeType.IT100)) {
                    break;
                }
                validCommand = true;
                break;
            case NetworkLogin: /* 005 */
                if (!dscAlarmBridgeType.equals(DSCAlarmBridgeType.Envisalink)) {
                    break;
                }

                if (password == null || password.length() < 1 || password.length() > 6) {
                    logger.error("sendCommand(): Password is invalid, must be between 1 and 6 chars", password);
                    break;
                }
                data = password;
                validCommand = true;
                break;
            case DumpZoneTimers: /* 008 */
                if (!dscAlarmBridgeType.equals(DSCAlarmBridgeType.Envisalink)) {
                    break;
                }
                validCommand = true;
                break;
            case SetTimeDate: /* 010 */
                Date date = new Date();
                SimpleDateFormat dateTime = new SimpleDateFormat("HHmmMMddYY");
                data = dateTime.format(date);
                validCommand = true;
                break;
            case CommandOutputControl: /* 020 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-8]")) {
                    logger.error("sendCommand(): Partition number must be a single character string from 1 to 8, it was: " + dscAlarmData[0]);
                    break;
                }

                if (dscAlarmData[1] == null || !dscAlarmData[1].matches("[1-4]")) {
                    logger.error("sendCommand(): Output number must be a single character string from 1 to 4, it was: " + dscAlarmData[1]);
                    break;
                }

                data = dscAlarmData[0];
                validCommand = true;
                break;
            case KeepAlive: /* 074 */
                if (!dscAlarmBridgeType.equals(DSCAlarmBridgeType.Envisalink)) {
                    break;
                }
            case PartitionArmControlAway: /* 030 */
            case PartitionArmControlStay: /* 031 */
            case PartitionArmControlZeroEntryDelay: /* 032 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-8]")) {
                    logger.error("sendCommand(): Partition number must be a single character string from 1 to 8, it was: {}", dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case PartitionArmControlWithUserCode: /* 033 */
            case PartitionDisarmControl: /* 040 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-8]")) {
                    logger.error("sendCommand(): Partition number must be a single character string from 1 to 8, it was: {}", dscAlarmData[0]);
                    break;
                }

                if (userCode == null || userCode.length() < 4 || userCode.length() > 6) {
                    logger.error("sendCommand(): User Code is invalid, must be between 4 and 6 chars: {}", userCode);
                    break;
                }
                data = dscAlarmData[0] + userCode;
                validCommand = true;
                break;
            case VirtualKeypadControl: /* 058 */
                if (!dscAlarmBridgeType.equals(DSCAlarmBridgeType.IT100)) {
                    break;
                }
            case TimeStampControl: /* 055 */
            case TimeDateBroadcastControl: /* 056 */
            case TemperatureBroadcastControl: /* 057 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[0-1]")) {
                    logger.error("sendCommand(): Value must be a single character string of 0 or 1: {}", dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case TriggerPanicAlarm: /* 060 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-8]")) {
                    logger.error("sendCommand(): Partition number must be a single character string from 1 to 8, it was: {}", dscAlarmData[0]);
                    break;
                }

                if (dscAlarmData[1] == null || !dscAlarmData[1].matches("[1-3]")) {
                    logger.error("sendCommand(): FAPcode must be a single character string from 1 to 3, it was: {}", dscAlarmData[1]);
                    break;
                }
                data = dscAlarmData[0] + dscAlarmData[1];
                validCommand = true;
                break;
            case KeyStroke: /* 070 */
                if (dscAlarmData[0] == null || dscAlarmData[0].length() != 1 || !dscAlarmData[0].matches("[0-9]|A|#|\\*")) {
                    logger.error("sendCommand(): \'keystroke\' must be a single character string from 0 to 9, *, #, or A, it was: {}", dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case KeySequence: /* 071 */
                if (!dscAlarmBridgeType.equals(DSCAlarmBridgeType.Envisalink)) {
                    break;
                }

                if (dscAlarmData[0] == null || dscAlarmData[0].length() > 6 || !dscAlarmData[0].matches("(\\d|#|\\*)+")) {
                    logger.error("sendCommand(): \'keysequence\' must be a string of up to 6 characters consiting of 0 to 9, *, or #, it was: {}", dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case CodeSend: /* 200 */

                if (userCode == null || userCode.length() < 4 || userCode.length() > 6) {
                    logger.error("sendCommand(): Access Code is invalid, must be between 4 and 6 chars: {}", dscAlarmData[0]);
                    break;
                }
                data = userCode;
                validCommand = true;
                break;

            default:
                validCommand = false;
                break;

        }

        if (validCommand) {
            String cmd = dscAlarmCommand(command, data);
            write(cmd);
            successful = true;
            logger.debug("sendCommand(): '{}' Command Sent - {}", dscAlarmCode, cmd);
        } else
            logger.error("sendCommand(): Command Not Sent - Invalid!");

        return successful;
    }

    private String dscAlarmCommand(String command, String data) {
        int sum = 0;

        String cmd = command + data;

        for (int i = 0; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            sum += c;
        }

        sum &= 0xFF;

        String strChecksum = Integer.toHexString(sum >> 4) + Integer.toHexString(sum & 0xF);

        return cmd + strChecksum.toUpperCase() + "\r\n";
    }
}
