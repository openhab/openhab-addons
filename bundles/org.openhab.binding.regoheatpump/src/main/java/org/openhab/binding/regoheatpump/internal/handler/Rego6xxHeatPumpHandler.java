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
package org.openhab.binding.regoheatpump.internal.handler;

import static org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.binding.regoheatpump.internal.rego6xx.CommandFactory;
import org.openhab.binding.regoheatpump.internal.rego6xx.ErrorLine;
import org.openhab.binding.regoheatpump.internal.rego6xx.Rego6xxProtocolException;
import org.openhab.binding.regoheatpump.internal.rego6xx.RegoRegisterMapper;
import org.openhab.binding.regoheatpump.internal.rego6xx.ResponseParser;
import org.openhab.binding.regoheatpump.internal.rego6xx.ResponseParserFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
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
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Rego6xxHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
abstract class Rego6xxHeatPumpHandler extends BaseThingHandler {

    private static final class ChannelDescriptor {
        private @Nullable Date lastUpdate;
        private byte @Nullable [] cachedValue;

        public byte @Nullable [] cachedValueIfNotExpired(int refreshTime) {
            Date lastUpdate = this.lastUpdate;
            if (lastUpdate == null || (lastUpdate.getTime() + refreshTime * 900 < new Date().getTime())) {
                return null;
            }

            return cachedValue;
        }

        public void setValue(byte[] value) {
            lastUpdate = new Date();
            cachedValue = value;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(Rego6xxHeatPumpHandler.class);
    private final Map<String, ChannelDescriptor> channelDescriptors = new HashMap<>();
    private final RegoRegisterMapper mapper = RegoRegisterMapper.REGO600;
    private @Nullable RegoConnection connection;
    private @Nullable ScheduledFuture<?> scheduledRefreshFuture;
    private int refreshInterval;

    protected Rego6xxHeatPumpHandler(Thing thing) {
        super(thing);
    }

    protected abstract RegoConnection createConnection() throws IOException;

    @Override
    public void initialize() {
        refreshInterval = ((Number) getConfig().get(REFRESH_INTERVAL)).intValue();

        try {
            connection = createConnection();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, 2, refreshInterval, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        super.dispose();

        RegoConnection connection = this.connection;
        this.connection = null;
        if (connection != null) {
            connection.close();
        }

        ScheduledFuture<?> scheduledRefreshFuture = this.scheduledRefreshFuture;
        this.scheduledRefreshFuture = null;
        if (scheduledRefreshFuture != null) {
            scheduledRefreshFuture.cancel(true);
        }

        synchronized (channelDescriptors) {
            channelDescriptors.clear();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            processChannelReadRequest(channelUID.getId());
        } else {
            RegoRegisterMapper.Channel channel = mapper.map(channelUID.getId());
            if (channel != null) {
                logger.debug("Executing command '{}' for channel '{}'", command, channelUID.getId());
                processChannelWriteRequest(channel, command);
            } else {
                logger.debug("Unsupported channel {}", channelUID.getId());
            }
        }
    }

    private static double commandToValue(Command command) {
        if (command instanceof QuantityType<?>) {
            return ((QuantityType<?>) command).doubleValue();
        }

        if (command instanceof DecimalType) {
            return ((DecimalType) command).doubleValue();
        }

        throw new NumberFormatException("Command '" + command + "' not supported");
    }

    private void processChannelWriteRequest(RegoRegisterMapper.Channel channel, Command command) {
        short value = (short) Math.round(commandToValue(command) / channel.scaleFactor());
        byte[] commandPayload = CommandFactory.createWriteToSystemRegisterCommand(channel.address(), value);
        executeCommand(null, commandPayload, ResponseParserFactory.WRITE, result -> {
            // Ignore result since it is a write command.
        });
    }

    private void processChannelReadRequest(String channelIID) {
        switch (channelIID) {
            case CHANNEL_LAST_ERROR_TYPE:
                readAndUpdateLastErrorType();
                break;
            case CHANNEL_LAST_ERROR_TIMESTAMP:
                readAndUpdateLastErrorTimestamp();
                break;
            case CHANNEL_FRONT_PANEL_POWER_LAMP:
                readAndUpdateFrontPanel(channelIID, (short) 0x0012);
                break;
            case CHANNEL_FRONT_PANEL_PUMP_LAMP:
                readAndUpdateFrontPanel(channelIID, (short) 0x0013);
                break;
            case CHANNEL_FRONT_PANEL_ADDITIONAL_HEAT_LAMP:
                readAndUpdateFrontPanel(channelIID, (short) 0x0014);
                break;
            case CHANNEL_FRONT_PANEL_WATER_HEATER_LAMP:
                readAndUpdateFrontPanel(channelIID, (short) 0x0015);
                break;
            case CHANNEL_FRONT_PANEL_ALARM_LAMP:
                readAndUpdateFrontPanel(channelIID, (short) 0x0016);
                break;
            default:
                readAndUpdateSystemRegister(channelIID);
                break;
        }
    }

    private Collection<String> linkedChannels() {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId).filter(this::isLinked)
                .collect(Collectors.toList());
    }

    private void refresh() {
        for (String channelIID : linkedChannels()) {
            if (Thread.interrupted()) {
                break;
            }

            processChannelReadRequest(channelIID);

            if (thing.getStatus() != ThingStatus.ONLINE) {
                break;
            }
        }
    }

    private void readAndUpdateLastErrorType() {
        readAndUpdateLastError(CHANNEL_LAST_ERROR_TYPE, e -> new StringType(Byte.toString(e.error())));
    }

    private void readAndUpdateLastErrorTimestamp() {
        readAndUpdateLastError(CHANNEL_LAST_ERROR_TIMESTAMP, e -> new DateTimeType(e.timestamp()));
    }

    private void readAndUpdateLastError(String channelIID, Function<ErrorLine, State> converter) {
        executeCommandAndUpdateState(channelIID, CommandFactory.createReadLastErrorCommand(),
                ResponseParserFactory.ERROR_LINE, e -> {
                    return e == ErrorLine.NO_ERROR ? UnDefType.NULL : converter.apply(e);
                });
    }

    private void readAndUpdateFrontPanel(String channelIID, short address) {
        byte[] command = CommandFactory.createReadFromFrontPanelCommand(address);
        executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.SHORT, DecimalType::new);
    }

