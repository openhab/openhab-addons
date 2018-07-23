/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.openhab.binding.souliss.internal.discovery.SoulissDiscoverThread.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide receive packet from network
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingUDPServerThread extends Thread {

    // protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean bExit = false;
    SoulissBindingUDPDecoder decoder = null;
    DiscoverResult discoverResult = null;
    DatagramSocket soulissDatagramSocket;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingUDPServerThread.class);

    // public SoulissBindingUDPServerThread(SoulissTypicals typicals, DiscoverResult discoverResultLoc) {
    // super();
    //
    // soulissDatagramSocket = SoulissDatagramSocketFactory.getDatagram_for_broadcast();
    //
    // discoverResult = discoverResultLoc;
    // decoder = new SoulissBindingUDPDecoder(discoverResult);
    // logger.info("Start UDPServerThread - Server in ascolto sulla porta "
    // + SoulissDatagramSocketFactory.getDatagram_for_broadcast().getLocalPort());
    // }

    public SoulissBindingUDPServerThread(DatagramSocket _datagramSocket, DiscoverResult _discoverResult) {
        super();
        init(_datagramSocket, _discoverResult);
    }

    private void init(DatagramSocket _datagramSocket, DiscoverResult _discoverResult) {
        this.discoverResult = _discoverResult;
        this.soulissDatagramSocket = _datagramSocket;

        // if (discoverResult != null) {
        decoder = new SoulissBindingUDPDecoder(discoverResult);
        logger.info("Starting UDP Server Thread - Server on port {}", soulissDatagramSocket.getLocalPort());
    }

    @Override
    public void run() {

        while (!bExit) {
            if (!soulissDatagramSocket.isClosed()) {
                try {
                    byte[] buf = new byte[256];
                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    soulissDatagramSocket.receive(packet);
                    buf = packet.getData();

                    // **************** DECODER ********************
                    logger.debug("Packet received (port {}) {}", soulissDatagramSocket.getLocalPort(),
                            MaCacoToString(buf));
                    decoder.decodeVNetDatagram(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                    logger.debug("Error in Class SoulissBindingUDPServerThread");
                    logger.error(e.getMessage());
                }
            } else {
                logger.debug("Socket Closed");
                bExit = true;
            }
        }

        logger.debug("Exit from server cicle");
        if (bExit) {
            soulissDatagramSocket.close();
            logger.debug("UDP Server (port {}) stopped", soulissDatagramSocket.getLocalPort());
        }
    }

    public void stopServer() {
        bExit = true;
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