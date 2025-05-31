/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.senseenergy.internal.api;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyDatagramGetRealtime;
import org.openhab.binding.senseenergy.internal.api.dto.SenseEnergyDatagramGetSysInfo;
import org.openhab.binding.senseenergy.utils.TpLinkEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * @link { SenseEnergyDatagram } implements the datagram which receives and responds to local requests
 *       from the sense monitor device querying realtime energy usage. Upon receiving a request for
 *       energy usage, it will send request to the @link { packetListener } who is responsible for
 *       calling @link { sendReponse } in order to respond.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyDatagram {

    private final Logger logger = LoggerFactory.getLogger(SenseEnergyDatagram.class);
    private static final int BUFFERSIZE = 1024;

    private @Nullable DatagramSocket datagramSocket;
    private @Nullable SenseEnergyDatagramListener packetListener;
    private volatile boolean connected = false;
    private Gson gson = new Gson();
    @Nullable
    private Thread udpListener;

    int port;
    String readerThreadName = "";

    public SenseEnergyDatagram(SenseEnergyDatagramListener packetListener) {
        this.packetListener = packetListener;
    }

    public synchronized void start(final int port, final String readerThreadName) throws IOException {
        if (this.connected) {
            return;
        }

        this.port = port;
        this.readerThreadName = readerThreadName;

        datagramSocket = new DatagramSocket(port);

        Thread localUdpListener = new Thread(new UDPListener());
        localUdpListener.setName(readerThreadName);
        localUdpListener.setDaemon(true);
        localUdpListener.start();
        udpListener = localUdpListener;

        this.connected = true;
    }

    public void stop() {
        logger.debug("datagram stop");
        Thread localUdpThread = udpListener;
        if (localUdpThread != null) {
            connected = false;
            localUdpThread.interrupt();
            udpListener = null;
        }
    }

    public void restart() throws IOException {
        stop();
        start(port, readerThreadName);
    }

    public boolean isRunning() {
        Thread localUDPListener = udpListener;
        return datagramSocket != null && connected && localUDPListener != null && localUDPListener.isAlive();
    }

    private class UDPListener implements Runnable {
        /*
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            long nextPacketTime = 0;
            logger.debug("Starting SenseEnergy Proxy Device Packet Receiver");

            DatagramSocket localSocket = datagramSocket;

            if (localSocket == null) {
                logger.error("UDPListener cannot start: Datagram socket is null.");
                return;
            }

            DatagramPacket packet = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);

            try {
                while (connected && !localSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        localSocket.receive(packet);
                    } catch (ClosedByInterruptException e) {
                        logger.debug("ClosedByInterruptExcepetion");
                        throw e;
                    } catch (IOException e) {
                        logger.debug("Exception during packet read - {}", e.getMessage());
                        Thread.sleep(100); // allow CPU to breath
                        continue;
                    }

                    // don't receive more than 1 request a second. Necessary to filter out receiving the same
                    // broadcast request packet on multiple interfaces (i.e. wi-fi and wired) at the same time
                    if (System.nanoTime() < nextPacketTime) {
                        continue;
                    }

                    JsonObject jsonResponse;
                    String decryptedPacket = new String(TpLinkEncryption.decrypt(packet.getData(), packet.getLength()));
                    try {
                        jsonResponse = JsonParser.parseString(decryptedPacket).getAsJsonObject();
                    } catch (JsonSyntaxException jsonSyntaxException) {
                        logger.trace("Invalid JSON received");
                        continue;
                    }

                    nextPacketTime = System.nanoTime() + 1000000000L;
                    if (jsonResponse.has("system") && jsonResponse.has("emeter")) {
                        SenseEnergyDatagramListener localPacketListener = packetListener;
                        if (localPacketListener != null) {
                            localPacketListener.requestReceived(packet.getSocketAddress());
                        }
                    }
                }
            } catch (InterruptedException | ClosedByInterruptException e) {
                Thread.currentThread().interrupt();
            } finally {
                localSocket.close();
                datagramSocket = null;
                connected = false;
            }
        }
    }

    public void sendResponse(SocketAddress socketAddress, SenseEnergyDatagramGetSysInfo getSysInfo,
            SenseEnergyDatagramGetRealtime getRealtime) throws IOException {
        String jsonResponse = String.format("{\"emeter\":{\"get_realtime\":%s},\"system\":{\"get_sysinfo\":%s}}",
                gson.toJson(getRealtime), gson.toJson(getSysInfo));

        byte[] encrypted = TpLinkEncryption.encrypt(jsonResponse);

        DatagramSocket localDatagramSocket = datagramSocket;
        if (localDatagramSocket == null) {
            logger.warn("sendResponse(): Datagram socket is null, cannot send response");
            return;
        }
        localDatagramSocket.send(new DatagramPacket(encrypted, encrypted.length, socketAddress));
    }
}
