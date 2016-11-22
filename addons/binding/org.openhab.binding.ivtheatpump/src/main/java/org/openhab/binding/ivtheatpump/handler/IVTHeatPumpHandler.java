/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ivtheatpump.handler;

import java.io.EOFException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.ivtheatpump.internal.protocol.CommandFactory;
import org.openhab.binding.ivtheatpump.internal.protocol.IVRConnection;
import org.openhab.binding.ivtheatpump.internal.protocol.RegoMapper;
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
public class IVTHeatPumpHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IVTHeatPumpHandler.class);
    private final IVRConnection connection;
    private ScheduledExecutorService executor;
    private RegoMapper mapper;

    public IVTHeatPumpHandler(Thing thing, IVRConnection connection) {
        super(thing);
        this.connection = connection;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        readFromSystemRegister(channelUID.getId());
    }

    @Override
    public void initialize() {
        mapper = new RegoMapper();
        executor = Executors.newSingleThreadScheduledExecutor();

        CompletableFuture.supplyAsync(this::isValidRegoDevice, executor);
        scheduleRefresh();
    }

    private void scheduleRefresh() {
        executor.schedule(this::refresh, 10, TimeUnit.SECONDS);
    }

    private void refresh() {
        CompletableFuture.allOf(mapper.channels().stream().filter(this::isLinked).map(this::readFromSystemRegister)
                .toArray(l -> new CompletableFuture[l])).thenRun(this::scheduleRefresh);
    }

    @Override
    public void dispose() {
        super.dispose();

        executor.shutdownNow();
        executor = null;

        connection.close();
    }

    private void onDisconnected() {
        logger.info("Disconnected.");

        connection.close();

        updateStatus(ThingStatus.OFFLINE);
        mapper.channels().forEach(channelIID -> updateState(channelIID, UnDefType.UNDEF));
    }

    private CompletableFuture<Void> readFromSystemRegister(String channelIID) {
        RegoMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            logger.warn("Unknown channel requested '{}'.", channelIID);
            return CompletableFuture.completedFuture(null);
        }

        logger.debug("Reading from system register '{}' ...", channelIID);

        return executeCommandAsync(CommandFactory.createReadFromSystemRegisterCmd(channel.address()),
                ResponseParser.StandardFormLength).thenApply(ResponseParser::standardForm)
                        .thenApply(ValueConverter::ToDoubleState).thenAccept(state -> {
                            logger.debug("Got system register '{}' = {}", channelIID, state);
                            updateState(channelIID, state);
                        });
    }

    private Boolean isValidRegoDevice() {
        Short regoVersion = ValueConverter.ToShort(ResponseParser.standardForm(
                executeCommand(CommandFactory.createReadRegoVersionCommand(), ResponseParser.StandardFormLength)));

        if (regoVersion == null) {
            return false;
        }

        updateStatus(ThingStatus.ONLINE);
        logger.info("Connected to Rego version {}.", regoVersion);

        return true;
    }

    private CompletableFuture<byte[]> executeCommandAsync(byte[] command, int length) {
        return CompletableFuture.supplyAsync(() -> {

            if (getThing().getStatus() != ThingStatus.ONLINE && isValidRegoDevice() == false) {
                return null;
            }

            return executeCommand(command, length);

        }, executor);
    }

    private byte[] executeCommand(byte[] command, int length) {
        try {
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
                    throw new EOFException();
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

        } catch (Exception e) {
            logger.warn("Command failed.", e);
            onDisconnected();
            return null;
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
