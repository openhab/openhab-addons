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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emotiva.internal.dto.EmotivaControlDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaSubscriptionRequest;
import org.openhab.binding.emotiva.internal.dto.EmotivaUnsubscribeDTO;
import org.openhab.binding.emotiva.internal.dto.EmotivaUpdateRequest;
import org.openhab.binding.emotiva.internal.protocol.EmotivaControlCommands;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;
import org.openhab.binding.emotiva.internal.protocol.EmotivaUdpResponse;
import org.openhab.binding.emotiva.internal.protocol.EmotivaXmlUtils;
import org.openhab.core.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the actual communication to Emotiva devices.
 *
 * @author Patrick Koenemann - Initial contribution
 * @author Espen Fossen - Adapted to Emotiva binding
 */
@NonNullByDefault
public class EmotivaUdpConnector {

    private final Logger logger = LoggerFactory.getLogger(EmotivaUdpConnector.class);

    /**
     * Buffer for incoming UDP packages.
     */
    private static final int MAX_PACKET_SIZE = 10240;

    /**
     * The device IP this connector is listening to / sends to.
     */
    private final String ipAddress;

    /**
     * The port this connector is listening to.
     */
    private final int receivingNotifyPort;

    /**
     * The port this connector is sending to.
     */
    private final int sendingControlPort;

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
     * Socket for receiving UDP packages.
     */
    private @Nullable DatagramSocket receivingSocket = null;
    /**
     * Socket for sending UDP packages.
     */
    private @Nullable DatagramSocket sendingSocket = null;

    /**
     * The listener that gets notified upon newly received messages.
     */
    private @Nullable Consumer<EmotivaUdpResponse> listener;

    private final EmotivaXmlUtils emotivaXmlUtils;

    private int receiveFailures = 0;
    private boolean listenerActive = false;

    /**
     * Create a new connector to an Emotiva device via the given configuration.
     *
     * @param config Emotiva configuration values
     */
    public EmotivaUdpConnector(EmotivaConfiguration config, ExecutorService executorService) throws JAXBException {
        if (config.notifyPort <= 0) {
            throw new IllegalArgumentException("Invalid udpReceivingNotifyPort: " + config.notifyPort);
        }
        if (config.controlPort <= 0) {
            throw new IllegalArgumentException("Invalid udpSendingControlPort: " + config.controlPort);
        }
        if (config.ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing ipAddress.");
        }
        this.ipAddress = config.ipAddress;
        this.receivingNotifyPort = config.notifyPort;
        this.sendingControlPort = config.controlPort;
        this.executorService = executorService;
        this.emotivaXmlUtils = new EmotivaXmlUtils();
    }

