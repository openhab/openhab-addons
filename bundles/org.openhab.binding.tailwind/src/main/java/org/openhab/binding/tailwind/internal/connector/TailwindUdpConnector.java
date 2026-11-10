/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.connector;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.tailwind.internal.TailwindConfiguration;
import org.openhab.binding.tailwind.internal.dto.TailwindControllerData;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TailwindUDPConnector} is a UDP server that runs in the background to
 * receive status updates when the TailWind controller sends them.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
public class TailwindUdpConnector {

    /** Buffer for incoming UDP packages. */
    private static final int MAX_PACKET_SIZE = 1024;
    private final Logger logger = LoggerFactory.getLogger(TailwindUdpConnector.class);
    /** Service to spawn new threads for handling status updates. */
    private final ExecutorService executorService;
    /** Thread factory for UDP listening thread. */
    private final NamedThreadFactory listeningThreadFactory = new NamedThreadFactory(TAILWIND_UDP_CONNECTOR_THREAD_NAME,
            true);
    /** Socket for receiving UDP packages. */
    private @Nullable DatagramSocket receivingSocket = null;
    /** Socket for sending UDP packages. */
    private @Nullable DatagramSocket sendingSocket = null;
    /** The listener that gets notified upon newly received messages. */
    private @Nullable Consumer<String> listener;
    private String threadNamePrefix = "";
    private int receiveFailures = 0;
    private boolean listenerActive = false;
    private TailwindConnectApi tailwindApi;
    private TailwindControllerData response = new TailwindControllerData();
    private Thing thing;
    private TailwindConfiguration config;

    /**
     * Constructor for TailwindUdpConnector
     *
     * @param thing
     * @param config
     * @param executorService
     * @param tailwindApi
     * @throws UnknownHostException
     */
    public TailwindUdpConnector(Thing thing, TailwindConfiguration config, ExecutorService executorService,
            TailwindConnectApi tailwindApi) throws UnknownHostException {
        this.tailwindApi = tailwindApi;
        this.executorService = executorService;
        this.thing = thing;
        this.config = config;
    }

