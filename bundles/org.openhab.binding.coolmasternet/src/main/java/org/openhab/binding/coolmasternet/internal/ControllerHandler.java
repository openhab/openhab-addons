/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.coolmasternet.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.coolmasternet.internal.config.ControllerConfiguration;
import org.openhab.binding.coolmasternet.internal.handler.HVACHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge to access a CoolMasterNet unit's ASCII protocol via TCP socket.
 *
 * A single CoolMasterNet can be connected to one or more HVAC units, each with a unique UID.
 * These are individual Things inside the bridge.
 *
 * @author Angus Gratton - Initial contribution
 * @author Wouter Born - Fix null pointer exceptions and stop refresh job on update/dispose
 */
@NonNullByDefault
public class ControllerHandler extends BaseBridgeHandler {
    private static final byte LF = "\n".getBytes(StandardCharsets.US_ASCII)[0];
    private static final byte PROMPT = ">".getBytes(StandardCharsets.US_ASCII)[0];
    private static final int SINK_TIMEOUT = 1000;
    private static final int SOCKET_TIMEOUT = 2000;

    private final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    private final Object refreshLock = new Object();
    private final Object socketLock = new Object();

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable Socket socket;

    public ControllerHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing CoolMasterNet Controller handler...");
        stopRefresh();
        startRefresh();
    }

    @Override
    public void dispose() {
        stopRefresh();
        logger.debug("Disconnecting CoolMasterNet Controller handler...");
        disconnect();
        super.dispose();
    }

    private void startRefresh() {
        synchronized (refreshLock) {
            ControllerConfiguration config = getConfigAs(ControllerConfiguration.class);
            logger.debug("Scheduling new refresh job");
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshHVACUnits, 0, config.refresh, TimeUnit.SECONDS);
        }
    }

    private void stopRefresh() {
        synchronized (refreshLock) {
            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
                logger.debug("Cancelling existing refresh job");
                localRefreshJob.cancel(true);
                refreshJob = null;
            }
        }
    }

    private void refreshHVACUnits() {
        try {
            checkConnection();
            updateStatus(ThingStatus.ONLINE);
            for (Thing t : getThing().getThings()) {
                HVACHandler h = (HVACHandler) t.getHandler();
                if (h != null) {
                    h.refresh();
                }
            }
        } catch (CoolMasterClientError e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /*
     * Return true if the client socket is connected.
     *
     * Use checkConnection() to probe if the coolmasternet is responding correctly,
     * and try to re-establish the connection if possible.
     */
    public boolean isConnected() {
        synchronized (socketLock) {
            Socket localSocket = socket;
            return localSocket != null && localSocket.isConnected() && !localSocket.isClosed();
        }
    }

    /*
     * Send a particular ASCII command to the CoolMasterNet, and return the successful response as a string.
     *
     * If the "OK" prompt is not received then a CoolMasterClientError is thrown that contains whatever
     * error message was printed by the CoolMasterNet.
     */
    @SuppressWarnings("resource")
    public @Nullable String sendCommand(String command) throws CoolMasterClientError {
        synchronized (socketLock) {
            checkConnection();

            StringBuilder response = new StringBuilder();
            try {
                Socket localSocket = socket;
                if (localSocket == null || !isConnected()) {
                    throw new CoolMasterClientError(String.format("No connection for sending command %s", command));
                }

                logger.trace("Sending command '{}'", command);
                OutputStream out = localSocket.getOutputStream();
                out.write(command.getBytes(StandardCharsets.US_ASCII));
                out.write(LF);

                final Reader isr = new InputStreamReader(localSocket.getInputStream(), StandardCharsets.US_ASCII);
                final BufferedReader in = new BufferedReader(isr);
                while (true) {
                    String line = in.readLine();
                    logger.trace("Read result '{}'", line);
                    if (line == null || "OK".equals(line)) {
                        return response.toString();
                    }
                    response.append(line);
                    if (response.length() > 100) {
                        throw new CoolMasterClientError(String.format("Unexpected response to command %s", command));
                    }
                }
            } catch (SocketTimeoutException e) {
                if (response.length() == 0) {
                    throw new CoolMasterClientError(String.format("No response to command %s", command));
                }
                throw new CoolMasterClientError(String.format("Command '%s' got error '%s'", command, response));
            } catch (IOException e) {
                logger.error("{}", e.getLocalizedMessage(), e);
                return null;
            }
        }
    }

    /*
     * Verify that the client socket is connected and responding, and try to reconnect if possible.
     * May block for 1-2 seconds.
     *
     * Throws CoolMasterNetClientError if there is a connection problem.
     */
    @SuppressWarnings("resource")
    private void checkConnection() throws CoolMasterClientError {
        synchronized (socketLock) {
            ControllerConfiguration config = getConfigAs(ControllerConfiguration.class);
            try {
                if (!isConnected()) {
                    connect();
                    if (!isConnected()) {
                        throw new CoolMasterClientError(
                                String.format("Failed to connect to %s:%s", config.host, config.port));
                    }
                }

                Socket localSocket = socket;
                if (localSocket == null) {
                    throw new CoolMasterClientError(
                            String.format("Failed to connect to %s:%s", config.host, config.port));
                }

                InputStream in = localSocket.getInputStream();

                // Sink (clear) buffer until earlier of the SINK_TIMEOUT or > prompt
                try {
                    localSocket.setSoTimeout(SINK_TIMEOUT);
                    while (true) {
                        int b = in.read();
                        if (b == -1) {
                            break;
                        }
                        if (b == PROMPT) {
                            if (in.available() > 0) {
                                throw new IOException("Unexpected data following prompt");
                            }
                            logger.trace("Buffer empty following unsolicited > prompt");
                            return;
                        }
                    }
                } catch (final SocketTimeoutException expectedFromRead) {
                } finally {
                    localSocket.setSoTimeout(SOCKET_TIMEOUT);
                }

                // Solicit for a prompt given we haven't received one earlier
                final OutputStream out = localSocket.getOutputStream();
                out.write(LF);

                // Block until the > prompt arrives or IOE if SOCKET_TIMEOUT
                final int b = in.read();
                if (b != PROMPT) {
                    throw new IOException("Unexpected character received");
                }
                if (in.available() > 0) {
                    throw new IOException("Unexpected data following prompt");
                }
                logger.trace("Buffer empty following solicited > prompt");
            } catch (IOException e) {
                disconnect();
                logger.debug("{}", e.getLocalizedMessage(), e);
                throw new CoolMasterClientError(
                        String.format("No response from CoolMasterNet unit %s:%s", config.host, config.port));
            }
        }
    }

    private void connect() throws IOException {
        synchronized (socketLock) {
            ControllerConfiguration config = getConfigAs(ControllerConfiguration.class);
            try {
                Socket localSocket = new Socket();
                localSocket.connect(new InetSocketAddress(config.host, config.port), SOCKET_TIMEOUT);
                localSocket.setSoTimeout(SOCKET_TIMEOUT);
                socket = localSocket;
            } catch (UnknownHostException e) {
                logger.error("Unknown socket host: {}", config.host);
                socket = null;
            } catch (SocketException e) {
                logger.error("Failed to connect to {}:{}: {}", config.host, config.port, e.getLocalizedMessage(), e);
                socket = null;
            }
        }
    }

    private void disconnect() {
        synchronized (socketLock) {
            Socket localSocket = socket;
            if (localSocket != null) {
                try {
                    localSocket.close();
                } catch (IOException e1) {
                    logger.error("{}", e1.getLocalizedMessage(), e1);
                }
                socket = null;
            }
        }
    }

    @NonNullByDefault
    public class CoolMasterClientError extends Exception {
        private static final long serialVersionUID = 1L;

        public CoolMasterClientError(String message) {
            super(message);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
