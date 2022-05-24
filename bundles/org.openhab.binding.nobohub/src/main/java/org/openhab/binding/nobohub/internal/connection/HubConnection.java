/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nobohub.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.Helpers;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.binding.nobohub.internal.model.Hub;
import org.openhab.binding.nobohub.internal.model.NoboCommunicationException;
import org.openhab.binding.nobohub.internal.model.NoboDataException;
import org.openhab.binding.nobohub.internal.model.Override;
import org.openhab.binding.nobohub.internal.model.OverrideMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection to the Nobø Hub (Socket wrapper).
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class HubConnection {

    private final Logger logger = LoggerFactory.getLogger(HubConnection.class);

    private final InetAddress host;
    private final NoboHubBridgeHandler hubHandler;
    private final String serialNumber;

    private @Nullable Socket hubConnection;
    private @Nullable PrintWriter out;
    private @Nullable BufferedReader in;

    public HubConnection(String hostName, String serialNumber, NoboHubBridgeHandler hubHandler)
            throws NoboCommunicationException {
        try {
            host = InetAddress.getByName(hostName);
        } catch (IOException ioex) {
            throw new NoboCommunicationException(String.format("Failed to resolve IP address of %s", hostName));
        }

        this.hubHandler = hubHandler;
        this.serialNumber = serialNumber;
    }

    public void connect() throws NoboCommunicationException {
        connectSocket();

        String hello = String.format("HELLO %s %s %s\r", NoboHubBindingConstants.API_VERSION, serialNumber,
                getDateString());
        write(hello);
        @Nullable
        String helloRes = readLine();
        if (null == helloRes || !helloRes.startsWith("HELLO")) {
            if (helloRes != null && helloRes.startsWith("REJECT")) {
                String[] reject = helloRes.split(" ", 2);
                throw new NoboCommunicationException(String.format("Hub rejects us with reason %s: %s", reject[1],
                        NoboHubBindingConstants.REJECT_REASONS.get(reject[1])));
            } else {
                throw new NoboCommunicationException("Hub rejects us with unknown reason");
            }
        }

        write("HANDSHAKE\r");
        @Nullable
        String handshakeRes = readLine();
        if (null == handshakeRes || !handshakeRes.startsWith("HANDSHAKE")) {
            throw new NoboCommunicationException("Hub rejects handshake");
        }

        refreshAllNoReconnect();
    }

    public void handshake() throws NoboCommunicationException {
        if (!isConnected()) {
            connect();
        } else {
            write("HANDSHAKE\r");
        }
    }

    public void setOverride(Hub hub, OverrideMode nextMode) throws NoboDataException, NoboCommunicationException {
        if (!isConnected()) {
            connect();
        }

        Override override = Override.fromMode(nextMode, LocalDateTime.now());
        sendCommand(override.generateCommandString("A03"));
        @Nullable
        String line = "";
        while (line != null && !line.startsWith("B03")) {
            line = readLine();
            hubHandler.receivedData(line);
        }

        if (null != line) {
            String l = Helpers.castToNonNull(line, "line");
            Override newOverride = Override.fromH04(l);
            hub.setActiveOverrideId(newOverride.getId());
            sendCommand(hub.generateCommandString("U03"));
        }
    }

    public void refreshAll() throws NoboCommunicationException {
        if (!isConnected()) {
            connect();
        } else {
            refreshAllNoReconnect();
        }
    }

    private void refreshAllNoReconnect() throws NoboCommunicationException {
        write("G00\r");

        @Nullable
        String line = "";
        while (line != null && !line.startsWith("H05")) {
            line = readLine();
            hubHandler.receivedData(line);
        }
    }

    public boolean isConnected() {
        if (hubConnection != null) {
            Socket conn = Helpers.castToNonNull(hubConnection, "hubConnection");
            return conn.isConnected();
        }

        return false;
    }

    public boolean hasData() throws NoboCommunicationException {
        if (null != in) {
            BufferedReader i = Helpers.castToNonNull(in, "in");
            try {
                return i.ready();
            } catch (IOException ioex) {
                throw new NoboCommunicationException("Failed detecting if buffer has any data", ioex);
            }
        }

        return false;
    }

    public void processReads(Duration timeout) throws NoboCommunicationException {
        try {
            if (null == hubConnection) {
                throw new NoboCommunicationException("No connection to Hub");
            }

            Socket conn = Helpers.castToNonNull(hubConnection, "hubConnection");

            logger.debug("Reading from Hub, waiting maximum {}", Helpers.formatDuration(timeout));
            conn.setSoTimeout((int) timeout.toMillis());

            try {
                @Nullable
                String line = readLine();
                if (line != null && line.startsWith("HANDSHAKE")) {
                    line = readLine();
                }

                hubHandler.receivedData(line);
            } catch (NoboCommunicationException nce) {
                if (!(nce.getCause() instanceof SocketTimeoutException)) {
                    connectSocket();
                }
            }
        } catch (SocketException se) {
            throw new NoboCommunicationException("Failed setting read timeout", se);
        }
    }

    private @Nullable String readLine() throws NoboCommunicationException {
        try {
            if (null != in) {
                BufferedReader reader = Helpers.castToNonNull(in, "in");
                String line = reader.readLine();
                if (line != null) {
                    logger.debug("Reading raw data string from Nobø Hub: {}", line);
                }
                return line;
            }
        } catch (IOException ioex) {
            throw new NoboCommunicationException("Failed reading from Nobø Hub", ioex);
        }

        return null;
    }

    public void sendCommand(String command) {
        write(command);
    }

    private void write(String s) {
        if (null != out) {
            logger.debug("Sending '{}'", s);
            PrintWriter o = Helpers.castToNonNull(out, "out");
            o.write(s);
            o.flush();
        }
    }

    private void connectSocket() throws NoboCommunicationException {
        try {
            Socket conn = new Socket(host, NoboHubBindingConstants.NOBO_HUB_TCP_PORT);
            out = new PrintWriter(conn.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            hubConnection = conn;
        } catch (IOException ioex) {
            throw new NoboCommunicationException(
                    String.format("Failed connecting to Nobø Hub at %s", host.getHostName()), ioex);
        }
    }

    public void disconnect() throws NoboCommunicationException {
        try {
            if (out != null) {
                Helpers.castToNonNull(out, "out").close();
            }

            if (in != null) {
                Helpers.castToNonNull(in, "in").close();
            }

            if (hubConnection != null) {
                Helpers.castToNonNull(hubConnection, "hubConnection").close();
            }
        } catch (IOException ioex) {
            throw new NoboCommunicationException("Error disconnecting from Hub", ioex);
        }
    }

    public void hardReconnect() throws NoboCommunicationException {
        disconnect();
        connect();
    }

    private String getDateString() {
        return LocalDateTime.now().format(NoboHubBindingConstants.DATE_FORMAT_SECONDS);
    }
}