    /**
     * Initialize socket connection to the UDP receive port for the given listener.
     *
     * @throws SocketException Is only thrown if <code>logNotTHrowException = false</code>.
     * @throws InterruptedException Typically happens during shutdown.
     */
    public void connect(Consumer<String> listener, boolean logNotThrowException, String threadName)
            throws SocketException, InterruptedException {
        this.threadNamePrefix = threadName;
        if (receivingSocket == null) {
            Boolean connected = false;
            Integer udpPort = TAILWIND_OPENHAB_HOST_UDP_PORT;
            Integer udpPortLimit = udpPort + 3;
            while (!connected && udpPort <= udpPortLimit) {
                try { // try ports starting with receivePort to receivePort + 3
                    receivingSocket = new DatagramSocket(udpPort);
                    sendingSocket = new DatagramSocket();
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
                    connected = true;
                } catch (SocketException e) {
                    if (udpPort > udpPort + 3) {
                        if (logNotThrowException) {
                            logger.warn(
                                    "Failed to open socket connection on port {} (maybe there is already another socket listener on that port?)",
                                    udpPort, e);
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed connection on port: {}, trying next port: {}", udpPort, udpPort + 1);
                    }
                    // Increment the port, previous one was already in use
                    udpPort += 1;
                    if (!logNotThrowException) {
                        throw e;
                    }
                }
            } // While connected = false

        } else if (!Objects.equals(this.listener, listener)) {
            throw new IllegalStateException("A listening thread is already running");
        }
    } // End connect(...)

    private void listen() {
        try {
            listenUnhandledInterruption();
        } catch (InterruptedException e) {
            // OH shutdown - don't log anything, just quit
        }
    } // End listen()

    private void listenUnhandledInterruption() throws InterruptedException {
        Integer recPortLocal = 0;
        if (receivingSocket != null) {
            recPortLocal = receivingSocket.getLocalPort();
        }
        // Set thread name with the port being monitored.
        Thread.currentThread().setName(threadNamePrefix.concat(recPortLocal.toString()));
        // Let the TailWind controller know where (URL) to send UDP status updates http://host:recPortLocal/report
        sendCommand(buildSetStatusReportCommand(config.getOpenHabHostAddress(), recPortLocal.toString()));
        // Document UDP port being used
        if (logger.isDebugEnabled()) {
            logger.debug("TailWind UPD listener started for: '{}:{}, thread: {}'", config.getOpenHabHostAddress(),
                    recPortLocal, Thread.currentThread().getName());
        } else {
            logger.info("TailWind binding listening on UDP port: {}:{}", config.getOpenHabHostAddress(), recPortLocal);
        }
        // Listen for status updates from the TailWind controller and pass them along to the thing handler
        final Consumer<String> listener2 = listener;
        final DatagramSocket socket2 = receivingSocket;
        // Integer udpPort = socket2 != null ? socket2.getLocalPort() : 0;
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
                // log & notify listener in new thread (so that listener loop continues immediately)
                executorService.execute(() -> {
                    final String message = new String(data, StandardCharsets.UTF_8);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received data on port {}: {}", socket2.getLocalPort(), message);
                    }
                    listener2.accept(message);
                });
            } catch (Exception e) {
                listenerActive = false;

                if (receivingSocket == null) {
                    logger.debug("Socket closed; stopping listener on port {}.", recPortLocal);
                } else {
                    // if we get 3 errors in a row, we should better add a delay to stop spamming the log!
                    if (receiveFailures++ > ATTEMPTS_WITH_COMMUNICATION_ERRORS) {
                        logger.debug(
                                "Unexpected error while listening on port {}; waiting 10sec before the next attempt to listen on that port. Error: {}",
                                recPortLocal, e.getMessage());
                        for (int i = 0; i < 50 && receivingSocket != null; i++) {
                            Thread.sleep(200); // 50 * 200ms = 10sec
                        }
                    } else {
                        logger.warn("Unexpected error while listening on port {}", recPortLocal, e);
                    }
                }
            }
        }
    } // End listenUnhandledInterruption()

    /** Close the socket connection. */
    public void disconnect() {
        listener = null;
        final DatagramSocket receivingSocket2 = receivingSocket;
        if (receivingSocket2 != null) {
            logger.debug("Tailwind UDP listener stopped for: '{}:{}'", config.getOpenHabHostAddress(),
                    receivingSocket2.getLocalPort());
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
    } // End disconnect()

    /**
     * @return connected status (true or false)
     */
    public boolean isConnected() {
        return receivingSocket != null;
    } // End isConnected()

    /**
     * @param command - URL to receive UDP Status messages from controller
     * @return Body string for command to send to TailWind controller
     */
    private String buildSetStatusReportCommand(String host, String port) {
        String udpUrl = host.concat(":").concat(port);
        JSONObject cmdSetStatusReport = new JSONObject(TAILWIND_CMD_SET_STATUS_REPORT);
        String cmdKeyFound = cmdSetStatusReport.getJSONObject(TAILWIND_JSON_KEY_DATA)
                .getJSONObject(TAILWIND_JSON_KEY_VALUE).getString(TAILWIND_JSON_KEY_URL);
        if (cmdKeyFound != null) {
            cmdSetStatusReport.getJSONObject(TAILWIND_JSON_KEY_DATA).getJSONObject(TAILWIND_JSON_KEY_VALUE)
                    .put(TAILWIND_JSON_KEY_URL, udpUrl);

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Command to set UDP status report URL was not formatted correctly for command: {}",
                        cmdSetStatusReport);
            } // If Debug logging
        } // If url command key was found
        return cmdSetStatusReport.toString();
    }

    /**
     * Method to send a command to the TailWind controller
     *
     * @param commandString - JSON formated command string
     * @return TailwindControllerData object response from the TailWind controller
     */
    private TailwindControllerData sendCommand(String commandString) {
        JSONObject tailwindCommandString = new JSONObject(commandString);
        String body = tailwindCommandString.toString();
        try {
            response = tailwindApi.getTailwindControllerData(thing, config.authToken, body);
        } catch (TailwindCommunicationException e) {
            // Error trying to connect to the TailWind controller possible configuration settings changes needed
            logger.warn("There was an error communicating to the TailWind controller! Error msg: {}", e.getMessage());
        } // Send command to TailWind controller

        if (!JSON_RESPONSE_RESULT_OK.equals(response.getResult())) {
            logger.warn("Set status report request failed with result: {}", response.getResult());
        } else {
            logger.debug("Command sent to TailWind controller succeeded: {}!", commandString);
        } // If command failed
        return response;
    }
}
