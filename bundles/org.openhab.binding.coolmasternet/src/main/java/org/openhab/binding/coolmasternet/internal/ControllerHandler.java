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
package org.openhab.binding.coolmasternet.internal;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledFuture;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.coolmasternet.internal.config.ControllerConfiguration;
import org.openhab.binding.coolmasternet.internal.handler.HVACHandler;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge to access a CoolMasterNet unit's ASCII protocol via TCP socket.
 *
 * <p>
 * A single CoolMasterNet can be connected to one or more HVAC units, each with
 * a unique UID. Each HVAC is an individual thing inside the bridge.
 *
 * @author Angus Gratton - Initial contribution
 * @author Wouter Born - Fix null pointer exceptions and stop refresh job on update/dispose
 */
@NonNullByDefault
public final class ControllerHandler extends BaseBridgeHandler {
    private static final String LF = "\n";
    private static final byte PROMPT = ">".getBytes(US_ASCII)[0];
    private static final int LS_LINE_LENGTH = 36;
    private static final int LS_LINE_TEMP_SCALE_OFFSET = 13;
    private static final int MAX_VALID_LINE_LENGTH = LS_LINE_LENGTH * 20;
    private static final int SINK_TIMEOUT_MS = 25;
    private static final int SOCKET_TIMEOUT_MS = 2000;

    private ControllerConfiguration cfg = new ControllerConfiguration();
    private Unit<?> unit = SIUnits.CELSIUS;
    private final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    private final Object socketLock = new Object();

    private @Nullable ScheduledFuture<?> poller;
    private @Nullable Socket socket;

    public ControllerHandler(final Bridge thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        cfg = getConfigAs(ControllerConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        determineTemperatureUnits();
        stopPoller();
        startPoller();
    }

    @Override
    public void dispose() {
        updateStatus(ThingStatus.OFFLINE);
        stopPoller();
        disconnect();
    }

    /**
     * Obtain the temperature unit configured for this controller.
     *
     * <p>
     * CoolMasterNet defaults to Celsius, but allows a user to change the scale
     * on a per-controller basis using the ASCII I/F "set deg" command. Given
     * changing the unit is very rarely performed, there is no direct support
     * for doing so within this binding.
     *
     * @return the unit as determined from the first line of the "ls" command
     */
    public Unit<?> getUnit() {
        return unit;
    }

    private void determineTemperatureUnits() {
        synchronized (socketLock) {
            try {
                checkConnection();
                final String ls = sendCommand("ls");
                if (ls.length() < LS_LINE_LENGTH) {
                    throw new CoolMasterClientError("Invalid 'ls' response: '%s'", ls);
                }
                final char scale = ls.charAt(LS_LINE_TEMP_SCALE_OFFSET);
                unit = scale == 'C' ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
                logger.trace("Temperature scale '{}' set to {}", scale, unit);
            } catch (final IOException ioe) {
                logger.warn("Could not determine temperature scale", ioe);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ioe.getMessage());
            }
        }
    }

    private void startPoller() {
        synchronized (scheduler) {
            logger.debug("Scheduling new poller");
            poller = scheduler.scheduleWithFixedDelay(this::poll, 0, cfg.refresh, SECONDS);
        }
    }

    private void stopPoller() {
        synchronized (scheduler) {
            final ScheduledFuture<?> poller = this.poller;
            if (poller != null) {
                logger.debug("Cancelling existing poller");
                poller.cancel(true);
                this.poller = null;
            }
        }
    }

