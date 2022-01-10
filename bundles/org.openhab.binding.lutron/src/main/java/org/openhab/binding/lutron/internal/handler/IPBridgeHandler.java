/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.handler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.lutron.internal.config.IPBridgeConfig;
import org.openhab.binding.lutron.internal.discovery.LutronDeviceDiscoveryService;
import org.openhab.binding.lutron.internal.net.TelnetSession;
import org.openhab.binding.lutron.internal.net.TelnetSessionListener;
import org.openhab.binding.lutron.internal.protocol.LIPCommand;
import org.openhab.binding.lutron.internal.protocol.LutronCommandNew;
import org.openhab.binding.lutron.internal.protocol.lip.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.lip.LutronOperation;
import org.openhab.binding.lutron.internal.protocol.lip.Monitoring;
import org.openhab.binding.lutron.internal.protocol.lip.TargetType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the main Lutron control hub.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added reconnect and heartbeat config parameters, moved discovery service registration to
 *         LutronHandlerFactory
 */
public class IPBridgeHandler extends LutronBridgeHandler {
    private static final Pattern RESPONSE_REGEX = Pattern
            .compile("~(OUTPUT|DEVICE|SYSTEM|TIMECLOCK|MODE|SYSVAR|GROUP),([0-9\\.:/]+),([0-9,\\.:/]*)\\Z");

    private static final String DB_UPDATE_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    private static final Integer SYSTEM_DBEXPORTDATETIME = 10;

    private static final int MAX_LOGIN_ATTEMPTS = 2;

    private static final String PROMPT_GNET = "GNET>";
    private static final String PROMPT_QNET = "QNET>";
    private static final String PROMPT_SAFE = "SAFE>";
    private static final String LOGIN_MATCH_REGEX = "(login:|[GQ]NET>|SAFE>)";

    private static final String DEFAULT_USER = "lutron";
    private static final String DEFAULT_PASSWORD = "integration";
    private static final int DEFAULT_RECONNECT_MINUTES = 5;
    private static final int DEFAULT_HEARTBEAT_MINUTES = 5;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(IPBridgeHandler.class);

    private IPBridgeConfig config;
    private int reconnectInterval;
    private int heartbeatInterval;
    private int sendDelay;

    private TelnetSession session;
    private BlockingQueue<LutronCommandNew> sendQueue = new LinkedBlockingQueue<>();

    private Thread messageSender;
    private ScheduledFuture<?> keepAlive;
    private ScheduledFuture<?> keepAliveReconnect;
    private ScheduledFuture<?> connectRetryJob;

    private Date lastDbUpdateDate;
    private LutronDeviceDiscoveryService discoveryService;

    private final AtomicBoolean requireSysvarMonitoring = new AtomicBoolean(false);

