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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private ExecutorService executor;
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

        executor = Executors.newSingleThreadExecutor();

        // Read rego version. When read successfully, set status to ONLINE.
        readRegoVersion();
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

        updateStatus(ThingStatus.OFFLINE);
        mapper.channels().forEach(channelIID -> updateState(channelIID, UnDefType.UNDEF));
    }

    private void readFromSystemRegister(String channelIID) {
        RegoMapper.Channel channel = mapper.map(channelIID);
        if (channel == null) {
            logger.warn("Unknown channel requested {}.", channelIID);
            return;
        }

        executeCommand(CommandFactory.createReadFromSystemRegisterCmd(channel.address()),
                ResponseParser.StandardFormLength).thenApply(ResponseParser::standardForm)
                        .thenApply(ValueConverter::ToDoubleState).thenAccept(state -> updateState(channelIID, state));
    }

    private void readRegoVersion() {
        executeCommand(CommandFactory.createReadRegoVersionCommand(), ResponseParser.StandardFormLength)
                .thenApply(ResponseParser::standardForm).thenApply(ValueConverter::ToShort).thenAccept(version -> {
                    if (version != null) {
                        updateStatus(ThingStatus.ONLINE);
                        logger.info("Connected to Rego version {}.", version);
                    }
                });
    }

    private CompletableFuture<byte[]> executeCommand(byte[] command, int length) {
        return CompletableFuture.supplyAsync(() -> {

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

        }, executor);
    }

    private static String byteArrayToHex(byte[] buffer) {
        return IntStream.range(0, buffer.length).map(i -> buffer[i]).mapToObj(i -> String.format("0x%02X", i))
                .collect(Collectors.joining(" "));
    }
}
