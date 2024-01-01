/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface defining the API for querying a gateway device.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public abstract class GatewayQueryService implements AutoCloseable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Lock REQUEST_LOCK = new ReentrantLock();

    private @Nullable Socket socket;

    @Nullable
    private final ThingStatusListener thingStatusListener;

    private final FineOffsetGatewayConfiguration config;

    @Nullable
    public abstract String getFirmwareVersion();

    public abstract Map<SensorGatewayBinding, SensorDevice> getRegisteredSensors();

    @Nullable
    public abstract SystemInfo fetchSystemInfo();

    public abstract Collection<MeasuredValue> getMeasuredValues();

    public GatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener) {
        this.config = config;
        this.thingStatusListener = thingStatusListener;
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
                logger.trace("executeCommand({}): data exceeded buffer length ({})", command, buffer.length);
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
            @Nullable
            ThingStatusListener statusListener = thingStatusListener;
            if (statusListener != null) {
                statusListener.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        ex.getMessage());
            }
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
            @Nullable
            ThingStatusListener statusListener = thingStatusListener;
            try {
                socket = new Socket(config.ip, config.port);
                socket.setSoTimeout(5000);
                this.socket = socket;
                if (statusListener != null) {
                    statusListener.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                }
            } catch (IOException e) {
                if (statusListener != null) {
                    statusListener.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getMessage());
                }
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
