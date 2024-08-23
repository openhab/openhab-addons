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
import org.openhab.binding.nobohub.internal.model.OverrideMode;
import org.openhab.binding.nobohub.internal.model.OverridePlan;
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

    private final String hostName;
    private final NoboHubBridgeHandler hubHandler;
    private final String serialNumber;

    private @Nullable InetAddress host;
    private @Nullable Socket hubConnection;
    private @Nullable PrintWriter out;
    private @Nullable BufferedReader in;

    public HubConnection(String hostName, String serialNumber, NoboHubBridgeHandler hubHandler)
            throws NoboCommunicationException {
        this.hostName = hostName;
        this.serialNumber = serialNumber;
        this.hubHandler = hubHandler;
    }

    public void connect() throws NoboCommunicationException {
        connectSocket();

        String hello = String.format("HELLO %s %s %s\r", NoboHubBindingConstants.API_VERSION, serialNumber,
                getDateString());
        write(hello);

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

        OverridePlan overridePlan = OverridePlan.fromMode(nextMode, LocalDateTime.now());
        sendCommand(overridePlan.generateCommandString("A03"));

        String line = "";
        while (line != null && !line.startsWith("B03")) {
            line = readLine();
            hubHandler.receivedData(line);
        }

        String l = line;
        if (null != l) {
            OverridePlan newOverridePlan = OverridePlan.fromH04(l);
            hub.setActiveOverrideId(newOverridePlan.getId());
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

        String line = "";
        while (line != null && !line.startsWith("H05")) {
            line = readLine();
            hubHandler.receivedData(line);
        }
    }

    public boolean isConnected() {
        Socket conn = this.hubConnection;
        if (null != conn) {
            return conn.isConnected();
        }

        return false;
    }

    public boolean hasData() throws NoboCommunicationException {
        BufferedReader i = this.in;
        if (null != i) {
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
            Socket conn = this.hubConnection;
            if (null == conn) {
                throw new NoboCommunicationException("No connection to Hub");
            }

            logger.trace("Reading from Hub, waiting maximum {}", Helpers.formatDuration(timeout));
            conn.setSoTimeout((int) timeout.toMillis());

            try {
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
        BufferedReader reader = this.in;
        try {
            if (null != reader) {
                String line = reader.readLine();
                if (line != null) {
                    logger.trace("Reading raw data string from Nobø Hub: {}", line);
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
        @Nullable
        PrintWriter o = this.out;
        if (null != o) {
            logger.trace("Sending '{}'", s);
            o.write(s);
            o.flush();
        }
    }

    private void connectSocket() throws NoboCommunicationException {
        if (null == host) {
            try {
                host = InetAddress.getByName(hostName);
            } catch (IOException ioex) {
                throw new NoboCommunicationException(String.format("Failed to resolve IP address of %s", hostName),
                        ioex);
            }
        }
        try {
            Socket conn = new Socket(host, NoboHubBindingConstants.NOBO_HUB_TCP_PORT);
            out = new PrintWriter(conn.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            hubConnection = conn;
        } catch (IOException ioex) {
            throw new NoboCommunicationException(String.format("Failed connecting to Nobø Hub at %s", hostName), ioex);
        }
    }

    public void disconnect() throws NoboCommunicationException {
        try {
            PrintWriter o = this.out;
            if (o != null) {
                o.close();
            }

            BufferedReader i = this.in;
            if (i != null) {
                i.close();
            }

            Socket conn = this.hubConnection;
            if (conn != null) {
                conn.close();
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
