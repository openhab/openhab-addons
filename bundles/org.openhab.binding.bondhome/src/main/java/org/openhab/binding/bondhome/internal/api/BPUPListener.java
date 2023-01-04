/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bondhome.internal.api;

import static java.nio.charset.StandardCharsets.*;
import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.handler.BondBridgeHandler;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * This Thread is responsible maintaining the Bond Push UDP Protocol
 *
 * @author Sara Geleskie Damiano - Initial contribution
 *
 */
@NonNullByDefault
public class BPUPListener implements Runnable {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 3000;
    private static final int SOCKET_RETRY_TIMEOUT_MILLISECONDS = 3000;

    private final Logger logger = LoggerFactory.getLogger(BPUPListener.class);

    // To parse the JSON responses
    private final Gson gsonBuilder;

    // Used for callbacks to handler
    private final BondBridgeHandler bridgeHandler;

    // UDP socket used to receive status events
    private @Nullable DatagramSocket socket;

    public @Nullable String lastRequestId;
    private long timeOfLastKeepAlivePacket;
    private boolean shutdown;

    private int numberOfKeepAliveTimeouts;

    /**
     * Constructor of the receiver runnable thread.
     *
     * @param address The address of the Bond Bridge
     * @throws SocketException is some problem occurs opening the socket.
     */
    public BPUPListener(BondBridgeHandler bridgeHandler) {
        logger.debug("Starting BPUP Listener...");

        this.bridgeHandler = bridgeHandler;
        this.timeOfLastKeepAlivePacket = -1;
        this.numberOfKeepAliveTimeouts = 0;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        Gson gson = gsonBuilder.create();
        this.gsonBuilder = gson;
        this.shutdown = true;
    }

    public boolean isRunning() {
        return !shutdown;
    }

    public void start(Executor executor) {
        shutdown = false;
        executor.execute(this);
    }

    /**
     * Send keep-alive as necessary and listen for push messages
     */
    @Override
    public void run() {
        receivePackets();
    }

    /**
     * Gracefully shutdown thread. Worst case takes TIMEOUT_TO_DATAGRAM_RECEPTION to
     * shutdown.
     */
    public void shutdown() {
        this.shutdown = true;
        DatagramSocket s = this.socket;
        if (s != null) {
            s.close();
            logger.debug("Listener closed socket");
            this.socket = null;
        }
    }

    private void sendBPUPKeepAlive() {
        // Create a buffer and packet for the response
        byte[] buffer = new byte[256];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);

