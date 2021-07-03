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
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.DatagramChannel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.discovery.DiscoverResult;
import org.openhab.core.thing.Bridge;
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
public class UDPListenDiscoverRunnable implements Runnable {

    @Nullable
    protected BufferedReader in = null;
    protected boolean bExit = false;
    @Nullable
    UDPDecoder decoder = null;
    // @Nullable
    // DiscoverResult discoverResult = null;

    private final Logger logger = LoggerFactory.getLogger(UDPListenDiscoverRunnable.class);

    public UDPListenDiscoverRunnable(Bridge bridge, @Nullable DiscoverResult pDiscoverResult) {
        decoder = new UDPDecoder(bridge, pDiscoverResult);
    }

    @Override
    public void run() {
        DatagramSocket socket = null;

        while (true) {
            try {
                // open socket for listening...
                var channel = DatagramChannel.open();
                socket = channel.socket();

                socket.setReuseAddress(true);
                socket.setBroadcast(true);

                var sa = new InetSocketAddress(230);
                socket.bind(sa);

                byte[] buf = new byte[200];
                // receive request
                final var packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(60000);
                socket.receive(packet);
                buf = packet.getData();

                // **************** DECODER ********************
                logger.debug("Packet received (port {}) {}", socket.getLocalPort(), macacoToString(buf));

                if (decoder != null) {
                    decoder.decodeVNetDatagram(packet);
                }

            } catch (BindException e) {
                logger.error("UDP Port busy, Souliss already listening? {} ", e.getLocalizedMessage());
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception e1) {
                    logger.error("UDP socket close failed: {} ", e1.getLocalizedMessage());
                }
            } catch (SocketTimeoutException e2) {
                logger.warn("UDP SocketTimeoutException close: {}", e2.getLocalizedMessage());
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ee) {
                logger.error("Exception receiving-decoding message: {} ", ee.getLocalizedMessage());
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }
    }

    private String macacoToString(byte[] frame) {
        var sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }
}
