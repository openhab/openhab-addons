/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal.handler;

import static org.openhab.binding.serial.internal.SerialBindingConstants.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link TcpBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class TcpBridgeHandler extends CommonBridgeHandler {

    /**
     * Since InputStream#read will block, we use our own instance of ScheduledThreadPoolExecutor
     * instead of OpenHab's default one to not block a background thread.
     */
    private final ScheduledThreadPoolExecutor readSchedulerExcecutor = new ScheduledThreadPoolExecutor(1);

    private TcpBridgeConfiguration config = new TcpBridgeConfiguration();
    private @Nullable Socket socket;
    private @Nullable ScheduledFuture<?> readScheduler;

    public TcpBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TcpBridgeConfiguration.class);
        if (!checkAndProcessConfiguration(config)) {
            return;
        }

        final String address = config.address;
        if (address.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Address must be set");
            return;
        }

        final int port = config.port;
        if (port <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set");
            return;
        }

        // initialize serial port
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), config.timeout * 1000);
            if (config.timeout > 0) {
                socket.setSoTimeout(config.timeout * 1000);
            }
            if (config.keepAlive) {
                socket.setKeepAlive(true);
            }

            this.socket = socket;
            inputStream = new BufferedInputStream(socket.getInputStream());
            outputStream = socket.getOutputStream();

            updateStatus(ThingStatus.ONLINE);

            // Since there is not a thing like a SerialPortEvent for Sockets
            // we have to trigger a read periodical.
            waitForData();
        } catch (final IllegalArgumentException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, ex.getMessage());
        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error");
            handleIOException(ex);
        }
    }

    /**
     * Replacement for SerialBridgeHandler's SerialPortEventListener.
     * Checks for available data every 250ms.
     */
    private void waitForData() {
        // If this is ever changed to values >= 1000,
        // this may interfere with tryToReconect. In this case
        // some kind of locking is required.
        int interval = 250;
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            ScheduledFuture<?> readScheduler = this.readScheduler;
            if (readScheduler != null) {
                readScheduler.cancel(false);
            }

            this.readScheduler = readSchedulerExcecutor.schedule(() -> {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    try {
                        InputStream inputStream = this.inputStream;
                        Socket socket = this.socket;
                        if (inputStream != null && socket != null) {
                            synchronized (inputStream) {
                                inputStream.mark(2);
                                // InputStream.available() does not recognise when a client has disconnected,
                                // so we will use BufferedInputStream and cache one byte.
                                int b = inputStream.read();
                                if (b < 0) {
                                    throw new SocketException("Connection lost");
                                }
                                inputStream.reset();
                                receiveAndProcessNow();
                            }
                        }

                        waitForData();
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                }
            }, interval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void processInput(String result) {
        if (isLinked(TRIGGER_CHANNEL)) {
            triggerChannel(TRIGGER_CHANNEL, CommonTriggerEvents.PRESSED);
        }
        if (isLinked(STRING_CHANNEL)) {
            refresh(STRING_CHANNEL, result);
        }
        if (isLinked(BINARY_CHANNEL)) {
            refresh(BINARY_CHANNEL, result);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> readScheduler = this.readScheduler;
        this.readScheduler = null;
        if (readScheduler != null) {
            readScheduler.cancel(true);
        }

        Socket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }

        super.dispose();
    }

    /**
     * TCP Connections do not reconnect automatically. In case of an IOException,
     * close the connection and try to re-connect.
     */
    @Override
    protected void handleIOException(IOException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        dispose();
        super.handleIOException(e);
        tryToReconnect();
    }

    private void tryToReconnect() {
        int reconnectInterval = config.reconnectInterval;
        if (reconnectInterval > 0) {
            logger.info("Trying to reconnnect to {}:{} in {} seconds", this.config.address, this.config.port,
                    reconnectInterval);
            scheduler.schedule(() -> {
                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    // Will re-call tryToReconnect on failure
                    initialize();
                }
            }, reconnectInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    protected String getLogPrefix() {
        return String.format("TCP socket '%s:%d'", config.address, config.port);
    }
}
