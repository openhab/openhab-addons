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
package org.openhab.binding.dscalarm.internal.handler;

import static org.openhab.binding.dscalarm.internal.DSCAlarmBindingConstants.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageType;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmPartitionConfiguration;
import org.openhab.binding.dscalarm.internal.config.DSCAlarmZoneConfiguration;
import org.openhab.binding.dscalarm.internal.discovery.DSCAlarmDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for a DSC Alarm Bridge Handler.
 *
 * @author Russell Stephens - Initial Contribution
 */
public abstract class DSCAlarmBaseBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(DSCAlarmBaseBridgeHandler.class);

    /** The DSC Alarm bridge type. */
    private DSCAlarmBridgeType dscAlarmBridgeType = null;

    /** The DSC Alarm bridge type. */
    private DSCAlarmProtocol dscAlarmProtocol = null;

    /** The DSC Alarm Discovery Service. */
    private DSCAlarmDiscoveryService dscAlarmDiscoveryService = null;

    /** The Panel Thing handler for the bridge. */
    private DSCAlarmBaseThingHandler panelThingHandler = null;

    /** Connection status for the bridge. */
    private boolean connected = false;

    /** Determines if things have changed. */
    private boolean thingsHaveChanged = false;

    /** Determines if all things have been initialized. */
    private boolean allThingsInitialized = false;

    /** Thing count. */
    private int thingCount = 0;

    /** Password for bridge connection authentication. */
    private String password = null;

    /** User Code for some DSC Alarm commands. */
    private String userCode = null;

    // Polling variables
    protected int pollPeriod = 0;
    private long pollElapsedTime = 0;
    private long pollStartTime = 0;
    private long refreshInterval = 5000;

    private ScheduledFuture<?> pollingTask;

    /**
     * Constructor.
     *
     * @param bridge
     * @param dscAlarmBridgeType
     */
    DSCAlarmBaseBridgeHandler(Bridge bridge, DSCAlarmBridgeType dscAlarmBridgeType, DSCAlarmProtocol dscAlarmProtocol) {
        super(bridge);
        this.dscAlarmBridgeType = dscAlarmBridgeType;
        this.dscAlarmProtocol = dscAlarmProtocol;
    }

    /**
     * Returns the bridge type.
     */
    public DSCAlarmBridgeType getBridgeType() {
        return dscAlarmBridgeType;
    }

    /**
     * Sets the protocol.
     *
     * @param dscAlarmProtocol
     */
    public void setProtocol(DSCAlarmProtocol dscAlarmProtocol) {
        this.dscAlarmProtocol = dscAlarmProtocol;
    }

    /**
     * Returns the protocol.
     */
    public DSCAlarmProtocol getProtocol() {
        return dscAlarmProtocol;
    }

    /**
     * Sets the bridge type.
     *
     * @param dscAlarmBridgeType
     */
    public void setBridgeType(DSCAlarmBridgeType dscAlarmBridgeType) {
        this.dscAlarmBridgeType = dscAlarmBridgeType;
    }

    @Override
    public void initialize() {
        if (this.pollPeriod > 15) {
            this.pollPeriod = 15;
        } else if (this.pollPeriod < 1) {
            this.pollPeriod = 1;
        }
        updateStatus(ThingStatus.OFFLINE);
        startPolling();
    }

    @Override
    public void dispose() {
        stopPolling();
        closeConnection();
        super.dispose();
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
        openConnection();

        if (isConnected()) {
            if (dscAlarmBridgeType != DSCAlarmBridgeType.Envisalink) {
                onConnected();
            }
        }
    }

    /**
     * Runs when connected.
     */
    public void onConnected() {
        logger.debug("onConnected(): Bridge Connected!");

        setBridgeStatus(true);

        thingsHaveChanged = true;
    }

    /**
     * Disconnect The Bridge.
     */
    private void disconnect() {
        closeConnection();

        if (!isConnected()) {
            setBridgeStatus(false);
        }
    }

    /**
     * Returns Connected.
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Sets Connected.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Set Bridge Status.
     *
     * @param isOnline
     */
    public void setBridgeStatus(boolean isOnline) {
        logger.debug("setBridgeStatus(): Setting Bridge to {}", isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        updateStatus(isOnline ? ThingStatus.ONLINE : ThingStatus.OFFLINE);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), BRIDGE_RESET);
        updateState(channelUID, isOnline ? OnOffType.ON : OnOffType.OFF);
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
     * @param doNotLog
     */
    public abstract void write(String writeString, boolean doNotLog);

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
    private void startPolling() {
        logger.debug("Starting DSC Alarm Polling Task.");
        if (pollingTask == null || pollingTask.isCancelled()) {
            pollingTask = scheduler.scheduleWithFixedDelay(this::polling, 0, refreshInterval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Method to stop the polling task.
     */
    private void stopPolling() {
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
        logger.debug("DSC Alarm Polling Task - '{}' is {}", getThing().getUID(), getThing().getStatus());

        if (isConnected()) {
            if (pollStartTime == 0) {
                pollStartTime = System.currentTimeMillis();
            }

            pollElapsedTime = ((System.currentTimeMillis() - pollStartTime) / 1000) / 60;

            // Send Poll command to the DSC Alarm if idle for 'pollPeriod'
            // minutes
            if (pollElapsedTime >= pollPeriod) {
                sendCommand(DSCAlarmCode.Poll);
                pollStartTime = 0;
            }

            checkThings();

            if (thingsHaveChanged && allThingsInitialized) {
                this.setBridgeStatus(isConnected());
                thingsHaveChanged = false;
                // Get a status report from DSC Alarm.
                sendCommand(DSCAlarmCode.StatusReport);
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
        logger.debug("Checking Things!");

        allThingsInitialized = true;

        List<Thing> things = getThing().getThings();

        if (things.size() != thingCount) {
            thingsHaveChanged = true;
            thingCount = things.size();
        }

        for (Thing thing : things) {

            DSCAlarmBaseThingHandler handler = (DSCAlarmBaseThingHandler) thing.getHandler();

            if (handler != null) {
                logger.debug("***Checking '{}' - Status: {}, Initialized: {}", thing.getUID(), thing.getStatus(),
                        handler.isThingHandlerInitialized());

                if (!handler.isThingHandlerInitialized() || thing.getStatus() != ThingStatus.ONLINE) {
                    allThingsInitialized = false;
                }

                if (handler.getDSCAlarmThingType().equals(DSCAlarmThingType.PANEL)) {
                    if (panelThingHandler == null) {
                        panelThingHandler = handler;
                    }
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
                                    logger.debug("findThing(): Thing Found - {}, {}, {}", t, handler,
                                            handlerDSCAlarmThingType);
                                    return thing;
                                case PARTITION:
                                    BigDecimal partitionNumber = (BigDecimal) config
                                            .get(DSCAlarmPartitionConfiguration.PARTITION_NUMBER);
                                    if (partitionId == partitionNumber.intValue()) {
                                        thing = t;
                                        logger.debug("findThing(): Thing Found - {}, {}, {}", t, handler,
                                                handlerDSCAlarmThingType);
                                        return thing;
                                    }
                                    break;
                                case ZONE:
                                    BigDecimal zoneNumber = (BigDecimal) config
                                            .get(DSCAlarmZoneConfiguration.ZONE_NUMBER);
                                    if (zoneId == zoneNumber.intValue()) {
                                        thing = t;
                                        logger.debug("findThing(): Thing Found - {}, {}, {}", t, handler,
                                                handlerDSCAlarmThingType);
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
                logger.debug("findThing(): Error Seaching Thing - {} ", e.getMessage(), e);
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
        if (incomingMessage != null && !incomingMessage.isEmpty()) {
            DSCAlarmMessage dscAlarmMessage = new DSCAlarmMessage(incomingMessage);
            DSCAlarmMessageType dscAlarmMessageType = dscAlarmMessage.getDSCAlarmMessageType();

            logger.debug("handleIncomingMessage(): Message received: {} - {}", incomingMessage,
                    dscAlarmMessage.toString());

            DSCAlarmEvent event = new DSCAlarmEvent(this);
            event.dscAlarmEventMessage(dscAlarmMessage);
            DSCAlarmThingType dscAlarmThingType = null;
            int partitionId = 0;
            int zoneId = 0;

            DSCAlarmCode dscAlarmCode = DSCAlarmCode
                    .getDSCAlarmCodeValue(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));

            if (panelThingHandler != null) {
                panelThingHandler.setPanelMessage(dscAlarmMessage);
            }

            if (dscAlarmCode == DSCAlarmCode.LoginResponse) {
                String dscAlarmMessageData = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);
                if (dscAlarmMessageData.equals("3")) {
                    sendCommand(DSCAlarmCode.NetworkLogin);
                    // onConnected();
                } else if (dscAlarmMessageData.equals("1")) {
                    onConnected();
                }
                return;
            } else if (dscAlarmCode == DSCAlarmCode.CommandAcknowledge) {
                String dscAlarmMessageData = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DATA);
                if (dscAlarmMessageData.equals("000")) {
                    setBridgeStatus(true);
                }
            }

            switch (dscAlarmMessageType) {
                case PANEL_EVENT:
                    dscAlarmThingType = DSCAlarmThingType.PANEL;
                    break;
                case PARTITION_EVENT:
                    dscAlarmThingType = DSCAlarmThingType.PARTITION;
                    partitionId = Integer
                            .parseInt(event.getDSCAlarmMessage().getMessageInfo(DSCAlarmMessageInfoType.PARTITION));
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
                        if (thingHandler.isThingHandlerInitialized() && thing.getStatus() == ThingStatus.ONLINE) {
                            thingHandler.dscAlarmEventReceived(event, thing);

                        } else {
                            logger.debug("handleIncomingMessage(): Thing '{}' Not Refreshed!", thing.getUID());
                        }
                    }
                } else {
                    logger.debug("handleIncomingMessage(): Thing Not Found! Send to Discovery Service!");

                    if (dscAlarmDiscoveryService != null) {
                        dscAlarmDiscoveryService.addThing(getThing(), dscAlarmThingType, event);
                    }
                }
            }
        } else {
            logger.debug("handleIncomingMessage(): No Message Received!");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        if (isConnected()) {
            switch (channelUID.getId()) {
                case BRIDGE_RESET:
                    if (command == OnOffType.OFF) {
                        disconnect();
                    }
                    break;
                case SEND_COMMAND:
                    if (!command.toString().isEmpty()) {
                        String[] tokens = command.toString().split(",");

                        String cmd = tokens[0];
                        String data = "";
                        if (tokens.length > 1) {
                            data = tokens[1];
                        }

                        sendDSCAlarmCommand(cmd, data);

                        updateState(channelUID, new StringType(""));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Method to send a sequence of key presses one at a time using the '070' command.
     *
     * @param keySequence
     */
    private boolean sendKeySequence(String keySequence) {
        logger.debug("sendKeySequence(): Sending key sequence '{}'.", keySequence);

        boolean sent = false;

        for (char key : keySequence.toCharArray()) {
            sent = sendCommand(DSCAlarmCode.KeyStroke, String.valueOf(key));

            if (!sent) {
                return sent;
            }
        }

        return sent;
    }

    /**
     * Sends a DSC Alarm command
     *
     * @param command
     * @param data
     */
    public boolean sendDSCAlarmCommand(String command, String data) {
        logger.debug("sendDSCAlarmCommand(): Attempting to send DSC Alarm Command: command - {} - data: {}", command,
                data);

        DSCAlarmCode dscAlarmCode = DSCAlarmCode.getDSCAlarmCodeValue(command);

        if (dscAlarmProtocol.equals(DSCAlarmProtocol.IT100_API) && dscAlarmCode.equals(DSCAlarmCode.KeySequence)) {
            return sendKeySequence(data);
        } else {
            return sendCommand(dscAlarmCode, data);
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
        boolean confidentialData = false;

        switch (dscAlarmCode) {
            case Poll: /* 000 */
            case StatusReport: /* 001 */
                validCommand = true;
                break;
            case LabelsRequest: /* 002 */
                if (!dscAlarmProtocol.equals(DSCAlarmProtocol.IT100_API)) {
                    break;
                }
                validCommand = true;
                break;
            case NetworkLogin: /* 005 */
                if (!dscAlarmProtocol.equals(DSCAlarmProtocol.ENVISALINK_TPI)) {
                    break;
                }

                if (password == null) {
                    logger.error("sendCommand(): No password!");
                    break;
                }
                data = password;
                confidentialData = true;
                validCommand = true;
                break;
            case DumpZoneTimers: /* 008 */
                if (!dscAlarmProtocol.equals(DSCAlarmProtocol.ENVISALINK_TPI)) {
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
                    logger.error(
                            "sendCommand(): Partition number must be a single character string from 1 to 8, it was: {}",
                            dscAlarmData[0]);
                    break;
                }

                if (dscAlarmData[1] == null || !dscAlarmData[1].matches("[1-4]")) {
                    logger.error(
                            "sendCommand(): Output number must be a single character string from 1 to 4, it was: {}",
                            dscAlarmData[1]);
                    break;
                }

                data = dscAlarmData[0];
                validCommand = true;
                break;
            case KeepAlive: /* 074 */
                if (!dscAlarmProtocol.equals(DSCAlarmProtocol.ENVISALINK_TPI)) {
                    break;
                }
            case PartitionArmControlAway: /* 030 */
            case PartitionArmControlStay: /* 031 */
            case PartitionArmControlZeroEntryDelay: /* 032 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-8]")) {
                    logger.error(
                            "sendCommand(): Partition number must be a single character string from 1 to 8, it was: {}",
                            dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case PartitionArmControlWithUserCode: /* 033 */
            case PartitionDisarmControl: /* 040 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-8]")) {
                    logger.error(
                            "sendCommand(): Partition number must be a single character string from 1 to 8, it was: {}",
                            dscAlarmData[0]);
                    break;
                }

                if (userCode == null || userCode.length() < 4 || userCode.length() > 6) {
                    logger.error("sendCommand(): User Code is invalid, must be between 4 and 6 chars");
                    break;
                }

                if (dscAlarmProtocol.equals(DSCAlarmProtocol.IT100_API)) {
                    data = dscAlarmData[0] + String.format("%-6s", userCode).replace(' ', '0');
                } else {
                    data = dscAlarmData[0] + userCode;
                }

                confidentialData = true;
                validCommand = true;
                break;
            case VirtualKeypadControl: /* 058 */
                if (!dscAlarmProtocol.equals(DSCAlarmProtocol.IT100_API)) {
                    break;
                }
            case TimeStampControl: /* 055 */
            case TimeDateBroadcastControl: /* 056 */
            case TemperatureBroadcastControl: /* 057 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[0-1]")) {
                    logger.error("sendCommand(): Value must be a single character string of 0 or 1: {}",
                            dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case TriggerPanicAlarm: /* 060 */
                if (dscAlarmData[0] == null || !dscAlarmData[0].matches("[1-3]")) {
                    logger.error("sendCommand(): FAPcode must be a single character string from 1 to 3, it was: {}",
                            dscAlarmData[1]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case KeyStroke: /* 070 */
                if (dscAlarmProtocol.equals(DSCAlarmProtocol.ENVISALINK_TPI)) {
                    if (dscAlarmData[0] == null || dscAlarmData[0].length() != 1
                            || !dscAlarmData[0].matches("[0-9]|A|#|\\*")) {
                        logger.error(
                                "sendCommand(): \'keystroke\' must be a single character string from 0 to 9, *, #, or A, it was: {}",
                                dscAlarmData[0]);
                        break;
                    }
                } else if (dscAlarmProtocol.equals(DSCAlarmProtocol.IT100_API)) {
                    if (dscAlarmData[0] == null || dscAlarmData[0].length() != 1
                            || !dscAlarmData[0].matches("[0-9]|\\*|#|F|A|P|[a-e]|<|>|=|\\^|L")) {
                        logger.error(
                                "sendCommand(): \'keystroke\' must be a single character string from 0 to 9, *, #, F, A, P, a to e, <, >, =, or ^, it was: {}",
                                dscAlarmData[0]);
                        break;
                    } else if (dscAlarmData[0].equals("L")) { /* Long Key Press */
                        try {
                            Thread.sleep(1500);
                            data = "^";
                            validCommand = true;
                            break;
                        } catch (InterruptedException e) {
                            logger.error("sendCommand(): \'keystroke\': Error with Long Key Press!");
                            break;
                        }
                    }
                } else {
                    break;
                }

                data = dscAlarmData[0];
                validCommand = true;
                break;
            case KeySequence: /* 071 */
                if (!dscAlarmProtocol.equals(DSCAlarmProtocol.ENVISALINK_TPI)) {
                    break;
                }

                if (dscAlarmData[0] == null || dscAlarmData[0].length() > 6
                        || !dscAlarmData[0].matches("(\\d|#|\\*)+")) {
                    logger.error(
                            "sendCommand(): \'keysequence\' must be a string of up to 6 characters consiting of 0 to 9, *, or #, it was: {}",
                            dscAlarmData[0]);
                    break;
                }
                data = dscAlarmData[0];
                validCommand = true;
                break;
            case CodeSend: /* 200 */
                if (userCode == null || userCode.length() < 4 || userCode.length() > 6) {
                    logger.error("sendCommand(): Access Code is invalid, must be between 4 and 6 chars");
                    break;
                }

                data = userCode;
                confidentialData = true;
                validCommand = true;
                break;

            default:
                validCommand = false;
                break;

        }

        if (validCommand) {
            String cmd = dscAlarmCommand(command, data);
            write(cmd, confidentialData);
            successful = true;
            logger.debug("sendCommand(): '{}' Command Sent - {}", dscAlarmCode, confidentialData ? "***" : cmd);
        } else {
            logger.error("sendCommand(): Command '{}' Not Sent - Invalid!", dscAlarmCode);
        }

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
