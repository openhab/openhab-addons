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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RegoHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public abstract class RegoHeatPumpHandler extends BaseThingHandler {

    private static final class ChannelDescriptor {
        private Date lastUpdate;

        public boolean isDirty(int refreshTime) {
            return lastUpdate == null || (lastUpdate.getTime() + refreshTime * 900 < new Date().getTime());
        }

        public void clearDirtyFlag() {
            lastUpdate = new Date();
        }

        public void markDirty() {
            lastUpdate = null;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(RegoHeatPumpHandler.class);
    private final Queue<String> pendingUpdates = new ArrayDeque<>();
    private final Map<String, ChannelDescriptor> channelDescriptors = new HashMap<>();
    private int refreshInterval;
    private Short regoVersion;
    private RegoConnection connection;
    private RegoRegisterMapper mapper;
    private ScheduledFuture<?> scheduledRefreshFuture;
    private Future<?> active;

    protected RegoHeatPumpHandler(Thing thing) {
        super(thing);
    }

    protected abstract RegoConnection createConnection();

    @Override
    public void initialize() {
        mapper = RegoRegisterMapper.rego600();
        connection = createConnection();
        refreshInterval = ((Number) getConfig().get(REFRESH_INTERVAL)).intValue();

        updateStatus(ThingStatus.UNKNOWN);

        scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, 1, refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();

        connection.close();

        scheduledRefreshFuture.cancel(true);
        scheduledRefreshFuture = null;

        synchronized (pendingUpdates) {
            pendingUpdates.clear();
            if (active != null) {
                active.cancel(true);
                active = null;
            }
        }

        synchronized (channelDescriptors) {
            channelDescriptors.clear();
        }

        connection = null;
        mapper = null;
        regoVersion = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        refreshChannelAsync(channelUID.getId());
    }

    private void refreshChannelAsync(String channelIID) {
        // CHANNEL_LAST_ERROR_CODE and CHANNEL_LAST_ERROR_TIMESTAMP are read from same
        // register. To prevent accessing same register twice when both channels are linked,
        // use same name for both so only a single fetch will be triggered.
        if (CHANNEL_LAST_ERROR_CODE.equals(channelIID) || CHANNEL_LAST_ERROR_TIMESTAMP.equals(channelIID)) {
            channelIID = CHANNEL_LAST_ERROR;
        }

        synchronized (pendingUpdates) {
            pendingUpdates.add(channelIID);
            if (active == null) {
                processNextChannel();
            }
        }
    }

    private void processNextChannel() {
        synchronized (pendingUpdates) {
            final String channelIID = pendingUpdates.poll();
            if (channelIID != null && Thread.interrupted() == false) {
                active = scheduler.submit(() -> {
                    try {
                        if (checkRegoDevice()) {
                            processChannelRequest(channelIID);
                        } else {
                            synchronized (pendingUpdates) {
                                pendingUpdates.clear();
                                active = null;
                            }
                        }

                    } finally {
                        processNextChannel();
                    }
                });
            } else {
                active = null;
            }
        }
    }

    private ChannelDescriptor channelDescriptorForChannel(final String channelIID) {
        synchronized (channelDescriptors) {
            ChannelDescriptor descriptor = channelDescriptors.get(channelIID);
            if (descriptor == null) {
                descriptor = new ChannelDescriptor();
                channelDescriptors.put(channelIID, descriptor);
            }
            return descriptor;
        }
    }

    private void processChannelRequest(final String channelIID) {
        final ChannelDescriptor descriptor = channelDescriptorForChannel(channelIID);
        if (descriptor.isDirty(refreshInterval) == false) {
            logger.debug("Not refreshing {} since it is up to date", channelIID);
            return;
        }

        try {
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

            descriptor.clearDirtyFlag();

        } catch (Exception e) {
            descriptor.markDirty();
        }
    }

    private Collection<String> linkedChannels() {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId).filter(this::isLinked)
                .collect(Collectors.toList());
    }

    private void refresh() {
        synchronized (pendingUpdates) {
            linkedChannels().forEach(this::refreshChannelAsync);
        }
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

    private void readFromFrontPanel(final String channelIID, short address) {
        final byte[] command = CommandFactory.createReadFromFrontPanelCommand(address);
        executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.Short, v -> {
            return v == 0 ? OnOffType.OFF : OnOffType.ON;
        });
    }

    private void readFromSystemRegister(final String channelIID) {
        RegoRegisterMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            logger.warn("Unknown channel requested '{}'.", channelIID);
        } else {
            final byte[] command = CommandFactory.createReadFromSystemRegisterCommand(channel.address());
            executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.Short, channel::convert);
        }
    }

    private <T> void executeCommandAndUpdateState(final String channelIID, final byte[] command,
            final ResponseParser<T> parser, Function<T, State> converter) {

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Reading value for channel '{}' ...", channelIID);
            }

            T result = executeCommand(command, parser);

            if (logger.isDebugEnabled()) {
                logger.debug("Got value for '{}' = {}", channelIID, result);
            }

            updateState(channelIID, converter.apply(result));

        } catch (Exception e) {
            logger.warn("Accessing value for channel '{}' failed due {}", channelIID, e);
            updateState(channelIID, UnDefType.UNDEF);
            throw new IllegalStateException(e);
        }
    }

    private boolean checkRegoDevice() {
        if (regoVersion == null) {
            try {
                logger.debug("Reading Rego device version...");
                regoVersion = executeCommand(CommandFactory.createReadRegoVersionCommand(),
                        ResponseParserFactory.Short);

                updateStatus(ThingStatus.ONLINE);
                logger.info("Connected to Rego version {}.", regoVersion);
            } catch (Exception e) {
                logger.warn("Reading rego version failed", e);
                return false;
            }
        }

        return true;
    }

    private <T> T executeCommand(final byte[] command, final ResponseParser<T> parser) throws IOException {
        try {
            if (connection.isConnected() == false) {
                connection.connect();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Sending {}", DatatypeConverter.printHexBinary(command));
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
                logger.debug("Received {}", DatatypeConverter.printHexBinary(response));
            }

            return parser.parse(response);

        } catch (IOException e) {
            logger.warn("Command failed.", e);

            connection.close();

            synchronized (channelDescriptors) {
                channelDescriptors.clear();
            }

            regoVersion = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            linkedChannels().forEach(channelIID -> updateState(channelIID, UnDefType.UNDEF));

            throw e;
        }
    }
}