    private void readAndUpdateSystemRegister(String channelIID) {
        RegoRegisterMapper.Channel channel = mapper.map(channelIID);
        if (channel != null) {
            byte[] command = CommandFactory.createReadFromSystemRegisterCommand(channel.address());
            executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.SHORT, value -> {
                Unit<?> unit = channel.unit();
                double result = Math.round(channel.convertValue(value) * channel.scaleFactor() * 10.0) / 10.0;
                return unit != null ? new QuantityType<>(result, unit) : new DecimalType(result);
            });
        } else {
            logger.debug("Unsupported channel {}", channelIID);
        }
    }

    private <T> void executeCommandAndUpdateState(String channelIID, byte[] command, ResponseParser<T> parser,
            Function<T, State> converter) {
        logger.debug("Reading value for channel '{}' ...", channelIID);
        executeCommand(channelIID, command, parser, result -> {
            logger.debug("Got value for '{}' = {}", channelIID, result);
            updateState(channelIID, converter.apply(result));
        });
    }

    private synchronized <T> void executeCommand(@Nullable String channelIID, byte[] command, ResponseParser<T> parser,
            Consumer<T> resultProcessor) {
        try {
            T result = executeCommandWithRetry(channelIID, command, parser, 5);
            resultProcessor.accept(result);
        } catch (IOException e) {
            logger.warn("Executing command for channel '{}' failed.", channelIID, e);

            synchronized (channelDescriptors) {
                channelDescriptors.clear();
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Rego6xxProtocolException | RuntimeException e) {
            logger.warn("Executing command for channel '{}' failed.", channelIID, e);
            if (channelIID != null) {
                updateState(channelIID, UnDefType.UNDEF);
            }
        } catch (InterruptedException e) {
            logger.debug("Execution interrupted when accessing value for channel '{}'.", channelIID, e);
            Thread.currentThread().interrupt();
        }
    }

    private <T> T executeCommandWithRetry(@Nullable String channelIID, byte[] command, ResponseParser<T> parser,
            int retry) throws Rego6xxProtocolException, IOException, InterruptedException {
        try {
            checkRegoDevice();
            return executeCommand(channelIID, command, parser);
        } catch (IOException | Rego6xxProtocolException e) {
            if (retry > 0) {
                logger.debug("Executing command for channel '{}' failed, retry {}.", channelIID, retry, e);
                Thread.sleep(200);
                return executeCommandWithRetry(channelIID, command, parser, retry - 1);
            }

            throw e;
        }
    }

    private void checkRegoDevice() throws Rego6xxProtocolException, IOException, InterruptedException {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Reading Rego device version...");
            Short regoVersion = executeCommand(null, CommandFactory.createReadRegoVersionCommand(),
                    ResponseParserFactory.SHORT);

            if (regoVersion != 600) {
                throw new IOException("Invalid rego version received " + regoVersion.toString());
            }

            updateStatus(ThingStatus.ONLINE);
            logger.debug("Connected to Rego version {}.", regoVersion);
        }
    }

