/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.runnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wizlighting.internal.entities.WizLightingResponse;
import org.openhab.binding.wizlighting.internal.handler.WizLightingMediator;
import org.openhab.binding.wizlighting.internal.utils.WizLightingPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Thread is responsible to receive all sync messages and redirect them to
 * {@link WizLightingMediator}.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingUpdateReceiverRunnable implements Runnable {

    private static final int TIMEOUT_TO_DATAGRAM_RECEPTION = 15000; // in milliseconds

    private final Logger logger = LoggerFactory.getLogger(WizLightingUpdateReceiverRunnable.class);

    private DatagramSocket datagramSocket;
    private final WizLightingMediator mediator;
    private final WizLightingPacketConverter packetConverter = new WizLightingPacketConverter();

    private boolean shutdown;
    private int listeningPort;

    /**
     * Constructor of the receiver runnable thread.
     *
     * @param mediator the {@link WizLightingMediator}
     * @param listeningPort the listening UDP port
     * @throws SocketException is some problem occurs opening the socket.
     */
    public WizLightingUpdateReceiverRunnable(final WizLightingMediator mediator, final int listeningPort)
            throws SocketException {
        logger.debug("Starting Update Receiver Runnable...");
        this.listeningPort = listeningPort;
        this.mediator = mediator;

        // Create a socket to listen on the port.
        logger.debug("Opening socket and start listening UDP port: {}", listeningPort);
        DatagramSocket dsocket = new DatagramSocket(null);
        dsocket.setReuseAddress(true);
        dsocket.setBroadcast(true);
        dsocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION);
        dsocket.bind(new InetSocketAddress(listeningPort));
        this.datagramSocket = dsocket;
        logger.debug("Update Receiver Runnable and socket started with success...");

        this.shutdown = false;
    }

    @Override
    public void run() {
        // Now loop forever, waiting to receive packets and redirect them to mediator.
        while (!this.shutdown) {
            datagramSocketHealthRoutine();

            // Create a buffer to read datagrams into. If a
            // packet is larger than this buffer, the
            // excess will simply be discarded!
            byte[] buffer = new byte[2048];

            // Create a packet to receive data into the buffer
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // Wait to receive a datagram
            try {
                datagramSocket.receive(packet);

                logger.debug("Received packet from: {}. Will process the packet...",
                        packet.getAddress().getHostAddress());

                // Redirect packet to the mediator
                WizLightingResponse response = this.packetConverter.transformResponsePacket(packet);
                if (response != null) {
                    this.mediator.processReceivedPacket(response);
                    logger.debug("Message delivered with success to mediator.");
                } else {
                    logger.debug("No WizLightingResponse was parsed from returned packet");
                }
            } catch (SocketTimeoutException e) {
                logger.trace("Socket Timeout receiving packet.");
            } catch (IOException e) {
                logger.debug("One exception has occurred: {} ", e.getMessage());
            }
        }

        // close the socket
        logger.trace("Ending run loop; closing socket.");
        datagramSocket.close();
    }

    private void datagramSocketHealthRoutine() {
        DatagramSocket datagramSocket = this.datagramSocket;
        if (datagramSocket.isClosed() || !datagramSocket.isConnected()) {
            logger.trace("Datagram Socket is disconnected or has been closed, will reconnect again...");
            try {
                // close the socket before trying to reopen
                this.datagramSocket.close();
                logger.trace("Old socket closed.");
                DatagramSocket dsocket = new DatagramSocket(null);
                dsocket.setReuseAddress(true);
                dsocket.setBroadcast(true);
                dsocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION);
                dsocket.bind(new InetSocketAddress(listeningPort));
                this.datagramSocket = dsocket;
                logger.trace("Datagram Socket reconnected.");
            } catch (SocketException exception) {
                logger.error("Problem creating one new socket on port {}. Error: {}", listeningPort,
                        exception.getLocalizedMessage());
            }
        }
    }

    /**
     * Gracefully shutdown thread. Worst case takes TIMEOUT_TO_DATAGRAM_RECEPTION to
     * shutdown.
     */
    public void shutdown() {
        this.shutdown = true;
    }
}
