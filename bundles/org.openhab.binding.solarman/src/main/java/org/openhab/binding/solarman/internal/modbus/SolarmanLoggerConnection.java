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
package org.openhab.binding.solarman.internal.modbus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanLoggerConnection implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(SolarmanLoggerConnection.class);
    @Nullable
    private Socket socket;

    public SolarmanLoggerConnection(String hostName, int port) {
        SocketAddress sockaddr = new InetSocketAddress(hostName, port);
        Socket localSocket = connectSocket(sockaddr);

        if (localSocket == null) {
            logger.debug("Error creating socket");
        } else {
            socket = localSocket;
        }
    }

    public byte[] sendRequest(byte[] reqFrame) throws SolarmanConnectionException {
        // Will not be used by multiple threads, so not bothering making it thread safe for now
        Socket localSocket = socket;

        if (localSocket == null) {
            throw new SolarmanConnectionException("Socket is null, not reading data this time");
        }

        try {
            logger.trace("Request frame: {}", bytesToHex(reqFrame));
            localSocket.getOutputStream().write(reqFrame);
        } catch (IOException e) {
            logger.debug("Unable to send frame to logger");
            return new byte[0];
        }

        byte[] buffer = new byte[1024];
        int attempts = 5;

        while (attempts > 0) {
            attempts--;
            try {
                int bytesRead = localSocket.getInputStream().read(buffer);
                if (bytesRead < 0) {
                    throw new SolarmanConnectionException("No data received");
                } else {
                    byte[] data = Arrays.copyOfRange(buffer, 0, bytesRead);
                    if (logger.isDebugEnabled()) {
                        logger.trace("Response frame: {}", bytesToHex(data));
                    }
                    return data;
                }
            } catch (SocketTimeoutException e) {
                logger.debug("Connection timeout", e);
                if (attempts == 0) {
                    throw new SolarmanConnectionException("Too many socket timeouts", e);
                }
            } catch (IOException e) {
                throw new SolarmanConnectionException("Error reading data from ", e);
            }
        }

        return new byte[0];
    }

    protected static String bytesToHex(byte[] bytes) {
        return IntStream.range(0, bytes.length).mapToObj(i -> String.format("%02X", bytes[i]))
                .collect(Collectors.joining());
    }

    private @Nullable Socket connectSocket(SocketAddress socketAddress) {
        try {
            Socket clientSocket = new Socket();

            clientSocket.setSoTimeout(10_000);
            clientSocket.connect(socketAddress, 10_000);

            return clientSocket;
        } catch (IOException e) {
            logger.debug("Could not open socket on IP {}", socketAddress, e);
            return null;
        }
    }

    @Override
    public void close() {
        Socket localSocket = socket;
        if (localSocket != null && !localSocket.isClosed()) {
            try {
                localSocket.close();
            } catch (IOException e) {
                logger.debug("Unable to close connection");
            }
        }
        socket = null;
    }

    public boolean isConnected() {
        Socket localSocket = socket;
        return localSocket != null && localSocket.isConnected();
    }
}
