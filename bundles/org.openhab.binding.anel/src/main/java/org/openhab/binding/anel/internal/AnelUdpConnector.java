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
package org.openhab.binding.anel.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the actual communication to ANEL devices.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelUdpConnector {

    /** Buffer for incoming UDP packages. */
    private static final int MAX_PACKET_SIZE = 512;

    private final Logger logger = LoggerFactory.getLogger(AnelUdpConnector.class);

    /** The device IP this connector is listening to / sends to. */
    private final String host;

    /** The port this connector is listening to. */
    private final int receivePort;

    /** The port this connector is sending to. */
    private final int sendPort;

    /** Service to spawn new threads for handling status updates. */
    private final ExecutorService executorService;

    /** Thread factory for UDP listening thread. */
    private final NamedThreadFactory listeningThreadFactory = new NamedThreadFactory(IAnelConstants.BINDING_ID, true);

    /** Socket for receiving UDP packages. */
    private @Nullable DatagramSocket receivingSocket = null;
    /** Socket for sending UDP packages. */
    private @Nullable DatagramSocket sendingSocket = null;

    /** The listener that gets notified upon newly received messages. */
    private @Nullable Consumer<String> listener;

    private int receiveFailures = 0;
    private boolean listenerActive = false;

    /**
     * Create a new connector to an Anel device via the given host and UDP
     * ports.
     *
     * @param host
     *            The IP address / network name of the device.
     * @param udpReceivePort
     *            The UDP port to listen for packages.
     * @param udpSendPort
     *            The UDP port to send packages.
     */
    public AnelUdpConnector(String host, int udpReceivePort, int udpSendPort, ExecutorService executorService) {
        if (udpReceivePort <= 0) {
            throw new IllegalArgumentException("Invalid udpReceivePort: " + udpReceivePort);
        }
        if (udpSendPort <= 0) {
            throw new IllegalArgumentException("Invalid udpSendPort: " + udpSendPort);
        }
        if (host.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing host.");
        }
        this.host = host;
        this.receivePort = udpReceivePort;
        this.sendPort = udpSendPort;
        this.executorService = executorService;
    }

    /**
     * Initialize socket connection to the UDP receive port for the given listener.
     *
     * @throws SocketException Is only thrown if <code>logNotTHrowException = false</code>.
     * @throws InterruptedException Typically happens during shutdown.
     */
    public void connect(Consumer<String> listener, boolean logNotThrowExcpetion)
            throws SocketException, InterruptedException {
        if (receivingSocket == null) {
            try {
                receivingSocket = new DatagramSocket(receivePort);
                sendingSocket = new DatagramSocket();
                this.listener = listener;

                /*-
                 * Due to the issue with 4 concurrently listening threads [1], we should follow Kais suggestion [2]
                 * to create our own listening daemonized thread.
                 *
                 * [1] https://community.openhab.org/t/anel-net-pwrctrl-binding-for-oh3/123378
                 * [2] https://www.eclipse.org/forums/index.php/m/1775932/?#msg_1775429
                 */
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
                    logger.warn(
                            "Failed to open socket connection on port {} (maybe there is already another socket listener on that port?)",
                            receivePort, e);
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
        logger.info("Anel NET-PwrCtrl listener started for: '{}:{}'", host, receivePort);

        final Consumer<String> listener2 = listener;
        final DatagramSocket socket2 = receivingSocket;
        while (listener2 != null && socket2 != null && receivingSocket != null) {
            try {
                final DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

                listenerActive = true;
                socket2.receive(packet); // receive packet (blocking call)
                listenerActive = false;

                final byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength() - 1);

                if (data == null || data.length == 0) {
                    if (isConnected()) {
                        logger.debug("Nothing received, this may happen during shutdown or some unknown error");
                    }
                    continue;
                }
                receiveFailures = 0; // message successfully received, unset failure counter

                /* useful for debugging without logger (e.g. in AnelUdpConnectorTest): */
                // System.out.println(String.format("%s [%s] received: %s", getClass().getSimpleName(),
                // new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()), new String(data).trim()));

                // log & notify listener in new thread (so that listener loop continues immediately)
                executorService.execute(() -> {
                    final String message = new String(data);

                    logger.debug("Received data on port {}: {}", receivePort, message);

                    listener2.accept(message);
                });
            } catch (Exception e) {
                listenerActive = false;

                if (receivingSocket == null) {
                    logger.debug("Socket closed; stopping listener on port {}.", receivePort);
                } else {
                    // if we get 3 errors in a row, we should better add a delay to stop spamming the log!
                    if (receiveFailures++ > IAnelConstants.ATTEMPTS_WITH_COMMUNICATION_ERRORS) {
                        logger.debug(
                                "Unexpected error while listening on port {}; waiting 10sec before the next attempt to listen on that port.",
                                receivePort, e);
                        for (int i = 0; i < 50 && receivingSocket != null; i++) {
                            Thread.sleep(200); // 50 * 200ms = 10sec
                        }
                    } else {
                        logger.warn("Unexpected error while listening on port {}", receivePort, e);
                    }
                }
            }
        }
    }

    /** Close the socket connection. */
    public void disconnect() {
        logger.debug("Anel NET-PwrCtrl listener stopped for: '{}:{}'", host, receivePort);
        listener = null;
        final DatagramSocket receivingSocket2 = receivingSocket;
        if (receivingSocket2 != null) {
            receivingSocket = null;
            if (!receivingSocket2.isClosed()) {
                receivingSocket2.close(); // this interrupts and terminates the listening thread
            }
        }
        final DatagramSocket sendingSocket2 = sendingSocket;
        if (sendingSocket2 != null) {
            synchronized (this) {
                if (Objects.equals(sendingSocket, sendingSocket2)) {
                    sendingSocket = null;
                    if (!sendingSocket2.isClosed()) {
                        sendingSocket2.close();
                    }
                }
            }
        }
    }

    public void send(String msg) throws IOException {
        logger.debug("Sending message '{}' to {}:{}", msg, host, sendPort);
        if (msg.isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        final InetAddress ipAddress = InetAddress.getByName(host);
        final byte[] bytes = msg.getBytes();
        final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ipAddress, sendPort);

        // make sure we are not interrupted by a disconnect while sending this message
        synchronized (this) {
            final DatagramSocket sendingSocket2 = sendingSocket;
            if (sendingSocket2 != null) {
                sendingSocket2.send(packet);

                /* useful for debugging without logger (e.g. in AnelUdpConnectorTest): */
                // System.out.println(String.format("%s [%s] sent: %s", getClass().getSimpleName(),
                // new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()), msg));

                logger.debug("Sending successful.");
            }
        }
    }

    public boolean isConnected() {
        return receivingSocket != null;
    }
}
