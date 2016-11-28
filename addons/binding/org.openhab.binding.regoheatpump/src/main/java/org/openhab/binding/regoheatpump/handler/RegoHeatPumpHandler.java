/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.handler;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.CommandFactory;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.RegoRegisterMapper;
import org.openhab.binding.regoheatpump.internal.protocol.ResponseParser;
import org.openhab.binding.regoheatpump.internal.protocol.ResponseParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RegoHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public abstract class RegoHeatPumpHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RegoHeatPumpHandler.class);
    private RegoConnection connection;
    private ScheduledExecutorService executor;
    private RegoRegisterMapper mapper;

    protected RegoHeatPumpHandler(Thing thing) {
        super(thing);
    }

    protected abstract RegoConnection createConnection();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        processChannelRequest(channelUID.getId());
    }

    @Override
    public void initialize() {
        mapper = RegoRegisterMapper.rego600();
        executor = Executors.newSingleThreadScheduledExecutor();

        CompletableFuture.runAsync(this::checkRegoDevice, executor);
        scheduleRefresh();
    }

    @Override
    public void dispose() {
        super.dispose();

        executor.shutdownNow();
        executor = null;

        closeConnection();
        mapper = null;
    }

    private CompletableFuture<Void> processChannelRequest(String channelIID) {
        switch (channelIID) {
            case RegoHeatPumpBindingConstants.CHANNEL_LAST_ERROR:
                return readLastError(channelIID);

            case RegoHeatPumpBindingConstants.CHANNEL_FRONT_PANEL_POWER_LED:
                return readFromFrontPanel(channelIID, (short) 0x0012);

            case RegoHeatPumpBindingConstants.CHANNEL_FRONT_PANEL_PUMP_LED:
                return readFromFrontPanel(channelIID, (short) 0x0013);

            case RegoHeatPumpBindingConstants.CHANNEL_FRONT_PANEL_ADDITIONAL_HEATING_LED:
                return readFromFrontPanel(channelIID, (short) 0x0014);

            case RegoHeatPumpBindingConstants.CHANNEL_FRONT_PANEL_WATER_HEATER_LED:
                return readFromFrontPanel(channelIID, (short) 0x0015);

            case RegoHeatPumpBindingConstants.CHANNEL_FRONT_PANEL_ALARM_LED:
                return readFromFrontPanel(channelIID, (short) 0x0016);
        }

        if (channelIID.startsWith(RegoHeatPumpBindingConstants.CHANNEL_GROUP_REGISTERS)) {
            return readFromSystemRegister(channelIID);
        }

        logger.error("Unable to handle unknown channel {}", channelIID);
        return CompletableFuture.completedFuture(null);
    }

    private void scheduleRefresh() {
        int refreshInterval = ((Number) getConfig().get(RegoHeatPumpBindingConstants.REFRESH_INTERVAL)).intValue();
        executor.schedule(this::refresh, refreshInterval, TimeUnit.SECONDS);
    }

    private Collection<String> linkedChannels() {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId).filter(this::isLinked)
                .collect(Collectors.toList());
    }

    private void refresh() {
        refresh(linkedChannels().iterator());
    }

    private void refresh(Iterator<String> channels) {
        if (channels.hasNext()) {
            processChannelRequest(channels.next()).thenRun(() -> {
                if (thing.getStatus() == ThingStatus.ONLINE) {
                    refresh(channels);
                } else {
                    scheduleRefresh();
                }
            });
        } else {
            scheduleRefresh();
        }
    }

    private void closeConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    private void onDisconnected(String message) {
        logger.info("Disconnected due {}.", message);

        closeConnection();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        linkedChannels().forEach(channelIID -> updateState(channelIID, UnDefType.UNDEF));
    }

    private CompletableFuture<Void> readLastError(String channelIID) {
        return executeCommandAndUpdateStateAsync(channelIID, CommandFactory.createReadLastErrorCommand(),
                ResponseParserFactory.ErrorLine, e -> {
                    return e == null ? UnDefType.NULL : new StringType(e.toString());
                });
    }

    private CompletableFuture<Void> readFromFrontPanel(String channelIID, short address) {
        final byte[] command = CommandFactory.createReadFromFrontPanelCommand(address);
        return executeCommandAndUpdateStateAsync(channelIID, command, ResponseParserFactory.Short, v -> {
            return v == 0 ? OnOffType.OFF : OnOffType.ON;
        });
    }

    private CompletableFuture<Void> readFromSystemRegister(String channelIID) {
        RegoRegisterMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            logger.warn("Unknown channel requested '{}'.", channelIID);
            return CompletableFuture.completedFuture(null);
        }

        final byte[] command = CommandFactory.createReadFromSystemRegisterCommand(channel.address());
        return executeCommandAndUpdateStateAsync(channelIID, command, ResponseParserFactory.Short, channel::convert);
    }

    private <T> CompletableFuture<Void> executeCommandAndUpdateStateAsync(String channelIID, byte[] command,
            ResponseParser<T> parser, Function<T, State> converter) {

        logger.debug("Reading value for channel '{}' ...", channelIID);
        return executeCommandAsync(command, parser).thenAccept(value -> {
            logger.debug("Got value for '{}' = {}", channelIID, value);
            updateState(channelIID, converter.apply(value));
        }).exceptionally(th -> {
            logger.debug("Accessing value for channel '{}' failed due {}", channelIID, th);
            updateState(channelIID, UnDefType.UNDEF);
            return null;
        });
    }

    private void checkRegoDevice() {
        logger.debug("Reading Rego device version...");
        short regoVersion = executeCommand(CommandFactory.createReadRegoVersionCommand(), ResponseParserFactory.Short);

        updateStatus(ThingStatus.ONLINE);
        logger.info("Connected to Rego version {}.", regoVersion);
    }

    private <T> CompletableFuture<T> executeCommandAsync(byte[] command, ResponseParser<T> parser) {
        return CompletableFuture.supplyAsync(() -> {

            if (thing.getStatus() != ThingStatus.ONLINE) {
                checkRegoDevice();
            }

            return executeCommand(command, parser);

        }, executor);
    }

    private <T> T executeCommand(byte[] command, ResponseParser<T> parser) {
        try {
            if (connection == null) {
                connection = createConnection();
            }

            if (connection.isConnected() == false) {
                connection.connect();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Sending {}", byteArrayToHex(command));
            }

            connection.write(command);

            byte[] response = new byte[parser.responseLength()];
            for (int i = 0; i < response.length;) {
                int value = connection.read();

                if (value == -1) {
                    throw new EOFException("Connection closed");
                }

                if (i == 0 && value != ResponseParser.ComputerAddress) {
                    continue;
                }

                response[i] = (byte) value;
                ++i;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Received {}", byteArrayToHex(response));
            }

            return parser.parse(response);

        } catch (IOException e) {
            logger.warn("Command failed.", e);
            onDisconnected(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private static String byteArrayToHex(byte[] buffer) {
        StringBuilder builder = new StringBuilder();
        for (byte b : buffer) {
            builder.append(String.format("%02X ", b));
        }
        return builder.toString();
    }
}