        DatagramSocket sock = this.socket;
        if (sock != null) {
            logger.trace("Sending keep-alive request ('\\n')");
            try {
                byte[] outBuffer = { (byte) '\n' };
                InetAddress inetAddress = InetAddress.getByName(bridgeHandler.getBridgeIpAddress());
                DatagramPacket outPacket = new DatagramPacket(outBuffer, 1, inetAddress, BOND_BPUP_PORT);
                sock.send(outPacket);
                sock.receive(inPacket);
                BPUPUpdate response = transformUpdatePacket(inPacket);
                if (response != null) {
                    @Nullable
                    String bondId = response.bondId;
                    if (bondId == null || !bondId.equalsIgnoreCase(bridgeHandler.getBridgeId())) {
                        logger.warn("Response isn't from expected Bridge!  Expected: {}  Got: {}",
                                bridgeHandler.getBridgeId(), bondId);
                    } else {
                        bridgeHandler.setBridgeOnline(inPacket.getAddress().getHostAddress());
                        numberOfKeepAliveTimeouts = 0;
                    }
                }
            } catch (SocketTimeoutException e) {
                numberOfKeepAliveTimeouts++;
                logger.trace("BPUP Socket timeout, number of timeouts: {}", numberOfKeepAliveTimeouts);
                if (numberOfKeepAliveTimeouts > 10) {
                    bridgeHandler.setBridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error.timeout");
                }
            } catch (IOException e) {
                logger.debug("One exception has occurred", e);
            }
        }
    }

    private void receivePackets() {
        try {
            DatagramSocket s = new DatagramSocket(null);
            s.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            s.bind(null);
            socket = s;
            logger.debug("Listener created UDP socket on port {} with timeout {}", s.getPort(),
                    SOCKET_TIMEOUT_MILLISECONDS);
        } catch (SocketException e) {
            logger.debug("Listener got SocketException", e);
            datagramSocketHealthRoutine();
        }

        // Create a buffer and packet for the response
        byte[] buffer = new byte[256];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);

        DatagramSocket sock = this.socket;
        while (sock != null && !this.shutdown) {
            // Check if we're due to send something to keep the connection
            long now = System.nanoTime() / 1000000L;
            long timePassedFromLastKeepAlive = now - timeOfLastKeepAlivePacket;

            if (timeOfLastKeepAlivePacket == -1 || timePassedFromLastKeepAlive >= 60000L) {
                sendBPUPKeepAlive();
                timeOfLastKeepAlivePacket = now;
            }

            try {
                sock.receive(inPacket);
                processPacket(inPacket);
            } catch (SocketTimeoutException e) {
                // Ignore. Means there was no updates while we waited.
                // We'll just loop around and try again after sending a keep alive.
            } catch (IOException e) {
                logger.debug("Listener got IOException waiting for datagram: {}", e.getMessage());
                datagramSocketHealthRoutine();
            }
        }
        logger.debug("Listener exiting");
    }

    private void processPacket(DatagramPacket packet) {
        logger.trace("Got datagram of length {} from {}", packet.getLength(), packet.getAddress().getHostAddress());

        BPUPUpdate update = transformUpdatePacket(packet);
        if (update != null) {
            if (!update.bondId.equalsIgnoreCase(bridgeHandler.getBridgeId())) {
                logger.warn("Response isn't from expected Bridge!  Expected: {}  Got: {}", bridgeHandler.getBridgeId(),
                        update.bondId);
            }

            // Check for duplicate packet
            if (isDuplicate(update)) {
                logger.trace("Dropping duplicate packet");
                return;
            }

            // Send the update the the bridge for it to pass on to the devices
            if (update.topic != null) {
                logger.trace("Forwarding message to bridge handler");
                bridgeHandler.forwardUpdateToThing(update);
            } else {
                logger.debug("No topic in incoming message!");
            }
        }
    }

    /**
     * Method that transforms {@link DatagramPacket} to a {@link BPUPUpdate} Object
     *
     * @param packet the {@link DatagramPacket}
     * @return the {@link BPUPUpdate}
     */
    public @Nullable BPUPUpdate transformUpdatePacket(final DatagramPacket packet) {
        String responseJson = new String(packet.getData(), 0, packet.getLength(), UTF_8);
        logger.debug("Message from {}:{} -> {}", packet.getAddress().getHostAddress(), packet.getPort(), responseJson);

        @Nullable
        BPUPUpdate response = null;
        try {
            response = this.gsonBuilder.fromJson(responseJson, BPUPUpdate.class);
        } catch (JsonParseException e) {
            logger.warn("Error parsing json! {}", e.getMessage());
        }
        return response;
    }

    private boolean isDuplicate(BPUPUpdate update) {
        boolean packetIsDuplicate = false;
        String newReqestId = update.requestId;
        String lastRequestId = this.lastRequestId;
        if (lastRequestId != null && newReqestId != null) {
            if (lastRequestId.equalsIgnoreCase(newReqestId)) {
                packetIsDuplicate = true;
            }
        }
        // Remember this packet for duplicate check
        lastRequestId = newReqestId;
        return packetIsDuplicate;
    }

    private void datagramSocketHealthRoutine() {
        @Nullable
        DatagramSocket datagramSocket = this.socket;
        if (datagramSocket == null || (datagramSocket.isClosed() || !datagramSocket.isConnected())) {
            logger.trace("Datagram Socket is disconnected or has been closed, reconnecting...");
            try {
                // close the socket before trying to reopen
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
                logger.trace("Old socket closed.");
                try {
                    Thread.sleep(SOCKET_RETRY_TIMEOUT_MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                DatagramSocket s = new DatagramSocket(null);
                s.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
                s.bind(null);
                this.socket = s;
                logger.trace("Datagram Socket reconnected using port {}.", s.getPort());
            } catch (SocketException exception) {
                logger.warn("Problem creating new socket : {}", exception.getLocalizedMessage());
            }
        }
    }
}
