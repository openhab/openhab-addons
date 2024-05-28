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

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.DEFAULT_UDP_SENDING_TIMEOUT;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service handles sending UDP message to Emotiva devices.
 *
 * @author Patrick Koenemann - Initial contribution
 * @author Espen Fossen - Adapted to Emotiva binding
 */
@NonNullByDefault
public class EmotivaUdpSendingService {

    private final Logger logger = LoggerFactory.getLogger(EmotivaUdpSendingService.class);

    /**
     * Buffer for incoming UDP packages.
     */
    private static final int MAX_PACKET_SIZE = 10240;

    /**
     * The device IP this connector is listening to / sends to.
     */
    private final String ipAddress;

    /**
     * The port this connector is sending to.
     */
    private final int sendingControlPort;

    /**
     * Service to spawn new threads for handling status updates.
     */
    private final ExecutorService executorService;

    /**
     * Socket for sending UDP packages.
     */
    private @Nullable DatagramSocket sendingSocket = null;

    /**
     * Sending response listener.
     */
    private @Nullable Consumer<EmotivaUdpResponse> listener;

    private final EmotivaXmlUtils emotivaXmlUtils;

    /**
     * Create a socket for sending message to Emotiva device via the given configuration.
     *
     * @param config Emotiva configuration values
     */
    public EmotivaUdpSendingService(EmotivaConfiguration config, ExecutorService executorService) throws JAXBException {
        if (config.controlPort <= 0) {
            throw new IllegalArgumentException("Invalid udpSendingControlPort: " + config.controlPort);
        }
        if (config.ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing ipAddress");
        }
        this.ipAddress = config.ipAddress;
        this.sendingControlPort = config.controlPort;
        this.executorService = executorService;
        this.emotivaXmlUtils = new EmotivaXmlUtils();
    }

    /**
     * Initialize socket connection to the UDP sending port
     *
     * @throws SocketException Is only thrown if <code>logNotThrowException = false</code>.
     * @throws InterruptedException Typically happens during shutdown.
     */
    public void connect(Consumer<EmotivaUdpResponse> listener, boolean logNotThrowException)
            throws SocketException, InterruptedException {
        try {
            sendingSocket = new DatagramSocket(sendingControlPort);

            this.listener = listener;
        } catch (SocketException e) {
            disconnect();

            if (!logNotThrowException) {
                throw e;
            }
        }
    }

    private void handleReceivedData(DatagramPacket answer, byte[] receivedData,
            Consumer<EmotivaUdpResponse> localListener) {
        // log & notify listener in new thread (so that listener loop continues immediately)
        executorService.execute(() -> {
            if (answer.getAddress() != null && answer.getLength() > 0) {
                logger.trace("Received data on port '{}': {}", answer.getPort(), receivedData);
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
        logger.debug("Emotiva sender stopped for '{}'", ipAddress);
        listener = null;
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

    public void sendUnsubscribe(EmotivaSubscriptionTags[] defaultCommand) throws IOException {
        send(emotivaXmlUtils.marshallJAXBElementObjects(new EmotivaUnsubscribeDTO(defaultCommand)));
    }

    public void send(String msg) throws IOException {
        logger.trace("Sending message '{}' to {}:{}", msg, ipAddress, sendingControlPort);
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
                localDatagramSocket.setSoTimeout(DEFAULT_UDP_SENDING_TIMEOUT);
                localDatagramSocket.send(packet);
                logger.debug("Sending successful");

                localDatagramSocket.receive(answer);
                final byte[] receivedData = Arrays.copyOfRange(answer.getData(), 0, answer.getLength() - 1);

                if (receivedData.length == 0) {
                    logger.debug("Nothing received, this may happen during shutdown or some unknown error");
                }

                if (localListener != null) {
                    handleReceivedData(answer, receivedData, localListener);
                }
            } else {
                throw new SocketException("Datagram Socket closed or not initialized");
            }
        }
    }
}
