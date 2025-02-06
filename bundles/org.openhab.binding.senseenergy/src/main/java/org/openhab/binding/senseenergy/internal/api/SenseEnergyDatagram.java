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
 * @link { SenseEnergyDatagram }
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class SenseEnergyDatagram {

    private final Logger logger = LoggerFactory.getLogger(SenseEnergyDatagram.class);
    static private final int BUFFERSIZE = 1024;

    private @Nullable DatagramSocket datagramSocket;
    private @Nullable SenseEnergyDatagramListener packetListener;
    private boolean connected = false;
    private Gson gson = new Gson();
    @Nullable
    private Thread udpListener;

    int port;
    String readerThreadName = "";

    public SenseEnergyDatagram(SenseEnergyDatagramListener packetListener) {
        this.packetListener = packetListener;
    }

    public synchronized void start(final int port, final String readerThreadName) throws IOException {
        if (this.connected == true) {
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
        connected = false;

        try {
            DatagramSocket localSocket = datagramSocket;
            if (localSocket != null) {
                localSocket.close();
                datagramSocket = null;
                logger.debug("Closing datagram listener");
            }
        } catch (Exception exception) {
            logger.debug("closeConnection(): Error closing connection - {}", exception.getMessage());
        }
    }

    public void restart() throws IOException {
        stop();
        start(port, readerThreadName);
    }

    public boolean isRunning() {
        return (datagramSocket == null) ? false : connected && udpListener != null && udpListener.isAlive();
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
                throw new IllegalStateException("Cannot access socket.");
            }

            DatagramPacket packet = new DatagramPacket(new byte[BUFFERSIZE], BUFFERSIZE);

            while (connected) {
                try {
                    localSocket.receive(packet);
                } catch (IOException exception) {
                    logger.debug("Exception during packet read - {}", exception.getMessage());
                    break;
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
                    continue;
                }

                nextPacketTime = System.nanoTime() + 1000000000L;
                if (jsonResponse.has("system") && jsonResponse.has("emeter")) {
                    logger.trace("Packet received - {} - {}", packet.getSocketAddress(), jsonResponse);

                    SenseEnergyDatagramListener localPacketListener = packetListener;
                    if (localPacketListener != null) {
                        localPacketListener.requestReceived(packet.getSocketAddress());
                    }
                }
            }
        }
    }

    public void sendResponse(SocketAddress socketAddress, SenseEnergyDatagramGetSysInfo getSysInfo,
            SenseEnergyDatagramGetRealtime getRealtime) throws IOException {

        String jsonResponse = "{\"emeter\":{\"get_realtime\":"
                + gson.toJson(getRealtime, SenseEnergyDatagramGetRealtime.class) + "},\"system\":{\"get_sysinfo\":"
                + gson.toJson(getSysInfo, SenseEnergyDatagramGetSysInfo.class) + "}}";

        byte[] encrypted = TpLinkEncryption.encrypt(jsonResponse);

        DatagramSocket localDatagramSocket = datagramSocket;
        if (localDatagramSocket != null) {
            localDatagramSocket.send(new DatagramPacket(encrypted, encrypted.length, socketAddress));
        }
    }

    public void readMessage(byte[] message) {
        SenseEnergyDatagramListener listener = this.packetListener;

        if (listener != null && message.length != 0) {
            listener.messageReceived(message);
        }
    }
}

/* @formatter:off
 * "echo" response on linux platforms that must filtered out
{ ...
  "type": "realtime_update"
}
 * @formatter:on
 */
