/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ivtheatpump.handler;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.ivtheatpump.IVTHeatPumpBindingConstants;
import org.openhab.binding.ivtheatpump.internal.protocol.CommandFactory;
import org.openhab.binding.ivtheatpump.internal.protocol.IVRConnection;
import org.openhab.binding.ivtheatpump.internal.protocol.RegoRegisterMapper;
import org.openhab.binding.ivtheatpump.internal.protocol.ResponseParser;
import org.openhab.binding.ivtheatpump.internal.protocol.ValueConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IVTHeatPumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public abstract class IVTHeatPumpHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IVTHeatPumpHandler.class);
    private final Collection<String> linkedChannels = Collections.synchronizedCollection(new LinkedList<String>());
    private IVRConnection connection;
    private ScheduledExecutorService executor;
    private RegoRegisterMapper mapper;

    protected IVTHeatPumpHandler(Thing thing) {
        super(thing);
    }

    protected abstract IVRConnection createConnection();

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        processChannelRequest(channelUID.getId());
    }

    @Override
    public void initialize() {
        super.initialize();

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
        linkedChannels.clear();
        mapper = null;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        linkedChannels.add(channelUID.getId());
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        linkedChannels.remove(channelUID.getId());
    }

    private CompletableFuture<Void> processChannelRequest(String channelIID) {
        if (channelIID.startsWith(IVTHeatPumpBindingConstants.CHANNEL_GROUP_SENSORS)) {
            return readFromSystemRegister(channelIID);
        }

        if (channelIID.startsWith("status")) {
            return readLastError();
        }

        logger.error("Unable to handle unknown channel {}", channelIID);
        return CompletableFuture.completedFuture(null);
    }

    private void scheduleRefresh() {
        int refreshInterval = ((Number) getConfig().get(IVTHeatPumpBindingConstants.REFRESH_INTERVAL)).intValue();
        executor.schedule(this::refresh, refreshInterval, TimeUnit.SECONDS);
    }

    private void refresh() {
        logger.debug("Refreshing channels {}", linkedChannels);
        refresh(new LinkedHashSet<>(linkedChannels).iterator());
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

    private void onDisconnected() {
        logger.info("Disconnected.");

        closeConnection();

        if (thing.getStatus() != ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE);
            mapper.channels().forEach(channelIID -> updateState(channelIID, UnDefType.UNDEF));
        }
    }

    private CompletableFuture<Void> readLastError() {
        return executeCommandAsync(CommandFactory.createReadFromDisplayCommand((short) 0),
                ResponseParser.LongFormLength).thenApply(ResponseParser::longForm).thenAccept(value -> {
                    logger.debug("Got last error '{}'", value);
                    updateState("status#lastError", new StringType(value));
                });
    }

    private CompletableFuture<Void> readFromSystemRegister(String channelIID) {
        RegoRegisterMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            logger.warn("Unknown channel requested '{}'.", channelIID);
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Reading from system register '{}' ...", channelIID);

        return executeCommandAsync(CommandFactory.createReadFromSystemRegisterCommand(channel.address()),
                ResponseParser.StandardFormLength).thenApply(ResponseParser::standardForm).thenAccept(value -> {
                    logger.debug("Got system register '{}' = {}", channelIID, value);
                    updateState(channelIID, new DecimalType(ValueConverter.toDouble(value)));
                }).exceptionally(th -> {
                    updateState(channelIID, UnDefType.UNDEF);
                    return null;
                });
    }

    private void checkRegoDevice() {
        short regoVersion = ResponseParser.standardForm(
                executeCommand(CommandFactory.createReadRegoVersionCommand(), ResponseParser.StandardFormLength));

        updateStatus(ThingStatus.ONLINE);
        logger.info("Connected to Rego version {}.", regoVersion);
    }

    private CompletableFuture<byte[]> executeCommandAsync(byte[] command, int length) {
        return CompletableFuture.supplyAsync(() -> {

            if (thing.getStatus() != ThingStatus.ONLINE) {
                checkRegoDevice();
            }

            return executeCommand(command, length);

        }, executor);
    }

    private byte[] executeCommand(byte[] command, int length) {
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

            byte[] response = new byte[length];
            for (int i = 0; i < length;) {
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

            return response;

        } catch (IOException e) {
            logger.warn("Command failed.", e);
            onDisconnected();
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
