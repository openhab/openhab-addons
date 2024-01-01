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
package org.openhab.binding.mynice.internal.handler;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiConnector} is responsible for reading and writing to the It4Wifi.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class It4WifiConnector extends Thread {
    private static final char ETX = '\u0003';
    private static final char STX = '\u0002';

    private final Logger logger = LoggerFactory.getLogger(It4WifiConnector.class);
    private final It4WifiHandler handler;
    private final InputStreamReader in;
    private final OutputStreamWriter out;

    public It4WifiConnector(It4WifiHandler handler, SSLSocket sslSocket) throws IOException {
        super(It4WifiConnector.class.getName());
        this.handler = handler;
        this.in = new InputStreamReader(sslSocket.getInputStream());
        this.out = new OutputStreamWriter(sslSocket.getOutputStream());
        setDaemon(true);
    }

    @Override
    public void run() {
        String buffer = "";
        int data;

        while (!interrupted()) {
            try {
                while ((data = in.read()) != -1) {
                    if (data == STX) {
                        buffer = "";
                    } else if (data == ETX) {
                        handler.received(buffer);
                    } else {
                        buffer += (char) data;
                    }
                }
            } catch (IOException e) {
                handler.communicationError(e.toString());
                interrupt();
            }
        }
    }

    @Override
    public void interrupt() {
        logger.debug("Closing streams");
        tryClose(in);
        tryClose(out);

        super.interrupt();
    }

    public synchronized void sendCommand(String command) {
        logger.debug("Sending ItT4Wifi :{}", command);
        try {
            out.write(STX + command + ETX);
            out.flush();
        } catch (IOException e) {
            handler.communicationError(e.toString());
        }
    }

    private void tryClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.debug("Exception closing stream : {}", e.getMessage());
        }
    }
}
