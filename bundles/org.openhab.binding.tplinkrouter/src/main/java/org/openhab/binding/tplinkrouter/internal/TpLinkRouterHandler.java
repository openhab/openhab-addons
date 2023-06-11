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

    private static final long RECONNECT_DELAY = TimeUnit.MINUTES.toMillis(1);

    private static final String REFRESH_CMD = "wlctl show";
    private static final String WIFI_ON_CMD = "wlctl set --switch on";
    private static final String WIFI_OFF_CMD = "wlctl set --switch off";
    private static final String QSS_ON_CMD = "wlctl set --qss on";
    private static final String QSS_OFF_CMD = "wlctl set --qss off";

    private final Logger logger = LoggerFactory.getLogger(TpLinkRouterHandler.class);

    private final TpLinkRouterTelnetConnector connector = new TpLinkRouterTelnetConnector();
    private final BlockingQueue<ChannelUIDCommand> commandQueue = new ArrayBlockingQueue<>(1);

    private TpLinkRouterConfiguration config = new TpLinkRouterConfiguration();
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
                logger.warn("Got exception", e);
                Thread.currentThread().interrupt();
            }
            if (command instanceof RefreshType) {
                connector.sendCommand(REFRESH_CMD);
            } else if (command == OnOffType.ON) {
                connector.sendCommand(WIFI_ON_CMD);
            } else if (command == OnOffType.OFF) {
                connector.sendCommand(WIFI_OFF_CMD);
            }
        } else if (WIFI_QSS.equals(channelUID.getId())) {
            try {
                commandQueue.put(new ChannelUIDCommand(channelUID, command));
            } catch (InterruptedException e) {
                logger.warn("Got exception", e);
                Thread.currentThread().interrupt();
            }
            if (command instanceof RefreshType) {
                connector.sendCommand(REFRESH_CMD);
            } else if (command == OnOffType.ON) {
                connector.sendCommand(QSS_ON_CMD);
            } else if (command == OnOffType.OFF) {
                connector.sendCommand(QSS_OFF_CMD);
            }
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
            scheduledFuture = null;
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
                    if ("Disabled".equals(value)) {
                        updateState(WIFI_QSS, OnOffType.OFF);
                    } else if ("Enable".equals(value)) {
                        updateState(WIFI_QSS, OnOffType.ON);
                    } else {
                        logger.warn("Unsupported value {} for label {}", value, label);
                    }
                    break;
                case "SecMode":
                    String[] parts = value.split("\\s|-");
                    updateState(WIFI_SECMODE, StringType.valueOf(parts[0]));
                    updateState(WIFI_AUTHENTICATION, StringType.valueOf(parts[1]));
                    if (parts.length >= 3) {
                        updateState(WIFI_ENCRYPTION, StringType.valueOf(parts[2]));
                    } else {
                        updateState(WIFI_ENCRYPTION, StringType.EMPTY);
                    }
                    break;
                case "Key":
                    updateState(WIFI_KEY, StringType.valueOf(value));
                    break;
                default:
                    logger.debug("Unrecognized label {}", label);
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
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("try to reconnect in {} ms", RECONNECT_DELAY);
        scheduler.schedule(this::createConnection, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onReaderThreadInterrupted() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void onReaderThreadStarted() {
        scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            handleCommand(new ChannelUID(getThing().getUID(), WIFI_STATUS), RefreshType.REFRESH);
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
