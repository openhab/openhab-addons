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
package org.openhab.binding.tapocontrol.internal.discovery;

import static org.openhab.binding.tapocontrol.internal.helpers.TapoEncoder.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.ByteUtils.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.JsonUtils.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode;
import org.openhab.binding.tapocontrol.internal.devices.bridge.TapoBridgeHandler;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResult;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;
import org.openhab.binding.tapocontrol.internal.helpers.TapoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home device UDP-connections.
 * THIS IS FOR TESTING
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoUdpDiscovery {
    private final Logger logger = LoggerFactory.getLogger(TapoUdpDiscovery.class);
    private final TapoBridgeHandler bridge;
    private String uid;

    private static final Integer BROADCAST_TIMEOUT_MS = 8000;
    private static final int[] BROADCAST_DISCOVERY_PORTS = { 9999, 20002 };
    private static final int TDP_CHECKSUM_DEFAULT = 1516993677;
    private static final int TDP_RANDOM_BOUND = 268435456;
    private static final int BUFFER_SIZE = 2048;
    private static final int RSA_KEYSIZE = 2048;
    private static final String RSA_MESSAGE_KEY = "rsa_key";
    private static final String RSA_MESSAGE_START_BYTES = "0200000100001100";

    public TapoUdpDiscovery(TapoBridgeHandler bridge) {
        this.bridge = bridge;
        uid = bridge.getUID().getAsString() + " - updDiscovery";
    }

    /***********************************
     * SCAN HANDLING
     ************************************/

    /**
     * start scan with configured broadcast address
     */
    protected void startScan() throws TapoErrorHandler {
        String broadcastAddress = bridge.getBridgeConfig().broadcastAddress;
        try {
            scanAllPorts(InetAddress.getByName(broadcastAddress));
        } catch (UnknownHostException e) {
            logger.debug("({}) unknown host exception {}", uid, broadcastAddress);
            throw new TapoErrorHandler(TapoErrorCode.ERR_CONFIG_IP, "unknown broadcast address");
        }
    }

    /**
     * Scan all available interfaces and ports
     */
    protected void scanAllInterfaces() throws TapoErrorHandler {
        List<InetAddress> broadcastList = listAllBroadcastAddresses();
        for (InetAddress inetAddress : broadcastList) {
            scanAllPorts(inetAddress);
        }
    }

    /**
     * Scan all defined ports of an specific broadcastAddress
     * 
     * @param broadcastAddress
     */
    protected void scanAllPorts(InetAddress broadcastAddress) throws TapoErrorHandler {
        for (int port : BROADCAST_DISCOVERY_PORTS) {
            udpScan(getBroadcastMessage(port), broadcastAddress, port);
        }
    }

    /**
     * 
     * @param sendData
     * @param broadcastAddress
     * @param discoveryPort
     * @throws TapoErrorHandler
     */
    private void udpScan(byte[] message, InetAddress broadcastAddress, int discoveryPort) throws TapoErrorHandler {
        logger.trace("({}) startUdpScan with address: '{}' to port {}", uid, broadcastAddress, discoveryPort);
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(BROADCAST_TIMEOUT_MS);
            udpSocket.setBroadcast(true);

            logger.trace("({}) send broadcast ('{}' byte) packet '{}'", uid, message.length, byteArrayToHex(message));

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, broadcastAddress, discoveryPort);

            udpSocket.send(sendPacket);

            while (!udpSocket.isClosed()) {
                // Wait for a response
                byte[] recvBuf = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket;
                try {
                    receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    udpSocket.receive(receivePacket);
                    handleDiscoveryResult(receivePacket);
                } catch (SocketTimeoutException e) {
                    logger.trace("({}) socketTimeOutException", uid);
                    udpSocket.close();
                }
            }
            udpSocket.close();
        } catch (Exception e) {
            logger.debug("({}) scan failed: '{}'", uid, e.getMessage());
            throw new TapoErrorHandler(e);
        }
    }

    /***********************************
     * PRIVATE HELPERS
     ************************************/

    /*
     * List all Broadcast-Addresses from all Interfaces
     */
    private List<InetAddress> listAllBroadcastAddresses() throws TapoErrorHandler {
        try {
            List<InetAddress> broadcastList = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                @Nullable
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
                        .forEach(broadcastList::add);
            }
            return broadcastList;
        } catch (Exception e) {
            logger.debug("({}) socket exception", uid);
            throw new TapoErrorHandler(e);
        }
    }

    /**
     * 
     * @param port
     * @return
     */
    private byte[] getBroadcastMessage(int port) throws TapoErrorHandler {
        String message;
        byte[] messageBytes = {};
        try {
            switch (port) {
                case 9999:
                    // message = "'system': {'get_sysinfo': None}"; // unencrypted message
                    message = "d0f281f88bff9af7d5ef94b6d1b4c09fec95e68fe187e8caf08bf68ba785e688cba7c8bdd9fbc1ba98ff9aeeb1d8b6d0bf9da7dca1dcf0d2a1ccaddfabc7aec8ad83ea85f1dfbcd3bed3bcd2fc9ff39ce98daf95eeccabcebae58ce284ebc9f388f588a486f598f98bff93fa9cf9d7b4d5b896ff8fec8de085f796b8dbb7d8adc9ebd1aa88ef8afea1c8a6c0af8db7ccb1ccb1";
                    messageBytes = hexStringToByteArray(message);
                    break;
                case 20002:
                    messageBytes = buildRsaPacket();
                    break;
                case 20004:
                    messageBytes = buildRsaPacket();
                    break;
            }
            return messageBytes;
        } catch (Exception e) {
            throw new TapoErrorHandler(e);
        }
    }

    /*
     * build discoverypacket with rsa key-message
     */
    private byte[] buildRsaPacket() throws TapoErrorHandler {
        String message;

        /* build jsonObject with rsa key */
        JsonObject parameters = new JsonObject();
        JsonObject messageObject = new JsonObject();

        parameters.addProperty(RSA_MESSAGE_KEY, new TapoKeyPair(RSA_KEYSIZE).getPublicKey());
        messageObject.add("params", parameters);
        message = messageObject.toString();
        logger.trace("({}) discovery-message: '{}'", uid, message);

        return buildByteMessage(message);
    }

    /*
     * build message prefix dynamic insert bytes that change based on the string, randomness and crc
     * algorithm found by reverse-engineering android app in com\tplink\tdp\common\b.java
     */
    private byte[] buildByteMessage(String message) throws TapoErrorHandler {
        byte[] replace;
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] fullPacket = new byte[messageBytes.length + 16];

        /* put startvalues and message */
        System.arraycopy(hexStringToByteArray(RSA_MESSAGE_START_BYTES), 0, fullPacket, 0, 8);
        System.arraycopy(messageBytes, 0, fullPacket, 16, messageBytes.length);

        /* replace with message lenght */
        replace = shortToByteArray((short) messageBytes.length, ByteOrder.BIG_ENDIAN);
        System.arraycopy(replace, 0, fullPacket, 4, 2);

        /* replace with a random array */
        replace = intToByteArray(new Random().nextInt(TDP_RANDOM_BOUND) + 0, ByteOrder.BIG_ENDIAN);
        System.arraycopy(replace, 0, fullPacket, 8, 4);

        /* replace with checksum default */
        replace = intToByteArray(TDP_CHECKSUM_DEFAULT, ByteOrder.BIG_ENDIAN);
        System.arraycopy(replace, 0, fullPacket, 12, 4);

        /* replace crc */
        replace = intToByteArray((int) crc32Checksum(fullPacket), ByteOrder.BIG_ENDIAN);
        System.arraycopy(replace, 0, fullPacket, 12, 4);

        return fullPacket;
    }

    /**
     * Handle discoveryresult and add to resultList
     * 
     * @param receivePacket
     */
    private void handleDiscoveryResult(DatagramPacket receivePacket) {
        String responseMessage = new String(receivePacket.getData(), StandardCharsets.UTF_8);
        logger.trace("({}) received responseMessage: '{}'", uid, responseMessage);
        responseMessage = responseMessage.substring(responseMessage.indexOf("{")).trim();
        TapoResponse tapoResponse = getObjectFromJson(responseMessage, TapoResponse.class);
        bridge.getDiscoveryService().addScanResult(getObjectFromJson(tapoResponse.result(), TapoDiscoveryResult.class));
    }
}
