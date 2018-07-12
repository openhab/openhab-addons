/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.openhab.binding.coolmasternet.handler.HVACHandler;
import org.openhab.binding.coolmasternet.internal.config.ControllerConfiguration;
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
                out.write(command.getBytes());
                out.write("\r\n".getBytes());

                BufferedReader in = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
                while (true) {
                    String line = in.readLine();
                    logger.trace("Read result '{}'", line);
                    if ("OK".equals(line)) {
                        return response.toString();
                    }
                    response.append(line);
                    if (response.length() > 100) {
                        /*
                         * Usually this loop times out on errors, but in the case that we just keep getting
                         * data we should also fail with an error.
                         */
                        throw new CoolMasterClientError(String.format("Got gibberish response to command %s", command));
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
                /* Flush anything pending in the input stream */
                while (in.available() > 0) {
                    in.read();
                }
                /* Send a CRLF, expect a > prompt (and a CRLF) back */
                OutputStream out = localSocket.getOutputStream();
                out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
                /*
                 * this will time out with IOException if it doesn't see that prompt
                 * with no other data following it, within 1 second (socket timeout)
                 */
                final byte prompt = ">".getBytes(StandardCharsets.US_ASCII)[0];
                while (in.read() != prompt || in.available() > 3) {
                    continue; // empty by design
                }
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
