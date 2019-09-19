/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.openhab.binding.souliss.internal.discovery.SoulissDiscoverJob.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide receive packet from network
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingUDPServerJob implements Runnable {

    // protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean bExit = false;
    SoulissBindingUDPDecoder decoder = null;
    DiscoverResult discoverResult = null;
    DatagramSocket soulissDatagramSocket;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingUDPServerJob.class);

    public SoulissBindingUDPServerJob(DatagramSocket _datagramSocket, DiscoverResult _discoverResult) {
        super();
        init(_datagramSocket, _discoverResult);
    }

    private void init(DatagramSocket _datagramSocket, DiscoverResult _discoverResult) {
        this.discoverResult = _discoverResult;
        this.soulissDatagramSocket = _datagramSocket;

        // if (discoverResult != null) {
        decoder = new SoulissBindingUDPDecoder(discoverResult);
        logger.info("Starting UDP Server Job - Server on port {}", soulissDatagramSocket.getLocalPort());
    }

    @Override
    public void run() {
        if (!soulissDatagramSocket.isClosed()) {
            try {
                byte[] buf = new byte[256];
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                soulissDatagramSocket.receive(packet);
                buf = packet.getData();

                // **************** DECODER ********************
                logger.debug("Packet received (port {}) {}", soulissDatagramSocket.getLocalPort(), MaCacoToString(buf));
                decoder.decodeVNetDatagram(packet);

            } catch (IOException e) {
                logger.debug("Error in Class SoulissBindingUDPServerThread");
                logger.error("Error: ", e);
            }
        } else {
            logger.info("Socket Closed (port {}) - Cannot receive data", soulissDatagramSocket.getLocalPort());
        }
    }

    private String MaCacoToString(byte[] frame) {
        StringBuilder sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }
}