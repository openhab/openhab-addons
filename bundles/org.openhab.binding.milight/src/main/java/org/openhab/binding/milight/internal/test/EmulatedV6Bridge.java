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
package org.openhab.binding.milight.internal.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openhab.binding.milight.internal.MilightBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emulates a Milight V6 iBox bridge to test and intercept communication with the official apps,
 * as well as test the binding to be conformant to the protocol.
 *
 * @author David Graeff - Initial contribution
 */
public class EmulatedV6Bridge {
    protected final Logger logger = LoggerFactory.getLogger(EmulatedV6Bridge.class);
    private boolean willbeclosed = false;
    private static final byte SID1 = (byte) 0xed;
    private static final byte SID2 = (byte) 0xab;
    private static final byte PW1 = 0;
    private static final byte PW2 = 0;

    // These bytes are the client session bytes
    private byte cls1 = (byte) 0xf6;
    private byte cls2 = (byte) 0x0D;

    private static final byte SEQ1 = 0, SEQ2 = 0;

    private static final byte[] FAKE_MAC = { (byte) 0xAC, (byte) 0xCF, (byte) 0x23, (byte) 0xF5, (byte) 0x7A,
            (byte) 0xD4 };

    // Send to the network by clients to find V6 bridges
    private byte[] searchBroadcast = new byte[] { 0x10, 0, 0, 0, 0x24, 0x02, cls1, cls2, 0x02, 0x39, 0x38, 0x35, 0x62,
            0x31, 0x35, 0x37, 0x62, 0x66, 0x36, 0x66, 0x63, 0x34, 0x33, 0x33, 0x36, 0x38, 0x61, 0x36, 0x33, 0x34, 0x36,
            0x37, 0x65, 0x61, 0x33, 0x62, 0x31, 0x39, 0x64, 0x30, 0x64 };

    // Send to broadcast address by the client usually and used to test if the client with the contained bridge id
    // is present on the network. If the IP of the bridge is known already, then SESSION_REQUEST is used usually.
    private byte[] sessionRequestFindBroadcast = new byte[] { 0x10, 0, 0, 0, 0x0A, 2, cls1, cls2, 1, FAKE_MAC[0],
            FAKE_MAC[1], FAKE_MAC[2], FAKE_MAC[3], FAKE_MAC[4], FAKE_MAC[5] };

    // Some clients send this as first command to get a session id, especially if the bridge IP is already known.
    private byte[] sessionRequest = new byte[] { (byte) 0x20, 0, 0, 0, (byte) 0x16, 2, (byte) 0x62, (byte) 0x3A,
            (byte) 0xD5, (byte) 0xED, (byte) 0xA3, 1, (byte) 0xAE, (byte) 0x08, (byte) 0x2D, (byte) 0x46, (byte) 0x61,
            (byte) 0x41, (byte) 0xA7, (byte) 0xF6, (byte) 0xDC, (byte) 0xAF, cls1, cls2, 0, 0, (byte) 0x1E };

    private byte[] sessionResponse = { (byte) 0x28, 0, 0, 0, (byte) 0x11, 0, 2, (byte) 0xAC, (byte) 0xCF, (byte) 0x23,
            (byte) 0xF5, (byte) 0x7A, (byte) 0xD4, (byte) 0x69, (byte) 0xF0, (byte) 0x3C, (byte) 0x23, 0, 1, SID1, SID2,
            0 };

    // Some clients call this as second command to establish a session.
    private static final byte[] ESTABLISH_SESSION_REQUEST = new byte[] { (byte) 0x30, 0, 0, 0, 3, SID1, SID2, 0 };

