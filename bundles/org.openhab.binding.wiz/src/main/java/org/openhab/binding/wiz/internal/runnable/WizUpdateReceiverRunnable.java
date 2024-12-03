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
package org.openhab.binding.wiz.internal.runnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wiz.internal.entities.WizResponse;
import org.openhab.binding.wiz.internal.handler.WizMediator;
import org.openhab.binding.wiz.internal.utils.WizPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Thread is responsible for receiving all sync messages and redirecting them to
 * {@link WizMediator}.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizUpdateReceiverRunnable implements Runnable {

    private static final int TIMEOUT_TO_DATAGRAM_RECEPTION_MILLISECONDS = 15000;

    private final Logger logger = LoggerFactory.getLogger(WizUpdateReceiverRunnable.class);

    private DatagramSocket datagramSocket;
    private final WizMediator mediator;
    private final WizPacketConverter packetConverter = new WizPacketConverter();

    private boolean shutdown;
    private int listeningPort;

    /**
     * Constructor of the receiver runnable thread.
     *
     * @param mediator the {@link WizMediator}
     * @param listeningPort the listening UDP port
     * @throws SocketException is some problem occurs opening the socket.
     */
    public WizUpdateReceiverRunnable(final WizMediator mediator, final int listeningPort) throws SocketException {
        this.listeningPort = listeningPort;
        this.mediator = mediator;

        // Create a socket to listen on the port.
        logger.debug("Opening socket and start listening UDP port: {}", listeningPort);
        DatagramSocket dsocket = new DatagramSocket(null);
        dsocket.setReuseAddress(true);
        dsocket.setBroadcast(true);
        dsocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION_MILLISECONDS);
        dsocket.bind(new InetSocketAddress(listeningPort));
        this.datagramSocket = dsocket;

        this.shutdown = false;
    }

    @Override
    public void run() {
        try {
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

                    // Redirect packet to the mediator
                    WizResponse response = this.packetConverter.transformResponsePacket(packet);
                    if (response != null) {
                        this.mediator.processReceivedPacket(response);
                    } else {
                        logger.debug("No WizResponse was parsed from returned packet");
                    }
                } catch (SocketTimeoutException e) {
                    logger.trace("No incoming data on port {} during {} ms socket was listening.", listeningPort,
                            TIMEOUT_TO_DATAGRAM_RECEPTION_MILLISECONDS);
                } catch (IOException e) {
                    logger.debug("One exception has occurred: {} ", e.getMessage());
                }
            }
        } finally {
            // close the socket
            datagramSocket.close();
        }
    }

    private void datagramSocketHealthRoutine() {
        DatagramSocket datagramSocket = this.datagramSocket;
        if (datagramSocket.isClosed() || !datagramSocket.isConnected()) {
            logger.trace("Datagram Socket is disconnected or has been closed (probably timed out), reconnecting...");
            try {
                // close the socket before trying to reopen
                this.datagramSocket.close();
                logger.trace("Old socket closed.");
                DatagramSocket dsocket = new DatagramSocket(null);
                dsocket.setReuseAddress(true);
                dsocket.setBroadcast(true);
                dsocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION_MILLISECONDS);
                dsocket.bind(new InetSocketAddress(listeningPort));
                this.datagramSocket = dsocket;
                logger.trace("Datagram Socket reconnected.");
            } catch (SocketException exception) {
                logger.debug("Problem creating one new socket on port {}. Error: {}", listeningPort,
                        exception.getLocalizedMessage());
            }
        }
    }

    /**
     * Gracefully shutdown thread. Worst case takes TIMEOUT_TO_DATAGRAM_RECEPTION_MILLISECONDS to
     * shutdown.
     */
    public void shutdown() {
        this.shutdown = true;
    }
}
