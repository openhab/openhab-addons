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
package org.openhab.binding.emotiva.internal;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.protocol.EmotivaUdpResponse;
import org.openhab.core.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service is used for receiving UDP message from Emotiva devices.
 *
 * @author Patrick Koenemann - Initial contribution
 * @author Espen Fossen - Adapted to Emotiva binding
 */
@NonNullByDefault
public class EmotivaUdpReceivingService {

    private final Logger logger = LoggerFactory.getLogger(EmotivaUdpReceivingService.class);

    /**
     * Buffer for incoming UDP packages.
     */
    private static final int MAX_PACKET_SIZE = 10240;

    /**
     * The device IP this connector is listening to / sends to.
     */
    private final String ipAddress;

    /**
     * The port this connector is listening to notify message.
     */
    private final int receivingPort;

    /**
     * Service to spawn new threads for handling status updates.
     */
    private final ExecutorService executorService;

    /**
     * Thread factory for UDP listening thread.
     */
    private final NamedThreadFactory listeningThreadFactory = new NamedThreadFactory(EmotivaBindingConstants.BINDING_ID,
            true);

    /**
     * Socket for receiving Notify UDP packages.
     */
    private @Nullable DatagramSocket receivingSocket = null;

    /**
     * The listener that gets notified upon newly received messages.
     */
    private @Nullable Consumer<EmotivaUdpResponse> listener;

    private int receiveNotifyFailures = 0;
    private boolean listenerNotifyActive = false;

    /**
     * Create a listener to an Emotiva device via the given configuration.
     *
     * @param receivingPort listening port
     * @param config Emotiva configuration values
     */
    public EmotivaUdpReceivingService(int receivingPort, EmotivaConfiguration config, ExecutorService executorService) {
        if (receivingPort <= 0) {
            throw new IllegalArgumentException("Invalid receivingPort: " + receivingPort);
        }
        if (config.ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing ipAddress");
        }
        this.ipAddress = config.ipAddress;
        this.receivingPort = receivingPort;
        this.executorService = executorService;
    }

    /**
     * Initialize socket connection to the UDP receive port for the given listener.
     *
     * @throws SocketException Is only thrown if <code>logNotThrowException = false</code>.
     * @throws InterruptedException Typically happens during shutdown.
     */
    public void connect(Consumer<EmotivaUdpResponse> listener, boolean logNotThrowException)
            throws SocketException, InterruptedException {
        if (receivingSocket == null) {
            try {
                receivingSocket = new DatagramSocket(receivingPort);

                this.listener = listener;

                listeningThreadFactory.newThread(this::listen).start();

                // wait for the listening thread to be active
                for (int i = 0; i < 20 && !listenerNotifyActive; i++) {
                    Thread.sleep(100); // wait at most 20 * 100ms = 2sec for the listener to be active
                }
                if (!listenerNotifyActive) {
                    logger.warn(
                            "Listener thread started but listener is not yet active after 2sec; something seems to be wrong with the JVM thread handling?!");
                }
            } catch (SocketException e) {
                if (logNotThrowException) {
                    logger.warn("Failed to open socket connection on port '{}'", receivingPort);
                }

                disconnect();

                if (!logNotThrowException) {
                    throw e;
                }
            }
        } else if (!Objects.equals(this.listener, listener)) {
            throw new IllegalStateException("A listening thread is already running");
        }
    }

    private void listen() {
        try {
            listenUnhandledInterruption();
        } catch (InterruptedException e) {
            // OH shutdown - don't log anything, just quit
        }
    }

    private void listenUnhandledInterruption() throws InterruptedException {
        logger.debug("Emotiva listener started for: '{}:{}'", ipAddress, receivingPort);

        final Consumer<EmotivaUdpResponse> localListener = listener;
        final DatagramSocket localReceivingSocket = receivingSocket;
        while (localListener != null && localReceivingSocket != null && receivingSocket != null) {
            try {
                final var answer = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

                listenerNotifyActive = true;
                localReceivingSocket.receive(answer); // receive packet (blocking call)
                listenerNotifyActive = false;

                if (Arrays.copyOfRange(answer.getData(), 0, answer.getLength() - 1).length == 0) {
                    if (isConnected()) {
                        logger.debug("Nothing received, this may happen during shutdown or some unknown error");
                    }
                    continue;
                }
                receiveNotifyFailures = 0; // message successfully received, unset failure counter

                handleReceivedData(answer, localListener);
            } catch (Exception e) {
                listenerNotifyActive = false;

                if (receivingSocket == null) {
                    logger.debug("Socket closed; stopping listener on port '{}'", receivingPort);
                } else {
                    logger.debug("Checkin receiveFailures count {}", receiveNotifyFailures);
                    // if we get 3 errors in a row, we should better add a delay to stop spamming the log!
                    if (receiveNotifyFailures++ > EmotivaBindingConstants.DEFAULT_CONNECTION_RETRIES) {
                        logger.debug(
                                "Unexpected error while listening on port '{}'; waiting 10sec before the next attempt to listen on that port",
                                receivingPort, e);
                        for (int i = 0; i < 50 && receivingSocket != null; i++) {
                            Thread.sleep(200); // 50 * 200ms = 10sec
                        }
                    } else {
                        logger.debug("Unexpected error while listening on port '{}'", receivingPort, e);
                    }
                }
            }
        }
    }

    private void handleReceivedData(DatagramPacket answer, Consumer<EmotivaUdpResponse> localListener) {
        // log & notify listener in new thread (so that listener loop continues immediately)
        executorService.execute(() -> {
            if (answer.getAddress() != null && answer.getLength() > 0) {
                logger.trace("Received data on port '{}'", answer.getPort());
                var emotivaUdpResponse = new EmotivaUdpResponse(new String(answer.getData(), 0, answer.getLength()),
                        answer.getAddress().getHostAddress());
                localListener.accept(emotivaUdpResponse);
            }
        });
    }

    /**
     * Close the socket connection.
     */
    public void disconnect() {
        logger.debug("Emotiva listener stopped for: '{}:{}'", ipAddress, receivingPort);
        listener = null;
        final DatagramSocket localReceivingSocket = receivingSocket;
        if (localReceivingSocket != null) {
            receivingSocket = null;
            if (!localReceivingSocket.isClosed()) {
                localReceivingSocket.close(); // this interrupts and terminates the listening thread
            }
        }
    }

    public boolean isConnected() {
        return receivingSocket != null;
    }
}
