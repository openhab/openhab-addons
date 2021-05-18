/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.discovery.SoulissDiscoverJob.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide receive packet from network
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 * @author Alessandro Del Pex - Souliss App
 */
@NonNullByDefault
public class SoulissBindingUDPServerJob implements Runnable {

    @Nullable
    protected BufferedReader in = null;
    protected boolean bExit = false;
    @Nullable
    SoulissBindingUDPDecoder decoder = null;
    @Nullable
    DiscoverResult discoverResult = null;

    @Nullable
    DatagramSocket soulissDatagramSocket;
    private final Logger logger = LoggerFactory.getLogger(SoulissBindingUDPServerJob.class);

    public SoulissBindingUDPServerJob(@Nullable DatagramSocket datagramSocket,
            @Nullable DiscoverResult pDiscoverResult) {
        super();
        this.discoverResult = pDiscoverResult;
        this.soulissDatagramSocket = datagramSocket;
        init(datagramSocket, pDiscoverResult);
    }

    private void init(@Nullable DatagramSocket datagramSocket, @Nullable DiscoverResult pDiscoverResult) {
        decoder = new SoulissBindingUDPDecoder(discoverResult);
        @Nullable
        DatagramSocket localSoulissDatagramSocket = this.soulissDatagramSocket;
        if (localSoulissDatagramSocket != null) {
            int localPort = localSoulissDatagramSocket.getLocalPort();
            logger.debug("Starting UDP Server Job - Server on port {}", localPort);
        }
    }

    @Override
    public void run() {
        @Nullable
        DatagramSocket localDatagramSocket = this.soulissDatagramSocket;
        if (localDatagramSocket != null) {
            if (!localDatagramSocket.isClosed()) {
                try {
                    byte[] buf = new byte[256];
                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    localDatagramSocket.receive(packet);
                    buf = packet.getData();

                    // **************** DECODER ********************
                    logger.debug("Packet received (port {}) {}", localDatagramSocket.getLocalPort(),
                            macacoToString(buf));
                    if (this.decoder != null) {
                        decoder.decodeVNetDatagram(packet);
                    }

                } catch (IOException e) {
                    logger.warn("Error in Class SoulissBindingUDPServerThread: {}", e.getMessage());
                }
            }
        } else {
            logger.warn("Socket Closed - Cannot receive data");
        }
    }

    private String macacoToString(byte[] frame) {
        StringBuilder sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }
}
