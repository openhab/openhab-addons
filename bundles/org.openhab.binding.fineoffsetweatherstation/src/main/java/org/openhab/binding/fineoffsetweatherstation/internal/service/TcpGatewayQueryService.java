/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Base class for the gateway query services that communicate via the proprietary TCP binary protocol.
 * <p>
 * It owns the socket lifecycle and the command/response exchange; the concrete subclasses
 * ({@link FineOffsetGatewayQueryService}, {@link ELVGatewayQueryService}) only differ in the
 * commands they send and how they parse the responses.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public abstract class TcpGatewayQueryService extends GatewayQueryService {

    private static final Lock REQUEST_LOCK = new ReentrantLock();

    private @Nullable Socket socket;

    public TcpGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener) {
        super(config, thingStatusListener);
    }

    protected byte @Nullable [] executeCommand(String command, byte[] request,
            Function<byte[], Boolean> validateResponse) {
        try {
            if (!REQUEST_LOCK.tryLock(30, TimeUnit.SECONDS)) {
                logger.debug("executeCommand({}): timed out while getting the lock", command);
                return null;
            }
        } catch (InterruptedException e) {
            logger.debug("executeCommand({}): was interrupted while getting the lock", command);
            return null;
        }

        byte[] buffer = new byte[2028];
        int bytesRead;

        try {
            Socket socket = getConnection();
            if (socket == null) {
                return null;
            }
            logger.trace("executeCommand({}): send request: {}", command,
                    Utils.toHexString(request, request.length, ""));
            InputStream in = socket.getInputStream();
            socket.getOutputStream().write(request);
            if ((bytesRead = in.read(buffer)) == -1) {
                logger.debug("executeCommand({}): connection closed by the gateway without a response", command);
                return null;
            }
            if (!validateResponse.apply(buffer)) {
                if (bytesRead > 0) {
                    logger.debug("executeCommand({}), invalid response: {}", command,
                            Utils.toHexString(buffer, bytesRead, ""));
                } else {
                    logger.debug("executeCommand({}): no response", command);
                }
                return null;
            }

        } catch (IOException ex) {
            updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            try {
                close();
            } catch (IOException e) {
                // ignored
            }
            return null;
        } catch (Exception ex) {
            logger.warn("executeCommand({})", command, ex);
            return null;
        } finally {
            REQUEST_LOCK.unlock();
        }

        var data = Arrays.copyOfRange(buffer, 0, bytesRead);
        logger.trace("executeCommand({}): received: {}", command, Utils.toHexString(data, data.length, ""));
        return data;
    }

    protected synchronized @Nullable Socket getConnection() {
        Socket socket = this.socket;
        if (socket == null) {
            try {
                socket = new Socket(config.ip, config.port);
                socket.setSoTimeout(5000);
                this.socket = socket;
                updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            } catch (IOException e) {
                updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return socket;
    }

    @Override
    public void close() throws IOException {
        Socket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            socket.close();
        }
    }
}
