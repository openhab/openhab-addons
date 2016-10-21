/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneMinderTelnetConnection {

    private final Logger logger = LoggerFactory.getLogger(ZoneMinderTelnetConnection.class);

    private String hostname;
    private Integer port;
    private Integer timeout;

    private Socket telnetSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    public ZoneMinderTelnetConnection(String hostname, Integer port, Integer timeout) {
        initialize(hostname, port, timeout);
    }

    protected void initialize(String hostname, Integer port, Integer timeout) {
        this.hostname = hostname;
        this.port = port;
        this.timeout = timeout;

        connect();
    }

    protected void connect() {
        try {
            telnetSocket = new Socket(hostname, port);
            synchronized (telnetSocket) {
                // Make sure read operations does have a timeout
                telnetSocket.setSoTimeout(timeout);
                telnetSocket.setKeepAlive(true);

                in = new BufferedReader(new InputStreamReader(telnetSocket.getInputStream()));
                out = new PrintWriter(telnetSocket.getOutputStream(), true);
            }
        } catch (IOException e) {
            return;
        }

    }

    public void close() {

        synchronized (telnetSocket) {
            try {
                in.close();
                out.close();
                telnetSocket.close();
            } catch (IOException e) {
                logger.error("close(): Error occurred in TelnetConnection to ZoneMinder Server. Exception='{}'",
                        e.getMessage());
            }
        }
    }

    private void reconnect() {
        synchronized (telnetSocket) {
            try {
                if (telnetSocket.isClosed()) {
                    in.close();
                    out.close();
                    telnetSocket.close();
                }
            } catch (IOException e) {
                logger.error("recconnect(): Error occurred in TelnetConnection to ZoneMinder Server. Exception='{}'",
                        e.getMessage());
            }

            connect();
        }

    }

    public ZoneMinderEvent readInput() {

        ZoneMinderEvent event = null;
        String result = null;

        // Check if connection is available
        synchronized (telnetSocket) {
            if (telnetSocket.isClosed()) {
                reconnect();
            }
        }
        try {
            result = in.readLine();
            if (result != null) {
                event = new ZoneMinderEvent(ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR, result);
            }

        } catch (SocketTimeoutException e) {
            // Just ignore timeouts
            logger.trace("readInput(): SocketTimeout occurred in TelnetConnection to ZoneMinder Server.");
        } catch (IOException e) {
            // Occurs if socket is closed, probably because we are shutting down. Else it will be fixed on next run
            logger.trace(
                    "readInput(): IOException occurred in TelnetConnection to ZoneMinder Server. Either we are shutting down, or some error occurred. The binding will automatically recorver.");
        }
        return event;
    }

}