    /**
     * Initialize socket connection to the UDP receive port for the given listener.
     *
     * @throws SocketException Is only thrown if <code>logNotThrowException = false</code>.
     * @throws InterruptedException Typically happens during shutdown.
     */
    public void connect(Consumer<EmotivaUdpResponse> listener, boolean logNotThrowExcpetion)
            throws SocketException, InterruptedException {
        if (receivingSocket == null) {
            try {
                receivingSocket = new DatagramSocket(receivingNotifyPort);
                sendingSocket = new DatagramSocket(sendingControlPort);

                this.listener = listener;

                listeningThreadFactory.newThread(this::listen).start();

                // wait for the listening thread to be active
                for (int i = 0; i < 20 && !listenerActive; i++) {
                    Thread.sleep(100); // wait at most 20 * 100ms = 2sec for the listener to be active
                }
                if (!listenerActive) {
                    logger.warn(
                            "Listener thread started but listener is not yet active after 2sec; something seems to be wrong with the JVM thread handling?!");
                }
            } catch (SocketException e) {
                if (logNotThrowExcpetion) {
                    logger.warn("Failed to open socket connection on port {}", receivingNotifyPort, e);
                }

                disconnect();

                if (!logNotThrowExcpetion) {
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
        logger.info("Emotiva listener started for: '{}:{}'", ipAddress, receivingNotifyPort);

        final Consumer<EmotivaUdpResponse> localListener = listener;
        final DatagramSocket localReceivingSocket = receivingSocket;
        while (localListener != null && localReceivingSocket != null && receivingSocket != null) {
            try {
                final DatagramPacket answer = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

                listenerActive = true;
                localReceivingSocket.receive(answer); // receive packet (blocking call)
                listenerActive = false;

                final byte[] receivedData = Arrays.copyOfRange(answer.getData(), 0, answer.getLength() - 1);

                if (receivedData.length == 0) {
                    if (isConnected()) {
                        logger.debug("Nothing received, this may happen during shutdown or some unknown error");
                    }
                    continue;
                }
                receiveFailures = 0; // message successfully received, unset failure counter

                handleReceivedData(answer, receivedData, localListener);
            } catch (Exception e) {
                listenerActive = false;

                if (receivingSocket == null) {
                    logger.info("Socket closed; stopping listener on port {}.", receivingNotifyPort);
                } else {
                    logger.info("Checkin receiveFailures count {}", receiveFailures);
                    // if we get 3 errors in a row, we should better add a delay to stop spamming the log!
                    if (receiveFailures++ > EmotivaBindingConstants.CONNECTION_RETRIES) {
                        logger.info(
                                "Unexpected error while listening on port {}; waiting 10sec before the next attempt to listen on that port.",
                                receivingNotifyPort, e);
                        for (int i = 0; i < 50 && receivingSocket != null; i++) {
                            Thread.sleep(200); // 50 * 200ms = 10sec
                        }
                    } else {
                        logger.info("Unexpected error while listening on port {}", receivingNotifyPort, e);
                    }
                }
            }
        }
    }

    private void handleReceivedData(DatagramPacket answer, byte[] receivedData,
            Consumer<EmotivaUdpResponse> localListener) {
        // log & notify listener in new thread (so that listener loop continues immediately)
        executorService.execute(() -> {
            if (answer.getAddress() != null && answer.getLength() > 0) {
                logger.debug("Received data on port {}: {}", receivingNotifyPort, receivedData);
                EmotivaUdpResponse emotivaUdpResponse = new EmotivaUdpResponse(
                        new String(answer.getData(), 0, answer.getLength()), answer.getAddress().getHostAddress());
                localListener.accept(emotivaUdpResponse);
            }
        });
    }

    /**
     * Close the socket connection.
     */
    public void disconnect() {
        logger.info("Emotiva listener stopped for: '{}:{}'", ipAddress, receivingNotifyPort);
        listener = null;
        final DatagramSocket localReceivingSocket = receivingSocket;
        if (localReceivingSocket != null) {
            receivingSocket = null;
            if (!localReceivingSocket.isClosed()) {
                localReceivingSocket.close(); // this interrupts and terminates the listening thread
            }
        }
        final DatagramSocket localSendingSocket = sendingSocket;
        if (localSendingSocket != null) {
            synchronized (this) {
                if (Objects.equals(sendingSocket, localSendingSocket)) {
                    sendingSocket = null;
                    if (!localSendingSocket.isClosed()) {
                        localSendingSocket.close();
                    }
                }
            }
        }
    }

    public void send(EmotivaControlDTO dto) throws IOException {
        send(emotivaXmlUtils.marshallJAXBElementObjects(dto));
    }

    public void sendSubscription(EmotivaSubscriptionTags[] tags, EmotivaConfiguration config) throws IOException {
        send(emotivaXmlUtils.marshallJAXBElementObjects(new EmotivaSubscriptionRequest(tags, config.protocolVersion)));
    }

    public void sendUpdate(EmotivaControlCommands defaultCommand, EmotivaConfiguration config) throws IOException {
        send(emotivaXmlUtils
                .marshallJAXBElementObjects(new EmotivaUpdateRequest(defaultCommand, config.protocolVersion)));
    }

    public void sendUpdate(EmotivaSubscriptionTags[] tags, EmotivaConfiguration config) throws IOException {
        send(emotivaXmlUtils.marshallJAXBElementObjects(new EmotivaUpdateRequest(tags, config.protocolVersion)));
    }

    public void sendSubscription(EmotivaControlCommands defaultCommand, EmotivaConfiguration config)
            throws IOException {
        send(emotivaXmlUtils
                .marshallJAXBElementObjects(new EmotivaSubscriptionRequest(defaultCommand, config.protocolVersion)));
    }

    public void sendUnsubscribe(EmotivaSubscriptionTags[] defaultCommand) throws IOException {
        send(emotivaXmlUtils.marshallJAXBElementObjects(new EmotivaUnsubscribeDTO(defaultCommand)));
    }

    public void send(String msg) throws IOException {
        logger.info("Sending message '{}' to {}:{}", msg, ipAddress, sendingControlPort);
        if (msg.isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        final InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
        byte[] buf = msg.getBytes(Charset.defaultCharset());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ipAddress, sendingControlPort);

        // make sure we are not interrupted by a disconnect while sending this message
        synchronized (this) {
            DatagramSocket localDatagramSocket = this.sendingSocket;
            final DatagramPacket answer = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
            final Consumer<EmotivaUdpResponse> localListener = listener;
            if (localDatagramSocket != null && !localDatagramSocket.isClosed()) {
                localDatagramSocket.send(packet);
                logger.info("Sending successful.");

                localDatagramSocket.receive(answer);
                final byte[] receivedData = Arrays.copyOfRange(answer.getData(), 0, answer.getLength() - 1);

                if (receivedData.length == 0) {
                    if (isConnected()) {
                        logger.debug("Nothing received, this may happen during shutdown or some unknown error");
                    }
                }

                if (localListener != null) {
                    handleReceivedData(answer, receivedData, localListener);
                }

            } else {
                throw new SocketException("Datagram Socket closed or not initialized");
            }
        }
    }

    public boolean isConnected() {
        return receivingSocket != null;
    }
}
