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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.regoheatpump.internal.protocol.CommandFactory;
import org.openhab.binding.regoheatpump.internal.protocol.ErrorLine;
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
        private byte[] cachedValue;

        public byte[] cachedValueIfNotExpired(int refreshTime) {
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

    private final Logger logger = LoggerFactory.getLogger(RegoHeatPumpHandler.class);
    private final Map<String, ChannelDescriptor> channelDescriptors = new HashMap<>();
    private int refreshInterval;
    private RegoConnection connection;
    private RegoRegisterMapper mapper;
    private ScheduledFuture<?> scheduledRefreshFuture;

    protected RegoHeatPumpHandler(Thing thing) {
        super(thing);
    }

    protected abstract RegoConnection createConnection();

    @Override
    public void initialize() {
        mapper = RegoRegisterMapper.rego600();
        connection = createConnection();
        refreshInterval = ((Number) getConfig().get(REFRESH_INTERVAL)).intValue();

        scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, 1, refreshInterval, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        super.dispose();

        connection.close();

        scheduledRefreshFuture.cancel(true);
        scheduledRefreshFuture = null;

        synchronized (channelDescriptors) {
            channelDescriptors.clear();
        }

        connection = null;
        mapper = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduledRefreshFuture.cancel(false);
            scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, 1, refreshInterval,
                    TimeUnit.SECONDS);
        } else {
            logger.debug("Unsupported command {}! Supported commands: REFRESH", command);
        }
    }

    private void processChannelRequest(final String channelIID) {
        switch (channelIID) {
            case CHANNEL_LAST_ERROR_CODE:
                readLastErrorCode();
                break;

            case CHANNEL_LAST_ERROR_TIMESTAMP:
                readLastErrorTimestamp();
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
                if (readFromSystemRegister(channelIID) == false) {
                    logger.debug("Unable to handle unknown channel {}", channelIID);
                }
                break;
        }
    }

    private Collection<String> linkedChannels() {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId).filter(this::isLinked)
                .collect(Collectors.toList());
    }

    private void refresh() {
        for (final String channelIID : linkedChannels()) {
            if (Thread.interrupted()) {
                break;
            }

            processChannelRequest(channelIID);

            if (thing.getStatus() != ThingStatus.ONLINE) {
                break;
            }
        }
    }

    private void readLastErrorCode() {
        readLastError(CHANNEL_LAST_ERROR_CODE, e -> new StringType(Byte.toString(e.error())));
    }

    private void readLastErrorTimestamp() {
        readLastError(CHANNEL_LAST_ERROR_TIMESTAMP, e -> new DateTimeType(e.timestamp()));
    }

    private void readLastError(final String channelIID, final Function<ErrorLine, State> converter) {
        executeCommandAndUpdateState(channelIID, CommandFactory.createReadLastErrorCommand(),
                ResponseParserFactory.ErrorLine, e -> {
                    return e == null ? UnDefType.NULL : converter.apply(e);
                });
    }

    private void readFromFrontPanel(final String channelIID, short address) {
        final byte[] command = CommandFactory.createReadFromFrontPanelCommand(address);
        executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.Short, v -> {
            return v == 0 ? OnOffType.OFF : OnOffType.ON;
        });
    }

    private boolean readFromSystemRegister(final String channelIID) {
        RegoRegisterMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            return false;
        }

        final byte[] command = CommandFactory.createReadFromSystemRegisterCommand(channel.address());
        executeCommandAndUpdateState(channelIID, command, ResponseParserFactory.Short, channel::convert);

        return true;
    }

    private synchronized <T> void executeCommandAndUpdateState(final String channelIID, final byte[] command,
            final ResponseParser<T> parser, Function<T, State> converter) {

        if (logger.isDebugEnabled()) {
            logger.debug("Reading value for channel '{}' ...", channelIID);
        }

        try {

            final T result = executeCommandWithRetry(channelIID, command, parser, 5);

            if (logger.isDebugEnabled()) {
                logger.debug("Got value for '{}' = {}", channelIID, result);
            }

            updateState(channelIID, converter.apply(result));

        } catch (IOException e) {

            logger.warn("Accessing value for channel '" + channelIID + "' failed.", e);

            synchronized (channelDescriptors) {
                channelDescriptors.clear();
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            linkedChannels().forEach(channel -> updateState(channel, UnDefType.UNDEF));

        } catch (InterruptedException e) {

            logger.debug("Execution interrupted when accessing value for channel '" + channelIID + "'", e);
            Thread.currentThread().interrupt();

        } catch (Exception e) {

            logger.warn("Accessing value for channel '" + channelIID + "' failed.", e);
            updateState(channelIID, UnDefType.UNDEF);
        }
    }

    private <T> T executeCommandWithRetry(final String channelIID, final byte[] command, final ResponseParser<T> parser,
            int retry) throws InterruptedException, IOException {

        try {
            checkRegoDevice();
            return executeCommand(channelIID, command, parser);

        } catch (IOException e) {

            logger.warn("Accessing value for channel '" + channelIID + "' failed, retry " + Integer.toString(retry), e);

            if (connection != null) {
                connection.close();
            }

            if (retry > 0) {
                Thread.sleep(200);
                return executeCommandWithRetry(channelIID, command, parser, retry - 1);
            }

            throw e;
        }
    }

    private void checkRegoDevice() throws IOException, InterruptedException {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Reading Rego device version...");
            final Short regoVersion = executeCommand(null, CommandFactory.createReadRegoVersionCommand(),
                    ResponseParserFactory.Short);

            if (regoVersion != 600) {
                throw new IOException("Invalid rego version received " + Short.toString(regoVersion));
            }

            updateStatus(ThingStatus.ONLINE);
            logger.info("Connected to Rego version {}.", regoVersion);
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

    private <T> T executeCommand(final String channelIID, final byte[] command, final ResponseParser<T> parser)
            throws IOException, InterruptedException {

        // CHANNEL_LAST_ERROR_CODE and CHANNEL_LAST_ERROR_TIMESTAMP are read from same
        // register. To prevent accessing same register twice when both channels are linked,
        // use same name for both so only a single fetch will be triggered.
        final String mappedChannelIID = (CHANNEL_LAST_ERROR_CODE.equals(channelIID)
                || CHANNEL_LAST_ERROR_TIMESTAMP.equals(channelIID)) ? CHANNEL_LAST_ERROR : channelIID;

        // Use transient channel descriptor for null (not cached) channels.
        final ChannelDescriptor descriptor = channelIID == null ? new ChannelDescriptor()
                : channelDescriptorForChannel(mappedChannelIID);

        final byte[] cachedValue = descriptor.cachedValueIfNotExpired(refreshInterval);
        if (cachedValue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cache did not yet expire, using cached value for {}", mappedChannelIID);
            }

            return parser.parse(cachedValue);
        }

        // Send command to device and wait for response.
        if (connection.isConnected() == false) {
            connection.connect();
        }

        // Give heat pump some time between commands. Feeding commands too quickly
        // might cause heat pump not to respond.
        Thread.sleep(80);

        // Protocol is request driven so there should be no data available before sending
        // a command to the heat pump.
        final InputStream inputStream = connection.getInputStream();
        final int available = inputStream.available();
        if (available > 0) {
            // Limit to max 64 bytes, fuse.
            final byte[] buffer = new byte[Math.min(64, available)];
            inputStream.read(buffer);
            logger.warn("There are {} unexpected bytes available. Skipping {}.", available, buffer);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending {}", DatatypeConverter.printHexBinary(command));
        }

        // Send command
        final OutputStream outputStream = connection.getOutputStream();
        outputStream.write(command);
        outputStream.flush();

        // Read response, wait for max 2 second for data to arrive.
        final byte[] response = new byte[parser.responseLength()];
        final long timeout = System.currentTimeMillis() + 2000;
        int pos = 0;

        do {
            final int len = inputStream.read(response, pos, response.length - pos);
            if (len > 0) {
                pos += len;
            } else {
                // TODO: remove line below, for debug only.
                logger.warn("Got EOS, giving more time for data to arrive");
                Thread.sleep(50);
            }

        } while (pos < response.length && timeout > System.currentTimeMillis());

        if (pos < response.length) {
            // if (logger.isDebugEnabled()) {
            logger.warn("Response not received, read {} bytes => {}", pos, response);
            // }
            throw new EOFException("Response not received");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Received {}", DatatypeConverter.printHexBinary(response));
        }

        final T result = parser.parse(response);

        // If reading/parsing was done successfully, cache response payload.
        descriptor.setValue(response);

        return result;
    }
}
