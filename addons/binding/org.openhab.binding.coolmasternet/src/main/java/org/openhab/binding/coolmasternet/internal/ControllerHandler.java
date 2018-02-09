/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet.internal;

import static org.openhab.binding.coolmasternet.internal.config.CoolMasterNetConfiguration.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.coolmasternet.handler.HVACHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge to access a CoolMasterNet unit's ASCII protocol via TCP socket.
 *
 * A single CoolMasterNet can be connected to one or more HVAC units, each with a unique UID.
 * These are individual Things inside the bridge.
 *
 * @author Angus Gratton
 */
public class ControllerHandler extends BaseBridgeHandler {
    private static final int SOCKET_TIMEOUT = 2000;
    private final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    private String host;
    private int port;
    private Socket socket;
    private final Object lock = new Object();
    private ScheduledFuture<?> refreshJob;

    public ControllerHandler(Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initialising CoolMasterNet Controller handler...");

        Configuration config = this.getConfig();
        host = (String) config.get(HOST);
        port = 10102;
        try {
            port = ((BigDecimal) config.get(PORT)).intValue();
        } catch (NullPointerException e) {
            // keep default
        }

        int refresh = 5;
        try {
            refresh = ((BigDecimal) config.get(REFRESH)).intValue();
        } catch (NullPointerException e) {
            // keep default
        }

        Runnable refreshHVACUnits = new Runnable() {
            @Override
            public void run() {
                try {
                    checkConnection();
                    updateStatus(ThingStatus.ONLINE);
                    for (Thing t : getThing().getThings()) {
                        HVACHandler h = (HVACHandler) t.getHandler();
                        h.refresh();
                    }
                } catch (CoolMasterClientError e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        };
        scheduler.scheduleWithFixedDelay(refreshHVACUnits, 0, refresh, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        super.dispose();
    }

    /*
     * Return true if the client socket is connected.
     *
     * Use checkConnection() to probe if the coolmasternet is responding correctly,
     * and try to re-establish the connection if possible.
     */
    public boolean isConnected() {
        synchronized (this.lock) {
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }

    /*
     * Send a particular ASCII command to the CoolMasterNet, and return the successful response as a string.
     *
     * If the "OK" prompt is not received then a CoolMasterClientError is thrown that contains whatever
     * error message was printed by the CoolMasterNet.
     */
    public String sendCommand(String command) throws CoolMasterClientError {
        synchronized (this.lock) {
            checkConnection();

            StringBuilder response = new StringBuilder();
            try {
                logger.trace("Sending command '{}'", command);
                OutputStream out = socket.getOutputStream();
                out.write(command.getBytes());
                out.write("\r\n".getBytes());

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
     *
     */
    public void checkConnection() throws CoolMasterClientError {
        synchronized (this.lock) {
            try {
                if (!isConnected()) {
                    connect();
                    if (!isConnected()) {
                        throw new CoolMasterClientError(String.format("Failed to connect to %s:%s", host, port));
                    }
                }

                java.io.InputStream in = socket.getInputStream();
                /* Flush anything pending in the input stream */
                while (in.available() > 0) {
                    in.read();
                }
                /* Send a CRLF, expect a > prompt (and a CRLF) back */
                OutputStream out = socket.getOutputStream();
                out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
                /*
                 * this will time out with IOException if it doesn't see that prompt
                 * with no other data following it, within 1 second (socket timeout)
                 */
                final byte PROMPT = ">".getBytes(StandardCharsets.US_ASCII)[0];
                while (in.read() != PROMPT || in.available() > 3) {
                }
            } catch (IOException e) {
                disconnect();
                logger.error("{}", e.getLocalizedMessage(), e);
                throw new CoolMasterClientError(String.format("No response from CoolMasterNet unit %s:%s", host, port));
            }
        }
    }

    private void connect() throws IOException {
        synchronized (this.lock) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
                socket.setSoTimeout(SOCKET_TIMEOUT);
            } catch (UnknownHostException e) {
                logger.error("unknown socket host {}", host);
                socket = null;
            } catch (SocketException e) {
                logger.error("{}", e.getLocalizedMessage(), e);
                socket = null;
            }
        }
    }

    public void disconnect() {
        synchronized (this.lock) {
            try {
                socket.close();
            } catch (IOException e1) {
                logger.error("{}", e1.getLocalizedMessage(), e1);
            }
            socket = null;
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