    private void poll() {
        try {
            checkConnection();
        } catch (final IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ioe.getMessage());
            return;
        }
        for (Thing t : getThing().getThings()) {
            final HVACHandler h = (HVACHandler) t.getHandler();
            if (h != null) {
                h.refresh();
            }
        }
        if (isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /**
     * Passively determine if the client socket appears to be connected, but do
     * modify the connection state.
     *
     * <p>
     * Use {@link #checkConnection()} if active verification (and potential
     * reconnection) of the CoolNetMaster connection is required.
     */
    public boolean isConnected() {
        synchronized (socketLock) {
            final Socket socket = this.socket;
            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }

    /**
     * Send a specific ASCII I/F command to CoolMasterNet and return its response.
     *
     * <p>
     * This method automatically acquires a connection.
     *
     * @return the server response to the command (never empty)
     * @throws IOException if communications failed with the server
     */
    public String sendCommand(final String command) throws IOException {
        synchronized (socketLock) {
            checkConnection();

            final StringBuilder response = new StringBuilder();
            try {
                final Socket socket = this.socket;
                if (socket == null || !isConnected()) {
                    throw new CoolMasterClientError(String.format("No connection for sending command %s", command));
                }

                logger.trace("Sending command '{}'", command);
                final Writer out = new OutputStreamWriter(socket.getOutputStream(), US_ASCII);
                out.write(command);
                out.write(LF);
                out.flush();

                final Reader isr = new InputStreamReader(socket.getInputStream(), US_ASCII);
                final BufferedReader in = new BufferedReader(isr);
                while (true) {
                    String line = in.readLine();
                    logger.trace("Read result '{}'", line);
                    if (line == null || "OK".equals(line)) {
                        return response.toString();
                    }
                    response.append(line);
                    if (response.length() > MAX_VALID_LINE_LENGTH) {
                        throw new CoolMasterClientError("Command '%s' received unexpected response '%s'", command,
                                response);
                    }
                }
            } catch (final SocketTimeoutException ste) {
                if (response.length() == 0) {
                    throw new CoolMasterClientError("Command '%s' received no response", command);
                }
                throw new CoolMasterClientError("Command '%s' received truncated response '%s'", command, response);
            }
        }
    }

    /**
     * Ensure a client socket is connected and ready to receive commands.
     *
     * <p>
     * This method may block for up to {@link #SOCKET_TIMEOUT_MS}, depending on
     * the state of the connection. This usual time is {@link #SINK_TIMEOUT_MS}.
     *
     * <p>
     * Return of this method guarantees the socket is ready to receive a
     * command. If the socket could not be made ready, an exception is raised.
     *
     * @throws IOException if the socket could not be made ready
     */
    private void checkConnection() throws IOException {
        synchronized (socketLock) {
            try {
                // Longer sink time used for initial connection welcome > prompt
                final int sinkTime;
                if (isConnected()) {
                    sinkTime = SINK_TIMEOUT_MS;
                } else {
                    sinkTime = SOCKET_TIMEOUT_MS;
                    connect();
                }

                final Socket socket = this.socket;
                if (socket == null) {
                    throw new IllegalStateException(
                            "Socket is null, which is unexpected because it was verified as available earlier in the same synchronized block; please log a bug report");
                }
                final InputStream in = socket.getInputStream();

                // Sink (clear) buffer until earlier of the sinkTime or > prompt
                try {
                    socket.setSoTimeout(sinkTime);
                    logger.trace("Waiting {} ms for buffer to sink", sinkTime);
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
                    socket.setSoTimeout(SOCKET_TIMEOUT_MS);
                }

                // Solicit for a prompt given we haven't received one earlier
                final Writer out = new OutputStreamWriter(socket.getOutputStream(), US_ASCII);
                out.write(LF);
                out.flush();

                // Block until the > prompt arrives or IOE if SOCKET_TIMEOUT_MS
                final int b = in.read();
                if (b != PROMPT) {
                    throw new IOException("Unexpected character received");
                }
                if (in.available() > 0) {
                    throw new IOException("Unexpected data following prompt");
                }
                logger.trace("Buffer empty following solicited > prompt");
            } catch (final IOException ioe) {
                disconnect();
                throw ioe;
            }
        }
    }

    /**
     * Opens the socket.
     *
     * <p>
     * Guarantees to either open the socket or thrown an exception.
     *
     * @throws IOException if the socket could not be opened
     */
    private void connect() throws IOException {
        synchronized (socketLock) {
            try {
                logger.debug("Connecting to {}:{}", cfg.host, cfg.port);
                final Socket socket = new Socket();
                socket.connect(new InetSocketAddress(cfg.host, cfg.port), SOCKET_TIMEOUT_MS);
                socket.setSoTimeout(SOCKET_TIMEOUT_MS);
                this.socket = socket;
            } catch (final IOException ioe) {
                socket = null;
                throw ioe;
            }
        }
    }

    /**
     * Attempts to disconnect the socket.
     *
     * <p>
     * Disconnection failure is not considered an error, although will be logged.
     */
    private void disconnect() {
        synchronized (socketLock) {
            final Socket socket = this.socket;
            if (socket != null) {
                logger.debug("Disconnecting from {}:{}", cfg.host, cfg.port);
                try {
                    socket.close();
                } catch (final IOException ioe) {
                    logger.warn("Could not disconnect", ioe);
                }
                this.socket = null;
            }
        }
    }

    /**
     * Encodes ASCII I/F protocol error messages.
     *
     * <p>
     * This exception is not used for normal socket and connection failures.
     * It is only used when there is a protocol level error (eg unexpected
     * messages or malformed content from the CoolNetMaster server).
     */
    public class CoolMasterClientError extends IOException {
        private static final long serialVersionUID = 2L;

        public CoolMasterClientError(final String message) {
            super(message);
        }

        public CoolMasterClientError(String format, Object... args) {
            super(String.format(format, args));
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }
}
