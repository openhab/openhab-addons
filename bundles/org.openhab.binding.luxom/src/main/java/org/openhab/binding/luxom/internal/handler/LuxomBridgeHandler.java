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
package org.openhab.binding.luxom.internal.handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxom.internal.handler.config.LuxomBridgeConfig;
import org.openhab.binding.luxom.internal.protocol.LuxomAction;
import org.openhab.binding.luxom.internal.protocol.LuxomCommand;
import org.openhab.binding.luxom.internal.protocol.LuxomCommunication;
import org.openhab.binding.luxom.internal.protocol.LuxomSystemInfo;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the main Luxom IP access module.
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class LuxomBridgeHandler extends BaseBridgeHandler {
    public static final int HEARTBEAT_INTERVAL_SECONDS = 50;
    private final LuxomSystemInfo systemInfo;

    private static final int DEFAULT_RECONNECT_INTERVAL_IN_MINUTES = 1;
    private static final long HEARTBEAT_ACK_TIMEOUT_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(LuxomBridgeHandler.class);

    private @Nullable LuxomBridgeConfig config;
    private final AtomicInteger nrOfSendPermits = new AtomicInteger(0);
    private int reconnectInterval;

    private @Nullable LuxomCommand previousCommand;
    private final LuxomCommunication communication;
    private final BlockingQueue<List<CommandExecutionSpecification>> sendQueue = new LinkedBlockingQueue<>();

    private @Nullable Thread messageSender;
    private @Nullable ScheduledFuture<?> heartBeat;
    private @Nullable ScheduledFuture<?> heartBeatTimeoutTask;
    private @Nullable ScheduledFuture<?> connectRetryJob;

    public @Nullable LuxomBridgeConfig getIPBridgeConfig() {
        return config;
    }

    public LuxomBridgeHandler(Bridge bridge) {
        super(bridge);
        logger.debug("Luxom bridge init");
        systemInfo = new LuxomSystemInfo();
        communication = new LuxomCommunication(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Bridge received command {} for {}", command.toFullString(), channelUID);
    }

    @Override
    public void initialize() {
        config = getConfig().as(LuxomBridgeConfig.class);

        if (validConfiguration(config)) {
            reconnectInterval = (config.reconnectInterval > 0) ? config.reconnectInterval
                    : DEFAULT_RECONNECT_INTERVAL_IN_MINUTES;

            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.connecting");
            scheduler.submit(this::connect); // start the async connect task
        }
    }

    private boolean validConfiguration(@Nullable LuxomBridgeConfig config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/bridge-configuration-missing");

            return false;
        }

        if (config.ipAddress == null || config.ipAddress.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/bridge-address-missing");

            return false;
        }

        return true;
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} (minutes)", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    private synchronized void connect() {
        if (communication.isConnected()) {
            return;
        }

        if (config != null) {
            logger.debug("Connecting to bridge at {}", config.ipAddress);
        }

        try {
            communication.startCommunication();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.
        }
    }

    public void startProcessing() {
        nrOfSendPermits.set(1);

        updateStatus(ThingStatus.ONLINE);

        messageSender = new Thread(this::sendCommandsThread, "OH-binding-" + getThing().getUID() + "-Sender");
        messageSender.start();

        logger.debug("Starting heartbeat job with interval {} (seconds)", HEARTBEAT_INTERVAL_SECONDS);
        heartBeat = scheduler.scheduleWithFixedDelay(this::sendHeartBeat, 10, HEARTBEAT_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    private void sendCommandsThread() {
        logger.debug("Starting send commands thread...");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                logger.debug("waiting for command to send...");
                List<CommandExecutionSpecification> commands = sendQueue.take();

                try {
                    for (CommandExecutionSpecification commandExecutionSpecification : commands) {
                        communication.sendMessage(commandExecutionSpecification.getCommand());
                    }
                } catch (IOException e) {
                    logger.warn("Communication error while sending, will try to reconnect. Error: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

                    reconnect();

                    // reconnect() will start a new thread; terminate this one
                    break;
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

        if (this.heartBeat != null) {
            this.heartBeat.cancel(true);
        }

        cancelCheckAliveTimeoutTask();

        if (messageSender != null && messageSender.isAlive()) {
            messageSender.interrupt();
        }

        this.communication.stopCommunication();
    }

    public void reconnect() {
        reconnect(false);
    }

    private synchronized void reconnect(boolean timeout) {
        if (timeout) {
            logger.debug("Keepalive timeout, attempting to reconnect to the bridge");
        } else {
            logger.debug("Connection problem, attempting to reconnect to the bridge");
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        disconnect();
        connect();
    }

    public void sendCommands(List<CommandExecutionSpecification> commands) {
        this.sendQueue.add(commands);
    }

    private @Nullable LuxomThingHandler findThingHandler(@Nullable String address) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof LuxomThingHandler) {
                LuxomThingHandler handler = (LuxomThingHandler) thing.getHandler();

                try {
                    if (handler != null && handler.getAddress().equals(address)) {
                        return handler;
                    }
                } catch (IllegalStateException e) {
                    logger.trace("Handler for id {} not initialized", address);
                }
            }
        }

        return null;
    }

    /**
     * needed with fast reconnect to update status of things
     */
    public void forceRefreshThings() {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof LuxomThingHandler) {
                LuxomThingHandler handler = (LuxomThingHandler) thing.getHandler();
                handler.ping();
            }
        }
    }

    private void sendHeartBeat() {
        logger.trace("Sending heartbeat");
        // Reconnect if no response is received within KEEPALIVE_TIMEOUT_SECONDS.
        heartBeatTimeoutTask = scheduler.schedule(() -> reconnect(true), HEARTBEAT_ACK_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);
        sendCommands(List.of(new CommandExecutionSpecification(LuxomAction.HEARTBEAT.getCommand())));
    }

    @Override
    public void thingUpdated(Thing thing) {
        LuxomBridgeConfig newConfig = thing.getConfiguration().as(LuxomBridgeConfig.class);
        boolean validConfig = validConfiguration(newConfig);
        boolean needsReconnect = validConfig && config != null && !config.sameConnectionParameters(newConfig);

        if (!validConfig || needsReconnect) {
            dispose();
        }

        this.thing = thing;
        this.config = newConfig;

        if (needsReconnect) {
            initialize();
        }
    }

    public void handleCommunicationError(IOException e) {
        logger.debug("Communication error while reading, will try to reconnect. Error: {}", e.getMessage());
        reconnect();
    }

    @Override
    public void dispose() {
        disconnect();
    }

    public void handleIncomingLuxomMessage(String luxomMessage) throws IOException {
        cancelCheckAliveTimeoutTask(); // we got a message

        logger.trace("Luxom: received {}", luxomMessage);
        LuxomCommand luxomCommand = new LuxomCommand(luxomMessage);

        // Now dispatch update to the proper thing handler

        if (LuxomAction.PASSWORD_REQUEST == luxomCommand.getAction()) {
            communication.sendMessage(LuxomAction.REQUEST_FOR_INFORMATION.getCommand()); // direct send, no queue, so
            // no tcp flow constraint
        } else if (LuxomAction.MODULE_INFORMATION == luxomCommand.getAction()) {
            cmdSystemInfo(luxomCommand.getData());
            if (ThingStatus.ONLINE != getThing().getStatus()) {
                // this all happens before TCP flow controle, when startProcessing is called, TCP flow is activated...
                startProcessing();
            }
        } else if (LuxomAction.ACKNOWLEDGE == luxomCommand.getAction()) {
            logger.trace("received acknowledgement");
        } else if (LuxomAction.DATA == luxomCommand.getAction()
                || LuxomAction.DATA_RESPONSE == luxomCommand.getAction()) {
            previousCommand = luxomCommand;
        } else if (LuxomAction.INVALID_ACTION != luxomCommand.getAction()) {
            if (LuxomAction.DATA_BYTE == luxomCommand.getAction()
                    || LuxomAction.DATA_BYTE_RESPONSE == luxomCommand.getAction()) {
                // data for previous command if it needs it
                if (previousCommand != null && previousCommand.getAction().isNeedsData()) {
                    previousCommand.setData(luxomCommand.getData());
                    luxomCommand = previousCommand;
                    previousCommand = null;
                }
            }

            if (luxomCommand != null) {
                LuxomThingHandler handler = findThingHandler(luxomCommand.getAddress());

                if (handler != null) {
                    handler.handleCommandComingFromBridge(luxomCommand);
                } else {
                    logger.warn("No handler found command {} for address : {}", luxomMessage,
                            luxomCommand.getAddress());
                }
            } else {
                logger.warn("Something was wrong with the order of incoming commands, resulting command is null");
            }
        } else {
            logger.trace("Luxom: not handled {}", luxomMessage);
        }
        logger.trace("nrOfPermits after receive: {}", nrOfSendPermits.get());
    }

    private void cancelCheckAliveTimeoutTask() {
        var task = heartBeatTimeoutTask;
        if (task != null) {
            // This method can be called from the keepAliveReconnect thread. Make sure
            // we don't interrupt ourselves, as that may prevent the reconnection attempt.
            task.cancel(false);
        }
    }

    private synchronized void cmdSystemInfo(@Nullable String info) {
        systemInfo.setSwVersion(info);
    }
}
