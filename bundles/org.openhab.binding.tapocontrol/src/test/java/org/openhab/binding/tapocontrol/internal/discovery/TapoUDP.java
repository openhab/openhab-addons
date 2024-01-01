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
package org.openhab.binding.tapocontrol.internal.api;

import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Handler class for TAPO Smart Home device UDP-connections.
 * THIS IS FOR TESTING
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoUDP {
    private final Logger logger = LoggerFactory.getLogger(TapoUDP.class);
    private static final Integer BROADCAST_TIMEOUT_MS = 5000;
    private static final Integer BROADCAST_DISCOVERY_PORT = 20002; // int
    private static final String BROADCAST_IP = "255.255.255.255";
    private static final String DISCOVERY_MESSAGE_KEY = "rsa_key";
    private static final String DISCOVERY_MESSAGE_START_BYTES = "0200000101e5110001cb8c577dd7deb8";
    private static final Integer BUFFER_SIZE = 501;
    private TapoCredentials credentials;

    public TapoUDP(TapoCredentials credentials) {
        this.credentials = credentials; // new TapoCredentials();
    }

    public JsonArray udpScan() {
        try {
            DatagramSocket udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(BROADCAST_TIMEOUT_MS);
            udpSocket.setBroadcast(true);

            /* create payload for handshake */
            String publicKey = credentials.getPublicKey();
            publicKey = generateOwnRSAKey(); // credentials.getPublicKey();
            JsonObject parameters = new JsonObject();
            JsonObject messageObject = new JsonObject();
            parameters.addProperty(DISCOVERY_MESSAGE_KEY, publicKey);
            messageObject.add("params", parameters);

            String discoveryMessage = messageObject.toString();

            byte[] startByte = hexStringToByteArray(DISCOVERY_MESSAGE_START_BYTES);
            byte[] message = discoveryMessage.getBytes("UTF-8");
            byte[] sendData = new byte[startByte.length + message.length];
            System.arraycopy(startByte, 0, sendData, 0, startByte.length);
            System.arraycopy(message, 0, sendData, startByte.length, message.length);

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName(BROADCAST_IP), BROADCAST_DISCOVERY_PORT);

            udpSocket.send(sendPacket);

            while (true) {
                // Wait for a response
                byte[] recvBuf = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket;
                try {
                    receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    udpSocket.receive(receivePacket);
                } catch (SocketTimeoutException e) {
                    udpSocket.close();
                    return new JsonArray();
                } catch (Exception e) {
                    udpSocket.close();
                    return new JsonArray();
                }

                // Check if the message is correct
                String responseMessage = new String(receivePacket.getData(), "UTF-8").trim();

                if (responseMessage.length() == 0) {
                    udpSocket.close();
                }
                String addressBC = receivePacket.getAddress().getHostAddress();
                gotDeviceAdress(addressBC);
            }
        } catch (Exception e) {
            // handle exception
        }
        return new JsonArray();
    }

    private void gotDeviceAdress(String ipAddress) {
        // handle exception
    }

    private String generateOwnRSAKey() {
        try {
            logger.trace("generating new keypair");
            KeyPairGenerator instance = KeyPairGenerator.getInstance("RSA");
            instance.initialize(1536, new SecureRandom());
            KeyPair generateKeyPair = instance.generateKeyPair();

            String publicKey = new String(java.util.Base64.getMimeEncoder()
                    .encode(((RSAPublicKey) generateKeyPair.getPublic()).getEncoded()));
            String privateKey = new String(java.util.Base64.getMimeEncoder()
                    .encode(((RSAPrivateKey) generateKeyPair.getPrivate()).getEncoded()));
            logger.trace("new privateKey: '{}'", privateKey);
            logger.trace("new ublicKey: '{}'", publicKey);

            return String.format("-----BEGIN PUBLIC KEY-----%n%s%n-----END PUBLIC KEY-----%n", publicKey);

        } catch (Exception e) {
            // couldn't generate own rsa key
            return "";
        }
    }
}
