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
package org.openhab.binding.panamaxfurman.internal.transport;

import static org.openhab.binding.panamaxfurman.internal.util.Util.exceptionToLog;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanCommunicationEventListener;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionActiveEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionBrokenEvent;
import org.openhab.binding.panamaxfurman.internal.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the communication to a Panamax/Furman Power Conditioner with a BlueBOLT-CV1 or BlueBOLT-CV2 card via telnet
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanmaxFurmanConnector {
    private final Logger logger = LoggerFactory.getLogger(PanmaxFurmanConnector.class);

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;

    private final String connectionName;
    private final String address;
    private final int port;
    private final int socketReadTimeoutSeconds;
    private final PanamaxFurmanCommunicationEventListener eventGenerator;

    private final Semaphore readerReadySemaphore = new Semaphore(1);
    private final Object ioMutex = new Object();

    /**
     * socketConnection will be recreated as necessary
     */
    private SocketConnection socketConnection;

    public PanmaxFurmanConnector(String connectionName, String address, int port, int socketReadTimeoutSeconds,
            PanamaxFurmanCommunicationEventListener eventGenerator) throws PanamaxFurmanDeviceUnavailableException {
        this.connectionName = connectionName;
        this.address = address;
        this.port = port;
        this.socketReadTimeoutSeconds = socketReadTimeoutSeconds;
        this.eventGenerator = eventGenerator;
        this.socketConnection = connectToDevice();
    }

    public void sendRequestToDevice(@Nullable String textToSend) {
        sendRequestToDevice(textToSend, true);
    }

    public synchronized void sendRequestToDevice(@Nullable String command, boolean retry) {
        if (command == null) {
            return;
        }
        synchronized (ioMutex) {
            try {
                socketConnection.writer.write(command + "\r");
                socketConnection.writer.flush();
                logger.debug(">> {}    @{}", command, connectionName);
            } catch (IOException e) {
                logger.debug("Connection with device is broken. retry={}  @{}", retry, connectionName, e);
                if (retry) {
                    try {
                        this.socketConnection = connectToDevice();
                        sendRequestToDevice(command, false);
                    } catch (PanamaxFurmanDeviceUnavailableException e1) {
                        eventGenerator.onConnectivityEvent(new PanamaxFurmanConnectionBrokenEvent(e1));
                        // Nothing to log, error was logged in connectToDevice()
                        // Don't rethrow as not being able to connect sometimes is a normal condition
                    }
                }
            }
        }
    }

    private synchronized SocketConnection connectToDevice() throws PanamaxFurmanDeviceUnavailableException {
        synchronized (ioMutex) {
            if (socketConnection != null && !socketConnection.inputReaderThread.isReading()) {
                closeConnection(socketConnection);
            }

            logger.trace("Attempting to connect via telnet @{}", connectionName);
            @Nullable
            InputReaderThread inputReaderThread = null;
            Socket telnetSocket = null;
            BufferedWriter writer = null;
            try {
                telnetSocket = new Socket();
                telnetSocket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT_MILLIS);
                telnetSocket.setKeepAlive(true);
                telnetSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(socketReadTimeoutSeconds));
                // new InputStreamThreadReader does readerReadySemaphore.aquire()
                inputReaderThread = new InputReaderThread(connectionName, readerReadySemaphore,
                        telnetSocket.getInputStream(), eventGenerator);
                inputReaderThread.start();
                logger.trace("Starting reader thread with read timeout of {}s, waiting for read @{}",
                        socketReadTimeoutSeconds, connectionName);
                // InputStreamThreadReader.start() will call readerReadySemaphore.release() once it's started
                readerReadySemaphore.acquire();
                logger.trace("received reader runnable started signal @{}", connectionName);
                writer = new BufferedWriter(
                        new OutputStreamWriter(telnetSocket.getOutputStream(), StandardCharsets.US_ASCII));
                eventGenerator.onConnectivityEvent(new PanamaxFurmanConnectionActiveEvent());
                return new SocketConnection(telnetSocket, writer, inputReaderThread);
            } catch (IOException | InterruptedException e) {
                logger.warn("Could not connect because of {} {}   @{}", e.getClass().getSimpleName(), e.getMessage(),
                        connectionName, exceptionToLog(logger, e));
                eventGenerator.onConnectivityEvent(new PanamaxFurmanConnectionBrokenEvent(e));
                // should any errors occur, clean everything up
                if (inputReaderThread != null) {
                    inputReaderThread.shutdown();
                }
                Util.closeQuietly(telnetSocket);
                Util.closeQuietly(writer);
                throw new PanamaxFurmanDeviceUnavailableException("Error connecting to " + address + ":" + port, e);
            } finally {
                readerReadySemaphore.release();
            }
        }
    }

    public void shutdown() {
        closeConnection(this.socketConnection);
    }

    private static void closeConnection(SocketConnection socketConnection) {
        socketConnection.inputReaderThread.shutdown();
        Util.closeQuietly(socketConnection.writer);
        Util.closeQuietly(socketConnection.telnetSocket);
    }

    private record SocketConnection(Socket telnetSocket, BufferedWriter writer, InputReaderThread inputReaderThread) {
    }
}
