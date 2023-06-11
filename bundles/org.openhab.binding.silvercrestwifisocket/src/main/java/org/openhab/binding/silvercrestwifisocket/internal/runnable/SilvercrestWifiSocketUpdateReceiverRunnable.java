/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.silvercrestwifisocket.internal.runnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.openhab.binding.silvercrestwifisocket.internal.exceptions.NotOneResponsePacketException;
import org.openhab.binding.silvercrestwifisocket.internal.exceptions.PacketIntegrityErrorException;
import org.openhab.binding.silvercrestwifisocket.internal.handler.SilvercrestWifiSocketMediator;
import org.openhab.binding.silvercrestwifisocket.internal.utils.WifiSocketPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Thread is responsible to receive all Wifi Socket messages and redirect them to
 * {@link SilvercrestWifiSocketMediator}.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class SilvercrestWifiSocketUpdateReceiverRunnable implements Runnable {

    private static final int TIMEOUT_TO_DATAGRAM_RECEPTION = 10000;

    private final Logger logger = LoggerFactory.getLogger(SilvercrestWifiSocketUpdateReceiverRunnable.class);

    private DatagramSocket datagramSocket;
    private final SilvercrestWifiSocketMediator mediator;
    private final WifiSocketPacketConverter packetConverter = new WifiSocketPacketConverter();

    private boolean shutdown;
    private int listeningPort;

    /**
     * Constructor of the receiver runnable thread.
     *
     * @param mediator the {@link SilvercrestWifiSocketMediator}
     * @param listeningPort the listening UDP port
     * @throws SocketException is some problem occurs opening the socket.
     */
    public SilvercrestWifiSocketUpdateReceiverRunnable(final SilvercrestWifiSocketMediator mediator,
            final int listeningPort) throws SocketException {
        logger.debug("Starting Update Receiver Runnable...");
        // Create a socket to listen on the port.
        this.listeningPort = listeningPort;
        this.mediator = mediator;

        logger.debug("Opening socket and start listening UDP port: {}", listeningPort);
        this.datagramSocket = new DatagramSocket(listeningPort);
        this.datagramSocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION);
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
                this.datagramSocket.receive(packet);

                logger.debug("Received packet from: {}. Will process the packet...",
                        packet.getAddress().getHostAddress());

                // Do mediator something with it
                this.mediator.processReceivedPacket(this.packetConverter.decryptResponsePacket(packet));

                logger.debug("Message delivered with success to mediator.");
            } catch (SocketTimeoutException e) {
                logger.trace("Socket Timeout receiving packet.");
            } catch (IOException e) {
                logger.debug("One exception has occurred: {} ", e.getMessage());
            } catch (PacketIntegrityErrorException e) {
                logger.debug("Packet has one integrity error: {}", e.getMessage());
            } catch (NotOneResponsePacketException e) {
                logger.debug(
                        "The message received is not one response. Probably the message received is one broadcast message looking for the socket.");
            }
        }

        // close the socket
        if (datagramSocket != null) {
            datagramSocket.close();
        }
    }

    private void datagramSocketHealthRoutine() {
        if (datagramSocket == null || datagramSocket.isClosed()) {
            logger.debug("Datagram Socket has been closed, will reconnect again...");
            DatagramSocket newDatagramSocket = null;
            try {
                newDatagramSocket = new DatagramSocket(listeningPort);
                newDatagramSocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION);
                datagramSocket = newDatagramSocket;
                logger.debug("Datagram Socket reconnected.");
            } catch (SocketException exception) {
                logger.error("Problem creating one new socket on port {}. Error: {}", listeningPort,
                        exception.getLocalizedMessage());
            }
        }
    }

    /**
     * Gracefully shutdown thread. Worst case takes TIMEOUT_TO_DATAGRAM_RECEPTION to shutdown.
     */
    public void shutdown() {
        this.shutdown = true;
    }
}