    // In response to SEARCH, ESTABLISH_SESSION_REQUEST but also to SESSION_REQUEST_FIND_BROADCAST
    private static final byte[] REESTABLISH_SESSION_RESPONSE = new byte[] { (byte) 0x18, 0, 0, 0, (byte) 0x40, 2,
            FAKE_MAC[0], FAKE_MAC[1], FAKE_MAC[2], FAKE_MAC[3], FAKE_MAC[4], FAKE_MAC[5], 0, (byte) 0x20, (byte) 0x39,
            (byte) 0x38, (byte) 0x35, (byte) 0x62, (byte) 0x31, (byte) 0x35, (byte) 0x37, (byte) 0x62, (byte) 0x66,
            (byte) 0x36, (byte) 0x66, (byte) 0x63, (byte) 0x34, (byte) 0x33, (byte) 0x33, (byte) 0x36, (byte) 0x38,
            (byte) 0x61, (byte) 0x36, (byte) 0x33, (byte) 0x34, (byte) 0x36, (byte) 0x37, (byte) 0x65, (byte) 0x61,
            (byte) 0x33, (byte) 0x62, (byte) 0x31, (byte) 0x39, (byte) 0x64, (byte) 0x30, (byte) 0x64, 1, 0, 1,
            (byte) 0x17, (byte) 0x63, 0, 0, (byte) 0x05, 0, (byte) 0x09, (byte) 0x78, (byte) 0x6C, (byte) 0x69,
            (byte) 0x6E, (byte) 0x6B, (byte) 0x5F, (byte) 0x64, (byte) 0x65, (byte) 0x76, (byte) 0x07, (byte) 0x5B,
            (byte) 0xCD, (byte) 0x15 };

    private static final byte[] REGISTRATION_REQUEST = { (byte) 0x80, 0, 0, 0, 0x11, SID1, SID2, SEQ1, SEQ2, 0, 0x33,
            PW1, PW2, 0, 0, 0, 0, 0, 0, 0, 0, 0x33 };

    // 80:00:00:00:15:(f0:fe:6b:16:b0:8a):05:02:00:34:00:00:00:00:00:00:00:00:00:00:34
    private static final byte[] REGISTRATION_REQUEST_RESPONSE = { (byte) 0x80, 0, 0, 0, 0x15, FAKE_MAC[0], FAKE_MAC[1],
            FAKE_MAC[2], FAKE_MAC[3], FAKE_MAC[4], FAKE_MAC[5], 5, 2, 0, 0x34, PW1, PW2, 0, 0, 0, 0, 0, 0, 0, 0, 0x34 };

    private static final byte[] KEEP_ALIVE_REQUEST = { (byte) 0xD0, 0, 0, 0, 2, SID1, SID2 };

    private static final byte[] KEEP_ALIVE_RESPONSE = { (byte) 0xD8, 0, 0, 0, (byte) 0x07, FAKE_MAC[0], FAKE_MAC[1],
            FAKE_MAC[2], FAKE_MAC[3], FAKE_MAC[4], FAKE_MAC[5], 1 };

    EmulatedV6Bridge() {
        new Thread(this::runDiscovery, "OH-binding-" + MilightBindingConstants.BINDING_ID + "-runDiscovery").start();
        new Thread(this::runBridge, "OH-binding-" + MilightBindingConstants.BINDING_ID + "-runBridge").start();
    }

    private void replaceWithMac(byte[] data, int offset) {
        data[offset + 0] = FAKE_MAC[0];
        data[offset + 1] = FAKE_MAC[1];
        data[offset + 2] = FAKE_MAC[2];
        data[offset + 3] = FAKE_MAC[3];
        data[offset + 4] = FAKE_MAC[4];
        data[offset + 5] = FAKE_MAC[5];
    }