    public void setDiscoveryService(LutronDeviceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public class LutronSafemodeException extends Exception {
        private static final long serialVersionUID = 1L;

        public LutronSafemodeException(String message) {
            super(message);
        }
    }

    public IPBridgeConfig getIPBridgeConfig() {
        return config;
    }

    public IPBridgeHandler(Bridge bridge) {
        super(bridge);

        this.session = new TelnetSession();

        this.session.addListener(new TelnetSessionListener() {
            @Override
            public void inputAvailable() {
                parseUpdates();
            }

            @Override
            public void error(IOException exception) {
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(IPBridgeConfig.class);

        if (validConfiguration(this.config)) {
            reconnectInterval = (config.reconnect > 0) ? config.reconnect : DEFAULT_RECONNECT_MINUTES;
            heartbeatInterval = (config.heartbeat > 0) ? config.heartbeat : DEFAULT_HEARTBEAT_MINUTES;
            sendDelay = (config.delay < 0) ? 0 : config.delay;

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Connecting");
            scheduler.submit(this::connect); // start the async connect task
        }
    }

    private boolean validConfiguration(IPBridgeConfig config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge configuration missing");

            return false;
        }

        String ipAddress = config.ipAddress;
        if (ipAddress == null || ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge address not specified");

            return false;
        }

        return true;
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    private synchronized void connect() {
        if (this.session.isConnected()) {
            return;
        }

        logger.debug("Connecting to bridge at {}", config.ipAddress);

        try {
            if (!login(config)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid username/password");

                return;
            }
        } catch (LutronSafemodeException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "main repeater is in safe mode");
            disconnect();
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.

            return;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.

            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "login interrupted");
            disconnect();

            return;
        }

        updateStatus(ThingStatus.ONLINE);

        // Disable prompts
        sendCommand(new LIPCommand(TargetType.BRIDGE, LutronOperation.EXECUTE, LutronCommandType.MONITORING, null,
                Monitoring.PROMPT, Monitoring.ACTION_DISABLE));

        initMonitoring();
        if (requireSysvarMonitoring.get()) {
            setSysvarMonitoring(true);
        }

        // Check the time device database was last updated. On the initial connect, this will trigger
        // a scan for paired devices.
        sendCommand(new LIPCommand(TargetType.BRIDGE, LutronOperation.QUERY, LutronCommandType.SYSTEM, null,
                SYSTEM_DBEXPORTDATETIME));

        messageSender = new Thread(this::sendCommandsThread, "Lutron sender");
        messageSender.start();

        logger.debug("Starting keepAlive job with interval {}", heartbeatInterval);
        keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, heartbeatInterval, heartbeatInterval,
                TimeUnit.MINUTES);
    }

    private void sendCommandsThread() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                LutronCommandNew command = sendQueue.take();

                logger.debug("Sending command {}", command);

                try {
                    session.writeLine(command.toString());
                } catch (IOException e) {
                    logger.warn("Communication error, will try to reconnect. Error: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

                    sendQueue.add(command); // Requeue command

                    reconnect();

                    // reconnect() will start a new thread; terminate this one
                    break;
                }
                if (sendDelay > 0) {
                    Thread.sleep(sendDelay); // introduce delay to throttle send rate
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void disconnect() {
        logger.debug("Disconnecting from bridge");

        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }

        if (this.keepAlive != null) {
            this.keepAlive.cancel(true);
        }

        if (this.keepAliveReconnect != null) {
            // This method can be called from the keepAliveReconnect thread. Make sure
            // we don't interrupt ourselves, as that may prevent the reconnection attempt.
            this.keepAliveReconnect.cancel(false);
        }

        if (messageSender != null && messageSender.isAlive()) {
            messageSender.interrupt();
        }

        try {
            this.session.close();
        } catch (IOException e) {
            logger.warn("Error disconnecting: {}", e.getMessage());
        }
    }

    private synchronized void reconnect() {
        logger.debug("Keepalive timeout, attempting to reconnect to the bridge");

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE);
        disconnect();
        connect();
    }

    private boolean login(IPBridgeConfig config) throws IOException, InterruptedException, LutronSafemodeException {
        this.session.open(config.ipAddress);
        this.session.waitFor("login:");

        // Sometimes the Lutron Smart Bridge Pro will request login more than once.
        for (int attempt = 0; attempt < MAX_LOGIN_ATTEMPTS; attempt++) {
            this.session.writeLine(config.user != null ? config.user : DEFAULT_USER);
            this.session.waitFor("password:");
            this.session.writeLine(config.password != null ? config.password : DEFAULT_PASSWORD);

            MatchResult matchResult = this.session.waitFor(LOGIN_MATCH_REGEX);

            if (PROMPT_GNET.equals(matchResult.group()) || PROMPT_QNET.equals(matchResult.group())) {
                return true;
            } else if (PROMPT_SAFE.equals(matchResult.group())) {
                logger.warn("Lutron repeater is in safe mode. Unable to connect.");
                throw new LutronSafemodeException("Lutron repeater in safe mode");
            }

            else {
                logger.debug("got another login prompt, logging in again");
                // we already got the login prompt so go straight to sending user
            }
        }
        return false;
    }

    @Override
    public void sendCommand(LutronCommandNew command) {
        sendQueue.add(command);
    }

    private LutronHandler findThingHandler(int integrationId) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof LutronHandler) {
                LutronHandler handler = (LutronHandler) thing.getHandler();

                try {
                    if (handler != null && handler.getIntegrationId() == integrationId) {
                        return handler;
                    }
                } catch (IllegalStateException e) {
                    logger.trace("Handler for id {} not initialized", integrationId);
                }
            }
        }

        return null;
    }

    private void parseUpdates() {
        String paramString;
        String scrubbedLine;

        for (String line : this.session.readLines()) {
            if (line.trim().equals("")) {
                // Sometimes we get an empty line (possibly only when prompts are disabled). Ignore them.
                continue;
            }

            logger.debug("Received message {}", line);

            // System is alive, cancel reconnect task.
            if (this.keepAliveReconnect != null) {
                this.keepAliveReconnect.cancel(true);
            }

            Matcher matcher = RESPONSE_REGEX.matcher(line);
            boolean responseMatched = matcher.find();

            if (!responseMatched) {
                // In some cases with Caseta a CLI prompt may be embedded within a received response line.
                if (line.contains("NET>")) {
                    // Try to remove it and re-attempt the regex match.
                    scrubbedLine = line.replaceAll("[GQ]NET> ", "");
                    matcher = RESPONSE_REGEX.matcher(scrubbedLine);
                    responseMatched = matcher.find();
                    if (responseMatched) {
                        line = scrubbedLine;
                        logger.debug("Cleaned response line: {}", scrubbedLine);
                    }
                }
            }

            if (!responseMatched) {
                logger.debug("Ignoring message {}", line);
                continue;
            } else {
                // We have a good response message
                LutronCommandType type = LutronCommandType.valueOf(matcher.group(1));

                if (type == LutronCommandType.SYSTEM) {
                    // SYSTEM messages are assumed to be a response to the SYSTEM_DBEXPORTDATETIME
                    // query. The response returns the last time the device database was updated.
                    setDbUpdateDate(matcher.group(2), matcher.group(3));

                    continue;
                }

                Integer integrationId;

                try {
                    integrationId = Integer.valueOf(matcher.group(2));
                } catch (NumberFormatException e1) {
                    logger.warn("Integer conversion error parsing update: {}", line);
                    continue;
                }
                paramString = matcher.group(3);

                // Now dispatch update to the proper thing handler
                LutronHandler handler = findThingHandler(integrationId);

                if (handler != null) {
                    try {
                        handler.handleUpdate(type, paramString.split(","));
                    } catch (NumberFormatException e) {
                        logger.warn("Number format exception parsing update: {}", line);
                    } catch (RuntimeException e) {
                        logger.warn("Runtime exception while processing update: {}", line, e);
                    }
                } else {
                    logger.debug("No thing configured for integration ID {}", integrationId);
                }
            }
        }
    }

    private void sendKeepAlive() {
        logger.debug("Scheduling keepalive reconnect job");

        // Reconnect if no response is received within 30 seconds.
        keepAliveReconnect = scheduler.schedule(this::reconnect, KEEPALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        logger.trace("Sending keepalive query");
        sendCommand(new LIPCommand(TargetType.BRIDGE, LutronOperation.QUERY, LutronCommandType.SYSTEM, null,
                SYSTEM_DBEXPORTDATETIME));
    }

    private void setDbUpdateDate(String dateString, String timeString) {
        try {
            Date date = new SimpleDateFormat(DB_UPDATE_DATE_FORMAT).parse(dateString + " " + timeString);

            if (this.lastDbUpdateDate == null || date.after(this.lastDbUpdateDate)) {
                scanForDevices();

                this.lastDbUpdateDate = date;
            }
        } catch (ParseException e) {
            logger.warn("Failed to parse DB update date {} {}", dateString, timeString);
        }
    }

    private void scanForDevices() {
        try {
            if (discoveryService != null) {
                logger.debug("Initiating discovery scan for devices");
                discoveryService.startScan(null);
            } else {
                logger.debug("Unable to initiate discovery because discoveryService is null");
            }
        } catch (Exception e) {
            logger.warn("Error scanning for paired devices: {}", e.getMessage(), e);
        }
    }

    private void initMonitoring() {
        for (Integer monitorType : Monitoring.REQUIRED_SET) {
            sendCommand(new LIPCommand(TargetType.BRIDGE, LutronOperation.EXECUTE, LutronCommandType.MONITORING, null,
                    monitorType, Monitoring.ACTION_ENABLE));
        }
    }

    private void setSysvarMonitoring(boolean enable) {
        Integer setting = (enable) ? Monitoring.ACTION_ENABLE : Monitoring.ACTION_DISABLE;
        sendCommand(new LIPCommand(TargetType.BRIDGE, LutronOperation.EXECUTE, LutronCommandType.MONITORING, null,
                Monitoring.SYSVAR, setting));
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        // enable sysvar monitoring the first time a sysvar child thing initializes
        if (childHandler instanceof SysvarHandler) {
            if (requireSysvarMonitoring.compareAndSet(false, true)) {
                setSysvarMonitoring(true);
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        IPBridgeConfig newConfig = thing.getConfiguration().as(IPBridgeConfig.class);
        boolean validConfig = validConfiguration(newConfig);
        boolean needsReconnect = validConfig && !this.config.sameConnectionParameters(newConfig);

        if (!validConfig || needsReconnect) {
            dispose();
        }

        this.thing = thing;
        this.config = newConfig;

        if (needsReconnect) {
            initialize();
        }
    }

    @Override
    public void dispose() {
        disconnect();
    }
}
