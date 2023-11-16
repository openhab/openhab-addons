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
package org.openhab.binding.dali.internal.handler;

import static org.openhab.binding.dali.internal.DaliBindingConstants.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dali.internal.protocol.DaliBackwardFrame;
import org.openhab.binding.dali.internal.protocol.DaliCommandBase;
import org.openhab.binding.dali.internal.protocol.DaliResponse;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DaliserverBridgeHandler} handles the lifecycle of daliserver connections.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliserverBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(BRIDGE_TYPE);

    private final Logger logger = LoggerFactory.getLogger(DaliserverBridgeHandler.class);
    private static final int DALI_DEFAULT_TIMEOUT = 5000;

    private DaliserverConfig config = new DaliserverConfig();
    private @Nullable ExecutorService commandExecutor;

    public DaliserverBridgeHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(DaliserverConfig.class);
        commandExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory(thing.getUID().getAsString(), true));
        updateStatus(ThingStatus.ONLINE);
    }

    private Socket getConnection() throws IOException {
        try {
            logger.debug("Creating connection to daliserver on: {}  port: {}", config.host, config.port);
            Socket socket = new Socket(config.host, config.port);
            socket.setSoTimeout(DALI_DEFAULT_TIMEOUT);
            return socket;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            throw e;
        }
    }

    @Override
    public void dispose() {
        if (commandExecutor != null) {
            commandExecutor.shutdownNow();
        }
    }

    public CompletableFuture<@Nullable Void> sendCommand(DaliCommandBase command) {
        return sendCommandWithResponse(command, DaliResponse.class).thenApply(c -> (Void) null);
    }

    public <T extends DaliResponse> CompletableFuture<@Nullable T> sendCommandWithResponse(DaliCommandBase command,
            Class<T> responseType) {
        CompletableFuture<@Nullable T> future = new CompletableFuture<>();
        ExecutorService commandExecutor = this.commandExecutor;
        if (commandExecutor != null) {
            commandExecutor.submit(() -> {
                byte[] prefix = new byte[] { 0x2, 0x0 };
                byte[] message = command.frame.pack();
                byte[] frame = new byte[prefix.length + message.length];
                System.arraycopy(prefix, 0, frame, 0, prefix.length);
                System.arraycopy(message, 0, frame, prefix.length, message.length);

                try (Socket socket = getConnection();
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        DataInputStream in = new DataInputStream(socket.getInputStream())) {
                    // send the command
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending: {}", HexUtils.bytesToHex(frame));
                    }
                    out.write(frame);
                    if (command.sendTwice) {
                        out.flush();
                        in.readNBytes(4); // discard
                        out.write(frame);
                    }
                    out.flush();

                    // read the response
                    try {
                        @Nullable
                        T response = parseResponse(in, responseType);
                        future.complete(response);
                        return;
                    } catch (DaliException e) {
                        future.completeExceptionally(e);
                        return;
                    }
                } catch (SocketTimeoutException e) {
                    logger.warn("Timeout sending command to daliserver: {} Message: {}", frame, e.getMessage());
                    future.completeExceptionally(new DaliException("Timeout sending command to daliserver", e));
                } catch (IOException e) {
                    logger.warn("Problem sending command to daliserver: {} Message: {}", frame, e.getMessage());
                    future.completeExceptionally(new DaliException("Problem sending command to daliserver", e));
                } catch (Exception e) {
                    logger.warn("Unexpected exception while sending command to daliserver: {} Message: {}", frame,
                            e.getMessage());
                    logger.trace("Stacktrace", e);
                    future.completeExceptionally(e);
                }
            });
        } else {
            future.complete(null);
        }
        return future;
    }

    private <T extends DaliResponse> @Nullable T parseResponse(DataInputStream reader, Class<T> responseType)
            throws IOException, DaliException {
        try {
            T result = responseType.getDeclaredConstructor().newInstance();
            byte[] response = reader.readNBytes(4);
            if (logger.isDebugEnabled()) {
                logger.debug("Received: {}", HexUtils.bytesToHex(response));
            }
            byte status = response[1], rval = response[2];
            if (status == 0) {
                // No return value to process.
            } else if (status == 1) {
                result.parse(new DaliBackwardFrame(rval));
            } else if (status == 255) {
                // This is "failure" - daliserver reports this for a garbled response when several ballasts reply. It
                // should be interpreted as "Yes".
                result.parse(null);
            } else {
                throw new DaliException("Invalid response status: " + status);
            }

            return result;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            return null;
        }
    }
}