    public void runDiscovery() {
        final byte[] discover = "HF-A11ASSISTHREAD".getBytes();

        try {
            byte[] a = new byte[0];
            DatagramPacket sPacket = new DatagramPacket(a, a.length);
            DatagramSocket datagramSocket = new DatagramSocket(MilightBindingConstants.PORT_DISCOVER);

            debugSession("EmulatedV6Bridge discover thread ready");
            byte[] buffer = new byte[1024];
            DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);

            // Now loop forever, waiting to receive packets and printing them.
            while (!willbeclosed) {
                rPacket.setLength(buffer.length);
                datagramSocket.receive(rPacket);
                sPacket.setAddress(rPacket.getAddress());
                sPacket.setPort(rPacket.getPort());

                int len = rPacket.getLength();

                if (len >= discover.length) {
                    if (Arrays.equals(discover, Arrays.copyOf(buffer, discover.length))) {
                        String data = rPacket.getAddress().getHostAddress() + ",ACCF23F57AD4,HF-LPB100";
                        debugSession("Discover message received. Send: " + data);
                        sendMessage(sPacket, datagramSocket, data.getBytes());
                        continue;
                    }
                }
                // logUnknownPacket(buffer, len, "No valid discovery received");
            }
        } catch (IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.error("{}", e.getLocalizedMessage());
        }
    }

    public void runBridge() {
        try {
            byte[] a = new byte[0];
            DatagramPacket sPacket = new DatagramPacket(a, a.length);
            DatagramSocket datagramSocket = new DatagramSocket(MilightBindingConstants.PORT_VER6);

            debugSession("EmulatedV6Bridge control thread ready");
            byte[] buffer = new byte[1024];
            DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);

            // Now loop forever, waiting to receive packets and printing them.
            while (!willbeclosed) {
                rPacket.setLength(buffer.length);
                datagramSocket.receive(rPacket);

                sPacket.setAddress(rPacket.getAddress());
                sPacket.setPort(rPacket.getPort());

                int len = rPacket.getLength();

                if (len < 5 || buffer[1] != 0 || buffer[2] != 0 || buffer[3] != 0) {
                    logUnknownPacket(buffer, len, "Not an iBox request!");
                    continue;
                }

                if (len >= sessionRequest.length && buffer[0] == sessionRequest[0]) {
                    sessionRequest[22] = buffer[22];
                    sessionRequest[23] = buffer[23];
                    boolean eq = ByteBuffer.wrap(sessionRequest, 0, sessionRequest.length)
                            .equals(ByteBuffer.wrap(buffer, 0, sessionRequest.length));
                    if (eq) {
                        debugSession("session get message received");
                        sessionResponse[19] = SID1;
                        sessionResponse[20] = SID2;
                        replaceWithMac(sessionResponse, 7);
                        debugSessionSend(sessionResponse, sPacket.getAddress());
                        sendMessage(sPacket, datagramSocket, sessionResponse);
                        continue;
                    }
                }

                if (len >= sessionRequestFindBroadcast.length && buffer[0] == sessionRequestFindBroadcast[0]) {
                    sessionRequestFindBroadcast[6] = buffer[6];
                    sessionRequestFindBroadcast[7] = buffer[7];
                    boolean eq = ByteBuffer.wrap(sessionRequestFindBroadcast, 0, 6)
                            .equals(ByteBuffer.wrap(buffer, 0, 6));
                    if (eq) {
                        debugSession("init session received");
                        cls1 = sessionRequestFindBroadcast[6];
                        cls2 = sessionRequestFindBroadcast[7];
                        replaceWithMac(REESTABLISH_SESSION_RESPONSE, 6);
                        debugSessionSend(REESTABLISH_SESSION_RESPONSE, sPacket.getAddress());
                        sendMessage(sPacket, datagramSocket, REESTABLISH_SESSION_RESPONSE);
                        continue;
                    }
                }

                if (len >= searchBroadcast.length && buffer[0] == searchBroadcast[0]) {
                    searchBroadcast[6] = buffer[6];
                    searchBroadcast[7] = buffer[7];
                    boolean eq = ByteBuffer.wrap(searchBroadcast, 0, searchBroadcast.length)
                            .equals(ByteBuffer.wrap(buffer, 0, searchBroadcast.length));
                    if (eq) {
                        debugSession("Search request");
                        cls1 = searchBroadcast[6];
                        cls2 = searchBroadcast[7];
                        replaceWithMac(REESTABLISH_SESSION_RESPONSE, 6);
                        debugSessionSend(REESTABLISH_SESSION_RESPONSE, sPacket.getAddress());
                        sendMessage(sPacket, datagramSocket, REESTABLISH_SESSION_RESPONSE);
                        continue;
                    }
                }

                if (len >= ESTABLISH_SESSION_REQUEST.length && buffer[0] == ESTABLISH_SESSION_REQUEST[0]) {
                    ESTABLISH_SESSION_REQUEST[5] = SID1;
                    ESTABLISH_SESSION_REQUEST[6] = SID2;
                    boolean eq = ByteBuffer.wrap(ESTABLISH_SESSION_REQUEST, 0, ESTABLISH_SESSION_REQUEST.length)
                            .equals(ByteBuffer.wrap(buffer, 0, ESTABLISH_SESSION_REQUEST.length));
                    if (eq) {
                        debugSession("Session establish request");
                        replaceWithMac(REESTABLISH_SESSION_RESPONSE, 6);
                        debugSessionSend(REESTABLISH_SESSION_RESPONSE, sPacket.getAddress());
                        sendMessage(sPacket, datagramSocket, REESTABLISH_SESSION_RESPONSE);
                        continue;
                    }
                }

                if (len >= KEEP_ALIVE_REQUEST.length && buffer[0] == KEEP_ALIVE_REQUEST[0]) {
                    KEEP_ALIVE_REQUEST[5] = SID1;
                    KEEP_ALIVE_REQUEST[6] = SID2;
                    boolean eq = ByteBuffer.wrap(KEEP_ALIVE_REQUEST, 0, KEEP_ALIVE_REQUEST.length)
                            .equals(ByteBuffer.wrap(buffer, 0, KEEP_ALIVE_REQUEST.length));
                    if (eq) {
                        debugSession("keep alive received");
                        replaceWithMac(KEEP_ALIVE_RESPONSE, 5);
                        debugSessionSend(KEEP_ALIVE_RESPONSE, sPacket.getAddress());
                        sendMessage(sPacket, datagramSocket, KEEP_ALIVE_RESPONSE);
                        continue;
                    }
                }

                if (len >= REGISTRATION_REQUEST.length && buffer[0] == REGISTRATION_REQUEST[0]) {
                    byte seq = buffer[8];
                    if (buffer[5] != SID1 || buffer[6] != SID2) {
                        logUnknownPacket(buffer, len,
                                "No valid ssid. Current ssid is " + String.format("%02X %02X", SID1, SID2));
                        continue;
                    }
                    if (buffer[11] != PW1 || buffer[12] != PW2) {
                        logUnknownPacket(buffer, len,
                                "No valid password. Current password is " + String.format("%02X %02X", PW1, PW2));
                        continue;
                    }

                    if (buffer[4] == 0x11) {
                        if (buffer[10] == 0x33) {
                            replaceWithMac(REGISTRATION_REQUEST_RESPONSE, 5);
                            sendMessage(sPacket, datagramSocket, REGISTRATION_REQUEST_RESPONSE);
                        } else if (buffer[10] == 0x31) {
                            // 80 00 00 00 11 {WifiBridgeSessionID1} {WifiBridgeSessionID2} 00 {SequenceNumber} 00 0x31
                            // {PasswordByte1 default 00} {PasswordByte2 default 00} {remoteStyle 08 for RGBW/WW/CW or
                            // 00 for bridge lamp} {LightCommandByte1} {LightCommandByte2} 0 0 0 {Zone1-4 0=All} 0
                            // {Checksum}

                            byte chksum = (byte) (buffer[10 + 0] + buffer[10 + 1] + buffer[10 + 2] + buffer[10 + 3]
                                    + buffer[10 + 4] + buffer[10 + 5] + buffer[10 + 6] + buffer[10 + 7] + buffer[10 + 8]
                                    + buffer[19]);

                            if (chksum != buffer[21]) {
                                logger.error("Checksum wrong:{} {}", chksum, buffer[21]);
                                continue;
                            }

                            StringBuilder debugStr = new StringBuilder();
                            if (buffer[13] == 0x08) {
                                debugStr.append("RGBWW ");
                            } else if (buffer[13] == 0x07) {
                                debugStr.append("RGBW ");
                            } else {
                                debugStr.append("iBox ");
                            }

                            debugStr.append("Zone " + buffer[19] + " ");

                            for (int i = 13; i < 19; ++i) {
                                debugStr.append(String.format("%02X ", buffer[i]));
                            }
                            logger.debug("{}", debugStr);
                        }
                    }

                    byte[] response = { (byte) 0x88, 0, 0, 0, (byte) 0x03, 0, seq, 0 };
                    sendMessage(sPacket, datagramSocket, response);
                    continue;
                }

                logUnknownPacket(buffer, len, "Not recognised command");
            }
        } catch (

        IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.error("{}", e.getLocalizedMessage());
        }
    }

    protected void logUnknownPacket(byte[] data, int len, String reason) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            s.append(String.format("%02X ", data[i]));
        }
        logger.error("{}: {}", reason, s);
    }

    protected void sendMessage(DatagramPacket packet, DatagramSocket datagramSocket, byte[] buffer) {
        packet.setData(buffer);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            logger.error("Failed to send Message to '{}', Error message: {}",
                    new Object[] { packet.getAddress().getHostAddress(), e.getMessage() });
        }
    }

    private void debugSessionSend(byte[] buffer, InetAddress address) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < buffer.length; ++i) {
            s.append(String.format("%02X ", buffer[i]));
        }
        // logger.debug("Sent packet '{}' to ({})", new Object[] { s.toString(), address.getHostAddress() });
    }

    private void debugSession(String msg) {
        // logger.debug(msg);
    }
}
