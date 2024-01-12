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
package org.openhab.binding.serial.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
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

    private TcpBridgeConfiguration config = new TcpBridgeConfiguration();

    private @Nullable Socket socket;

    private @Nullable ScheduledFuture<?> readScheduler;

    public TcpBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TcpBridgeConfiguration.class);
        if (!initialize(config)) {
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
            socket.connect(new InetSocketAddress(config.address, config.port), config.timeout * 1000);
            if (config.timeout > 0) {
                socket.setSoTimeout(config.timeout * 1000);
            }
            if (config.keepAlive) {
                socket.setKeepAlive(true);
            }

            this.socket = socket;
            inputStream = socket.getInputStream();
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
        // this may interfere with tryToReconnect. In this case
        // some kind of locking is required.
        int interval = 250;
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            this.readScheduler.cancel(true);
            this.readScheduler = scheduler.schedule(() -> {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    try {
                        InputStream inputStream = this.inputStream;
                        if (inputStream != null) {
                            synchronized (inputStream) {
                                if (inputStream.available() > 0) {
                                    receiveAndProcessNow();
                                }
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
    public void dispose() {
        ScheduledFuture<?> readScheduler = this.readScheduler;
        this.readScheduler = null;
        if (readScheduler != null) {
            try {
                readScheduler.cancel(true);
            } catch (Exception ignore) {
            }
        }

        Socket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
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
