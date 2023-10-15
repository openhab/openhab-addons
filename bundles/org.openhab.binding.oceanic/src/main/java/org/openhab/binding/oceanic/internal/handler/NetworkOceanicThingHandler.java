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
package org.openhab.binding.oceanic.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.oceanic.internal.NetworkOceanicBindingConfiguration;
import org.openhab.binding.oceanic.internal.Throttler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkOceanicThingHandler} implements {@link OceanicThingHandler} for an Oceanic water softener that is
 * reached using a socat TCP proxy
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class NetworkOceanicThingHandler extends OceanicThingHandler {

    private static final int REQUEST_TIMEOUT = 3000;
    private static final int RECONNECT_INTERVAL = 15;

    private final Logger logger = LoggerFactory.getLogger(NetworkOceanicThingHandler.class);

    private @Nullable Socket socket;
    private @Nullable InputStream inputStream;
    private @Nullable OutputStream outputStream;
    protected @Nullable ScheduledFuture<?> reconnectJob;

    public NetworkOceanicThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        NetworkOceanicBindingConfiguration config = getConfigAs(NetworkOceanicBindingConfiguration.class);

        try {
            socket = new Socket(config.ipAddress, config.portNumber);
            if (socket != null) {
                socket.setSoTimeout(REQUEST_TIMEOUT);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (UnknownHostException e) {
            logger.error("An exception occurred while resolving host {}:{} : '{}'", config.ipAddress, config.portNumber,
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not resolve host " + config.ipAddress + ": " + e.getMessage());
        } catch (IOException e) {
            logger.debug("An exception occurred while connecting to host {}:{} : '{}'", config.ipAddress,
                    config.portNumber, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not connect to host " + config.ipAddress + ": " + e.getMessage());
            reconnectJob = scheduler.schedule(reconnectRunnable, RECONNECT_INTERVAL, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        NetworkOceanicBindingConfiguration config = getConfigAs(NetworkOceanicBindingConfiguration.class);

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("An exception occurred while disconnecting to host {}:{} : '{}'", config.ipAddress,
                        config.portNumber, e.getMessage());
            } finally {
                socket = null;
                outputStream = null;
                inputStream = null;
            }
        }

        super.dispose();
    }

    @Override
    protected @Nullable String requestResponse(String commandAsString) {
        synchronized (this) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                NetworkOceanicBindingConfiguration config = getConfigAs(NetworkOceanicBindingConfiguration.class);
                Throttler.lock(config.ipAddress);

                String request = commandAsString + "\r";

                byte[] dataBuffer = new byte[bufferSize];
                byte[] tmpData = new byte[bufferSize];
                String line;
                int len = -1;
                int index = 0;
                boolean sequenceFound = false;

                final byte lineFeed = (byte) '\n';
                final byte carriageReturn = (byte) '\r';
                final byte nullChar = (byte) '\0';
                final byte eChar = (byte) 'E';
                final byte rChar = (byte) 'R';

                try {
                    logger.debug("Sending request '{}'", request);

                    outputStream.write(request.getBytes());
                    outputStream.flush();

                    while (true) {
                        if ((len = inputStream.read(tmpData)) > -1) {
                            if (logger.isTraceEnabled()) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < len; i++) {
                                    sb.append(String.format("%02X ", tmpData[i]));
                                }
                                logger.trace("Read {} bytes : {}", len, sb.toString());
                            }

                            for (int i = 0; i < len; i++) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Byte {} equals '{}' (hex '{}')", i,
                                            new String(new byte[] { tmpData[i] }), String.format("%02X", tmpData[i]));
                                }

                                if (tmpData[i] == nullChar && !sequenceFound) {
                                    sequenceFound = true;
                                    logger.trace("Start of sequence found");
                                }

                                if (sequenceFound && tmpData[i] != lineFeed && tmpData[i] != carriageReturn
                                        && tmpData[i] != nullChar) {
                                    dataBuffer[index++] = tmpData[i];
                                    if (logger.isTraceEnabled()) {
                                        logger.trace("dataBuffer[{}] set to '{}'(hex '{}')", index - 1,
                                                new String(new byte[] { dataBuffer[index - 1] }),
                                                String.format("%02X", dataBuffer[index - 1]));
                                    }
                                }

                                if (sequenceFound && i >= 2) {
                                    if (tmpData[i - 2] == eChar && tmpData[i - 1] == rChar && tmpData[i] == rChar) {
                                        // Received ERR from the device.
                                        return null;
                                    }
                                }

                                if (sequenceFound && i > 0
                                        && (tmpData[i - 1] != carriageReturn && tmpData[i] == nullChar)) {
                                    index = 0;
                                    // Ignore trash received
                                    if (logger.isTraceEnabled()) {
                                        StringBuilder sb = new StringBuilder();
                                        for (int j = 0; j < i; j++) {
                                            sb.append(String.format("%02X ", tmpData[j]));
                                        }
                                        logger.trace("Ingoring {} bytes : {}", i, sb);
                                    }
                                }

                                if (sequenceFound && (tmpData[i] == carriageReturn)) {
                                    if (index > 0) {
                                        line = new String(Arrays.copyOf(dataBuffer, index));
                                        logger.debug("Received response '{}'", line);
                                        line = StringUtils.chomp(line);
                                        line = line.replace(",", ".");
                                        line = line.trim();
                                        index = 0;

                                        return line;
                                    }
                                }

                                if (index == bufferSize) {
                                    index = 0;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.debug("An exception occurred while quering host {}:{} : '{}'", config.ipAddress,
                            config.portNumber, e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    reconnectJob = scheduler.schedule(reconnectRunnable, RECONNECT_INTERVAL, TimeUnit.SECONDS);
                } finally {
                    Throttler.unlock(config.ipAddress);
                }
            }
            return null;
        }
    }

    private Runnable reconnectRunnable = () -> {
        dispose();
        initialize();
    };
}
