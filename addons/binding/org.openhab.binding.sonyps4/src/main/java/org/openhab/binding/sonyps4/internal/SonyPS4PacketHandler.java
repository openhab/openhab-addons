/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyPS4PacketHandler} is responsible for creating and parsing
 * packets to / from the PS4.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
public class SonyPS4PacketHandler {

    private static final String VERSION = "1.1";
    private static final String DDP_VERSION = "00020020";
    private static final int REQ_VERSION = 0x20000;

    // PS4 Commands
    private static final int HELLO_REQ = 0x6f636370;
    private static final int BYEBYE_REQ = 0x04;
    private static final int LOGIN_RSP = 0x07;
    private static final int APP_START_REQ = 0x0a;
    private static final int APP_START_RSP = 0x0b;
    private static final int OSK_START_REQ = 0x0c;
    private static final int OSK_CHANGE_STRING_REQ = 0x0e;
    private static final int OSK_CONTROL_REQ = 0x10;
    private static final int STATUS_REQ = 0x14;
    private static final int STANDBY_REQ = 0x1a;
    private static final int STANDBY_RSP = 0x1b;
    private static final int REMOTE_CONTROL_REQ = 0x1c;
    private static final int LOGIN_REQ = 0x1e;
    private static final int HANDSHAKE_REQ = 0x20;
    private static final int APP_START2_REQ = 0x24;

    private final Logger logger = LoggerFactory.getLogger(SonyPS4PacketHandler.class);

    // packet.put(VERSION.getBytes());

    private ByteBuffer newPacketOfSize(int size) {
        ByteBuffer packet = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size);
        return packet;
    }

    public byte[] makeSearchPacket() {
        StringBuilder packet = new StringBuilder("SRCH * HTTP/1.1\n");
        packet.append("device-discovery-protocol-version:" + DDP_VERSION + "\n");
        return packet.toString().getBytes();
    }

    public byte[] makeWakeupPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("WAKEUP * HTTP/1.1\n");
        packet.append("client-type:i\n");
        packet.append("auth-type:C\n");
        packet.append("user-credential:" + userCredential + "\n");
        packet.append("device-discovery-protocol-version:" + DDP_VERSION + "\n");
        return packet.toString().getBytes();
    }

    public byte[] makeHelloPacket() {
        ByteBuffer packet = newPacketOfSize(28);
        packet.putInt(HELLO_REQ);
        packet.putInt(REQ_VERSION);
        packet.put(new byte[16]); // Seed = 16 bytes

        String message = new String(packet.array(), StandardCharsets.UTF_8);
        logger.debug("Hello packet. {}", message);
        return packet.array();
    }

    public byte[] makeByebyePacket() {
        ByteBuffer packet = newPacketOfSize(8);
        packet.putInt(BYEBYE_REQ);
        return packet.array();
    }

    public byte[] makeHandshakePacket(byte[] key) {
        if (key.length != 256) {
            return new byte[0];
        }
        ByteBuffer packet = newPacketOfSize(256 + 24);
        packet.putInt(HANDSHAKE_REQ);
        packet.put(key);
        packet.put(new byte[16]); // Seed = 16 bytes
        return packet.array();
    }

    public byte[] makeLoginPacket() {
        ByteBuffer packet = newPacketOfSize(256 + 64 + 64);
        packet.putInt(LOGIN_REQ);
        packet.putInt(1234); // PIN Code
        packet.putInt(5678); // Magic number
        packet.put(new byte[64]); // account_id
        packet.put(new byte[256]); // app_label
        packet.put(new byte[16]); // os_version
        packet.put(new byte[16]); // model
        packet.put(new byte[16]); // pass_code
        return packet.array();
    }

    public byte[] makeStatusPacket(int status) {
        ByteBuffer packet = newPacketOfSize(12);
        packet.putInt(STATUS_REQ);
        packet.putInt(status); // status
        return packet.array();
    }

    public byte[] makeStandbyPacket() {
        ByteBuffer packet = newPacketOfSize(12);
        packet.putInt(STANDBY_REQ);
        packet.putLong(0); // Padding
        return packet.array();
    }

}