    private ChannelDescriptor channelDescriptorForChannel(String channelIID) {
        synchronized (channelDescriptors) {
            ChannelDescriptor descriptor = channelDescriptors.get(channelIID);
            if (descriptor == null) {
                descriptor = new ChannelDescriptor();
                channelDescriptors.put(channelIID, descriptor);
            }
            return descriptor;
        }
    }

    private <T> T executeCommand(@Nullable String channelIID, byte[] command, ResponseParser<T> parser)
            throws Rego6xxProtocolException, IOException, InterruptedException {
        try {
            return executeCommandInternal(channelIID, command, parser);
        } catch (IOException e) {
            RegoConnection connection = this.connection;
            if (connection != null) {
                connection.close();
            }

            throw e;
        }
    }

    private <T> T executeCommandInternal(@Nullable String channelIID, byte[] command, ResponseParser<T> parser)
            throws Rego6xxProtocolException, IOException, InterruptedException {
        // CHANNEL_LAST_ERROR_CODE and CHANNEL_LAST_ERROR_TIMESTAMP are read from same
        // register. To prevent accessing same register twice when both channels are linked,
        // use same name for both so only a single fetch will be triggered.
        String mappedChannelIID = (CHANNEL_LAST_ERROR_TYPE.equals(channelIID)
                || CHANNEL_LAST_ERROR_TIMESTAMP.equals(channelIID)) ? CHANNEL_LAST_ERROR : channelIID;

        // Use transient channel descriptor for null (not cached) channels.
        ChannelDescriptor descriptor;
        if (mappedChannelIID == null) {
            descriptor = new ChannelDescriptor();
        } else {
            descriptor = channelDescriptorForChannel(mappedChannelIID);
        }

        byte[] cachedValue = descriptor.cachedValueIfNotExpired(refreshInterval);
        if (cachedValue != null) {
            logger.debug("Cache did not yet expire, using cached value for {}", mappedChannelIID);
            return parser.parse(cachedValue);
        }

        // Send command to device and wait for response.
        RegoConnection connection = this.connection;
        if (connection == null) {
            throw new IOException("Unable to execute command - no connection available");

        }
        if (!connection.isConnected()) {
            connection.connect();
        }

        // Give heat pump some time between commands. Feeding commands too quickly
        // might cause heat pump not to respond.
        Thread.sleep(80);

        // Protocol is request driven so there should be no data available before sending
        // a command to the heat pump.
        InputStream inputStream = connection.inputStream();
        int available = inputStream.available();
        if (available > 0) {
            // Limit to max 64 bytes, fuse.
            byte[] buffer = new byte[Math.min(64, available)];
            inputStream.read(buffer);
            logger.debug("There are {} unexpected bytes available. Skipping {}.", available, buffer);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending {}", HexUtils.bytesToHex(command));
        }

        // Send command
        OutputStream outputStream = connection.outputStream();
        outputStream.write(command);
        outputStream.flush();

        // Read response, wait for max 2 second for data to arrive.
        byte[] response = new byte[parser.responseLength()];
        long timeout = System.currentTimeMillis() + 2000;
        int pos = 0;

        do {
            int len = inputStream.read(response, pos, response.length - pos);
            if (len > 0) {
                pos += len;
            } else {
                // Give some time for response to arrive...
                Thread.sleep(50);
            }

        } while (pos < response.length && timeout > System.currentTimeMillis());

        if (pos < response.length) {
            logger.debug("Response not received, read {} bytes => {}", pos, response);

            throw new IOException("Response not received - got " + Integer.toString(pos) + " bytes of "
                    + Integer.toString(response.length));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Received {}", HexUtils.bytesToHex(response));
        }

        T result = parser.parse(response);

        // If reading/parsing was done successfully, cache response payload.
        descriptor.setValue(response);

        return result;
    }
}
