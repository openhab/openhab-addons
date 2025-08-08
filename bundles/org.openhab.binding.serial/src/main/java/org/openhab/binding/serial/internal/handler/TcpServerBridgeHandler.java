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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link TcpServerBridgeHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * Like {@link TcpBridgeHandler}, but listens for an incoming connection on a defined
 * TCP port. Only one active connection per time is allowed.
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class TcpServerBridgeHandler extends CommonBridgeHandler {

    private TcpServerBridgeConfiguration config = new TcpServerBridgeConfiguration();

    /**
     * Since ServerSocket#accept will block, we use our own instance of ScheduledThreadPoolExecutor
     * instead of OpenHab's default one to not block a background thread.
     */
    private final ScheduledThreadPoolExecutor connectionSchedulerExcecutor = new ScheduledThreadPoolExecutor(1);

    /**
     * Since InputStream#read will block, we use our own instance of ScheduledThreadPoolExecutor
     * instead of OpenHab's default one to not block a background thread.
     */
    private final ScheduledThreadPoolExecutor readSchedulerExcecutor = new ScheduledThreadPoolExecutor(1);

    private @Nullable ServerSocket server;
    private @Nullable Socket socket;
    private @Nullable ScheduledFuture<?> readScheduler;
    private @Nullable ScheduledFuture<?> connectionScheduler;

    public TcpServerBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        config = getConfigAs(TcpServerBridgeConfiguration.class);
        if (!checkAndProcessConfiguration(config)) {
            return;
        }

        final int port = config.port;
        if (port <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set");
            return;
        }

        final String bindAddress = config.bindAddress;
        if (bindAddress.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "BindAddress must be set");
            return;
        }

        // initialize serial port
        try {
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(bindAddress, port), 1);
            this.server = server;
            logger.info("Listening on TCP address {} port {}", bindAddress, port);

            updateStatus(ThingStatus.ONLINE);

            // Wait for an incoming connection
            waitForConnection();

        } catch (final IllegalArgumentException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, ex.getMessage());
        } catch (final IOException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "I/O error");
            handleIOException(ex);
        }
    }

    private void waitForConnection() {
        ServerSocket server = this.server;
        TcpServerBridgeConfiguration config = this.config;
        if (getThing().getStatus() == ThingStatus.ONLINE && server != null) {
            ScheduledFuture<?> acceptScheduler = this.connectionScheduler;
            if (acceptScheduler != null) {
                acceptScheduler.cancel(true);
            }

            this.connectionScheduler = connectionSchedulerExcecutor.schedule(() -> {
                if (getThing().getStatus() == ThingStatus.ONLINE && server.equals(this.server)) {
                    try {
                        synchronized (server) {
                            Socket socket = server.accept();
                            if (!server.equals(this.server)) {
                                // Drop this connection, it is not valid anymore
                                logger.warn("Rejecting incoming connection from {}:{} (invalid)",
                                        socket.getInetAddress(), socket.getPort());
                                socket.close();
                                return;
                            }

                            if (this.socket != null) {
                                // Normally, this should not happen because the ServerSocket has been bound with
                                // 'backlog=1' parameter. But it seems that this is only a recommendation to the
                                // operating system and might be ignored, so we have to handle this.
                                logger.warn("Rejecting incoming connection from {}:{} (socket already bound)",
                                        socket.getInetAddress(), socket.getPort());
                                try {
                                    socket.shutdownInput();
                                    socket.shutdownOutput();
                                    socket.close();
                                } catch (IOException ignore) {
                                }
                                waitForConnection();
                                return;
                            }

                            logger.info("Accepting incoming connection from {}:{}", socket.getInetAddress(),
                                    socket.getPort());
                            socket.setKeepAlive(this.config.keepAlive);
                            if (config.timeout > 0) {
                                socket.setSoTimeout(config.timeout * 1000);
                            }
                            this.socket = socket;
                            this.inputStream = new BufferedInputStream(socket.getInputStream());
                            this.outputStream = socket.getOutputStream();
                        }
                        waitForData();
                        waitForConnection();
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                }
            }, 100, TimeUnit.MILLISECONDS);
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
            ScheduledFuture<?> readScheduler = this.readScheduler;
            if (readScheduler != null) {
                readScheduler.cancel(false);
            }

            this.readScheduler = readSchedulerExcecutor.schedule(() -> {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    try {
                        InputStream inputStream = this.inputStream;
                        if (inputStream != null) {
                            synchronized (inputStream) {
                                inputStream.mark(1);
                                // InputStream.available() does not recognise when a client has disconnected,
                                // so we will use BufferedInputStream and cache one byte.
                                if (inputStream.read() < 0) {
                                    logger.info("{} connection lost", getLogPrefix());
                                    disposeSocket();
                                    return;
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

    private void disposeSocket() {
        // Dispose inputStream and outputStream and any active readers
        disposeReader();

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
    }

    @Override
    public void dispose() {
        super.dispose();
        disposeSocket();

        ScheduledFuture<?> connectionScheduler = this.connectionScheduler;
        this.connectionScheduler = null;
        if (connectionScheduler != null) {
            connectionScheduler.cancel(true);
        }

        ServerSocket server = this.server;
        this.server = null;
        if (server != null) {
            try {
                server.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * TCP Connections do not reconnect automatically. In case of an IOException,
     * close the connection and try to re-connect.
     */
    @Override
    protected void handleIOException(IOException e) {
        Socket socket = this.socket;
        if (socket != null) {
            logger.warn("Connection to {}:{} failed with IOException: {}", socket.getInetAddress(), socket.getPort(),
                    e.getMessage());
        }

        // Unlike TcpBridgeHandler, this only disposes the current connection,
        // not the listening port itself.
        disposeSocket();
        super.handleIOException(e);
    }

    @Override
    protected String getLogPrefix() {
        return String.format("TCP Server '%s:%d'", config.bindAddress, config.port);
    }
}
