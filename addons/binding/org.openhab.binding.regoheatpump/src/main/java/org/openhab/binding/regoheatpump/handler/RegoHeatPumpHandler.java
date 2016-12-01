/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.handler;

import static org.openhab.binding.regoheatpump.RegoHeatPumpBindingConstants.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.library.types.DateTimeType;
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
import org.openhab.binding.regoheatpump.internal.protocol.CommandFactory;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.RegoRegisterMapper;
import org.openhab.binding.regoheatpump.internal.protocol.ResponseParser;
import org.openhab.binding.regoheatpump.internal.protocol.ResponseParserFactory;
import org.openhab.binding.regoheatpump.internal.utils.QueueHashSet;
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
    private final QueueHashSet<String> pending = new QueueHashSet<String>();
    private RegoConnection connection;
    private ScheduledExecutorService executor;
    private RegoRegisterMapper mapper;

    protected RegoHeatPumpHandler(Thing thing) {
        super(thing);
    }

    protected abstract RegoConnection createConnection();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        addPendingChannel(channelUID.getId());
        executor.submit(this::processQueue);
    }

    @Override
    public void initialize() {
        mapper = RegoRegisterMapper.rego600();
        executor = Executors.newSingleThreadScheduledExecutor();

        // Check if we have a valid rego device we can connect to.
        executor.execute(() -> {
            try {
                checkRegoDevice();
            } catch (IOException e) {
                // If checking rego version failed than error and status
                // are already handled and nothing left for us here.
            }
        });

        int refreshInterval = ((Number) getConfig().get(REFRESH_INTERVAL)).intValue();
        executor.scheduleWithFixedDelay(this::refresh, refreshInterval, refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();

        executor.shutdownNow();
        executor = null;

        closeConnection();

        mapper = null;
        pending.clear();
    }

    private void processChannelRequest(String channelIID) {
        switch (channelIID) {
            case CHANNEL_LAST_ERROR:
                readLastError();
                break;

            case CHANNEL_FRONT_PANEL_POWER_LED:
                readFromFrontPanel(channelIID, (short) 0x0012);
                break;

            case CHANNEL_FRONT_PANEL_PUMP_LED:
                readFromFrontPanel(channelIID, (short) 0x0013);
                break;

            case CHANNEL_FRONT_PANEL_ADDITIONAL_HEATING_LED:
                readFromFrontPanel(channelIID, (short) 0x0014);
                break;

            case CHANNEL_FRONT_PANEL_WATER_HEATER_LED:
                readFromFrontPanel(channelIID, (short) 0x0015);
                break;

            case CHANNEL_FRONT_PANEL_ALARM_LED:
                readFromFrontPanel(channelIID, (short) 0x0016);
                break;

            default:
                if (channelIID.startsWith(CHANNEL_GROUP_REGISTERS)) {
                    readFromSystemRegister(channelIID);
                } else {
                    logger.error("Unable to handle unknown channel {}", channelIID);
                }
                break;
        }
    }

    private Collection<String> linkedChannels() {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId).filter(this::isLinked)
                .collect(Collectors.toList());
    }

    private void addPendingChannel(String channelIID) {
        // CHANNEL_LAST_ERROR_CODE and CHANNEL_LAST_ERROR_TIMESTAMP are read from same
        // register. To prevent accessing same register twice when both channels are linked,
        // use same name for both so only a single fetch will be triggered.
        if (CHANNEL_LAST_ERROR_CODE.equals(channelIID) || CHANNEL_LAST_ERROR_TIMESTAMP.equals(channelIID)) {
            channelIID = CHANNEL_LAST_ERROR;
        }
        pending.push(channelIID);
    }

    private void refresh() {
        linkedChannels().forEach(this::addPendingChannel);
        processQueue();
    }

    private void processQueue() {
        while (Thread.interrupted() == false) {
            final String channelIID = pending.peek();
            if (channelIID == null) {
                break;
            }

            processChannelRequest(channelIID);
            pending.poll();

            if (thing.getStatus() != ThingStatus.ONLINE) {
                pending.clear();
                break;
            }
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

    private void readLastError() {
        executeCommandAndUpdateState(CHANNEL_LAST_ERROR_CODE, CommandFactory.createReadLastErrorCommand(),
                ResponseParserFactory.ErrorLine, e -> {
                    if (e == null) {
                        updateState(CHANNEL_LAST_ERROR_TIMESTAMP, UnDefType.NULL);
                        return UnDefType.NULL;
                    }

                    try {
                        updateState(CHANNEL_LAST_ERROR_TIMESTAMP, new DateTimeType(e.timestamp()));
                    } catch (RuntimeException ex) {
                        logger.warn("Unable to convert timestamp '{}' to DateTimeType due {}", e.timestampAsString(),
                                ex);
                        updateState(CHANNEL_LAST_ERROR_TIMESTAMP, UnDefType.UNDEF);
                    }

                    return new StringType(Byte.toString(e.error()));
                });
    }

    private void readFromFrontPanel(String channelIID, short address) {
        final byte[] command = CommandFactory.createReadFromFrontPanelCommand(address);
        executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.Short, v -> {
            return v == 0 ? OnOffType.OFF : OnOffType.ON;
        });
    }

    private void readFromSystemRegister(String channelIID) {
        RegoRegisterMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            logger.warn("Unknown channel requested '{}'.", channelIID);
        } else {
            final byte[] command = CommandFactory.createReadFromSystemRegisterCommand(channel.address());
            executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.Short, channel::convert);
        }
    }

    private <T> void executeCommandAndUpdateState(String channelIID, byte[] command, ResponseParser<T> parser,
            Function<T, State> converter) {

        try {
            if (thing.getStatus() != ThingStatus.ONLINE) {
                checkRegoDevice();
            }

            logger.debug("Reading value for channel '{}' ...", channelIID);
            T result = executeCommand(command, parser);

            logger.debug("Got value for '{}' = {}", channelIID, result);
            updateState(channelIID, converter.apply(result));

        } catch (Exception e) {
            logger.debug("Accessing value for channel '{}' failed due {}", channelIID, e);
            updateState(channelIID, UnDefType.UNDEF);
        }
    }

    private void checkRegoDevice() throws IOException {
        logger.debug("Reading Rego device version...");
        short regoVersion = executeCommand(CommandFactory.createReadRegoVersionCommand(), ResponseParserFactory.Short);

        updateStatus(ThingStatus.ONLINE);
        logger.info("Connected to Rego version {}.", regoVersion);
    }

    private <T> T executeCommand(byte[] command, ResponseParser<T> parser) throws IOException {
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
            throw e;
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
