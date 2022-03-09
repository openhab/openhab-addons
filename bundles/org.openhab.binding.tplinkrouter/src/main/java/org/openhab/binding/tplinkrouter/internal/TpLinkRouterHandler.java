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
package org.openhab.binding.tplinkrouter.internal;

import static org.openhab.binding.tplinkrouter.internal.TpLinkRouterBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TpLinkRouterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Olivier Marceau - Initial contribution
 */
@NonNullByDefault
public class TpLinkRouterHandler extends BaseThingHandler implements TpLinkRouterTelenetListener {

    private static final Integer RECONNECT_DELAY = 60000; // 1 minute

    private final Logger logger = LoggerFactory.getLogger(TpLinkRouterHandler.class);

    private TpLinkRouterConfiguration config = new TpLinkRouterConfiguration();
    private final TpLinkRouterTelnetConnector connector = new TpLinkRouterTelnetConnector();
    private final BlockingQueue<ChannelUIDCommand> commandQueue = new ArrayBlockingQueue<>(1);
    private @Nullable ScheduledFuture<?> scheduledFuture;

    public TpLinkRouterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (WIFI_STATUS.equals(channelUID.getId())) {
            try {
                commandQueue.put(new ChannelUIDCommand(channelUID, command));
            } catch (InterruptedException e) {
                logger.error("Got exception", e);
                Thread.currentThread().interrupt();
            }
            if (command instanceof RefreshType) {
                connector.sendCommand("wlctl show");
            }
            if (command == OnOffType.ON) {
                connector.sendCommand("wlctl set --switch on");
            } else if (command == OnOffType.OFF) {
                connector.sendCommand("wlctl set --switch off");
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(TpLinkRouterConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::createConnection);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> scheduledFutureLocal = scheduledFuture;
        if (scheduledFutureLocal != null) {
            scheduledFutureLocal.cancel(true);
        }
        commandQueue.clear();
        connector.dispose();
        super.dispose();
    }

    private void createConnection() {
        connector.dispose();
        try {
            connector.connect(this, config, this.getThing().getUID().getAsString());
        } catch (IOException e) {
            logger.debug("Error while connecting, will retry in {} ms", RECONNECT_DELAY);
            scheduler.schedule(this::createConnection, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void receivedLine(String line) {
        logger.debug("Received line: {}", line);
        Pattern pattern = Pattern.compile("(\\w+)=(.+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String label = matcher.group(1);
            String value = matcher.group(2);
            switch (label) {
                case "Status":
                    if ("Disabled".equals(value)) {
                        updateState(WIFI_STATUS, OnOffType.OFF);
                    } else if ("Up".equals(value)) {
                        updateState(WIFI_STATUS, OnOffType.ON);
                    } else {
                        logger.warn("Unsupported value {} for label {}", value, label);
                    }
                    break;
                case "SSID":
                    updateState(WIFI_SSID, StringType.valueOf(value));
                    break;
                case "bandWidth":
                    updateState(WIFI_BANDWIDTH, StringType.valueOf(value));
                    break;
                case "QSS":
                    updateState(WIFI_QSS, StringType.valueOf(value));
                    break;
                case "SecMode":
                    updateState(WIFI_SECMODE, StringType.valueOf(value));
                    break;
                case "Key":
                    updateState(WIFI_KEY, StringType.valueOf(value));
                    break;
            }
        } else if ("cmd:SUCC".equals(line)) {
            ChannelUIDCommand channelUIDCommand = commandQueue.poll();
            if (channelUIDCommand != null && channelUIDCommand.getCommand() instanceof State) {
                updateState(channelUIDCommand.getChannelUID(), (State) channelUIDCommand.getCommand());
            }
        } else if ("Login incorrect. Try again.".equals(line)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Login or password incorrect");
        }
    }

    @Override
    public void onReaderThreadStopped() {
        updateStatus(ThingStatus.UNINITIALIZED);
        logger.debug("try to reconnect in {} ms", RECONNECT_DELAY);
        scheduler.schedule(this::createConnection, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onReaderThreadInterrupted() {
        updateStatus(ThingStatus.UNINITIALIZED);
    }

    @Override
    public void onReaderThreadStarted() {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            Channel wifiStateChannel = this.getThing().getChannel(WIFI_STATUS);
            if (wifiStateChannel != null) {
                this.handleCommand(wifiStateChannel.getUID(), RefreshType.REFRESH);
            }
        }, 0, config.refreshInterval, TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onCommunicationUnavailable() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Connection not available. Check if there is not another open connection.");
    }
}

/**
 * Stores a command with associated channel
 *
 * @author Olivier Marceau - Initial contribution
 */
@NonNullByDefault
class ChannelUIDCommand {
    private final ChannelUID channelUID;
    private final Command command;

    public ChannelUIDCommand(ChannelUID channelUID, Command command) {
        this.channelUID = channelUID;
        this.command = command;
    }

    public ChannelUID getChannelUID() {
        return channelUID;
    }

    public Command getCommand() {
        return command;
    }
}
