/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.handler;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.config.IPBridgeConfig;
import org.openhab.binding.lutron.internal.net.TelnetSession;
import org.openhab.binding.lutron.internal.net.TelnetSessionListener;
import org.openhab.binding.lutron.internal.protocol.LutronCommand;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.LutronOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the main Lutron control hub.
 *
 * @author Allan Tong - Initial contribution
 */
public class IPBridgeHandler extends BaseBridgeHandler {
    private static final Pattern STATUS_REGEX = Pattern.compile("~(OUTPUT|DEVICE|SYSTEM),(\\d+),(.*)");

    private static final Integer MONITOR_PROMPT = 12;
    private static final Integer MONITOR_DISABLE = 2;

    private Logger logger = LoggerFactory.getLogger(IPBridgeHandler.class);

    private TelnetSession session;
    private BlockingQueue<LutronCommand> sendQueue = new LinkedBlockingQueue<>();

    private ScheduledFuture<?> messageSender;
    private ScheduledFuture<?> keepAlive;
    private ScheduledFuture<?> keepAliveReconnect;

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
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, 0, TimeUnit.SECONDS);

        this.keepAlive = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendKeepAlive();
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private synchronized void connect() {
        if (this.session.isConnected()) {
            return;
        }

        IPBridgeConfig config = getThing().getConfiguration().as(IPBridgeConfig.class);

        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge configuration missing");

            return;
        }

        if (StringUtils.isEmpty(config.getIpAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge address not specified");

            return;
        }

        this.logger.debug("Connecting to bridge at " + config.getIpAddress());

        try {
            if (!login(config)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid username/password");

                return;
            }

            // Disable prompts
            sendCommand(new LutronCommand(LutronOperation.EXECUTE, LutronCommandType.MONITORING, -1, MONITOR_PROMPT,
                    MONITOR_DISABLE));
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();

            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "login interrupted");
            disconnect();

            return;
        }

        this.messageSender = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                sendCommands();
            }
        }, 0, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
    }

    private void sendCommands() {
        try {
            while (true) {
                LutronCommand command = this.sendQueue.take();

                this.logger.debug("Sending command " + command.toString());

                try {
                    this.session.writeLine(command.toString());
                } catch (IOException e) {
                    this.logger.error("Communication error, will try to reconnect", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

                    // Requeue command
                    this.sendQueue.add(command);

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
        this.logger.debug("Disconnecting from bridge");

        if (this.messageSender != null) {
            this.messageSender.cancel(true);
        }

        try {
            this.session.close();
        } catch (IOException e) {
            this.logger.error("Error disconnecting", e);
        }

        updateStatus(ThingStatus.OFFLINE);
    }

    private synchronized void reconnect() {
        this.logger.debug("Keepalive timeout, attempting to reconnect to the bridge");

        disconnect();
        connect();
    }

    private boolean login(IPBridgeConfig config) throws IOException, InterruptedException {
        this.session.open(config.getIpAddress());

        this.session.waitFor("login:");
        this.session.writeLine(config.getUser());
        this.session.waitFor("password:");
        this.session.writeLine(config.getPassword());

        MatchResult matchResult = this.session.waitFor("(login:|GNET>)");

        return "GNET>".equals(matchResult.group());
    }

    void sendCommand(LutronCommand command) {
        this.sendQueue.add(command);
    }

    private LutronHandler findThingHandler(int integrationId) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof LutronHandler) {
                LutronHandler handler = (LutronHandler) thing.getHandler();

                if (handler.getIntegrationId() == integrationId) {
                    return handler;
                }
            }
        }

        return null;
    }

    private void parseUpdates() {
        for (String line : this.session.readLines()) {
            if (line.trim().equals("")) {
                // Sometimes we get an empty line (possibly only when prompts are disabled). Ignore them.
                continue;
            }

            this.logger.debug("Received message " + line);

            Matcher matcher = STATUS_REGEX.matcher(line);

            if (matcher.matches()) {
                LutronCommandType type = LutronCommandType.valueOf(matcher.group(1));

                if (type == LutronCommandType.SYSTEM) {
                    // SYSTEM messages are assumed to be a response to a keep alive message.
                    // Cancel reconnect task.
                    if (this.keepAliveReconnect != null) {
                        this.keepAliveReconnect.cancel(true);
                    }

                    continue;
                }

                Integer integrationId = new Integer(matcher.group(2));
                LutronHandler handler = findThingHandler(integrationId);

                if (handler != null) {
                    String paramString = matcher.group(3);

                    try {
                        handler.handleUpdate(type, paramString.split(","));
                    } catch (Exception e) {
                        this.logger.error("Error processing update", e);
                    }
                } else {
                    this.logger.info("No thing configured for integration ID " + integrationId);
                }
            } else {
                this.logger.info("Ignoring message " + line);
            }
        }
    }

    private void sendKeepAlive() {
        // Reconnect if no response is received within 30 seconds.
        this.keepAliveReconnect = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }, 30, TimeUnit.SECONDS);

        sendCommand(new LutronCommand(LutronOperation.QUERY, LutronCommandType.SYSTEM, -1, 1));
    }

    @Override
    public void dispose() {
        this.keepAlive.cancel(true);
        disconnect();
    }
}
