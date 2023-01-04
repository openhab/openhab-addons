/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.playstation.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PS4PacketHandler} is responsible for creating and parsing
 * packets to / from the PS4.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class PS4PacketHandler {

    private static final String APPLICATION_NAME = "openHAB PlayStation 4 Binding";
    private static final String DEVICE_NAME = "openHAB Server";

    private static final String OS_VERSION = "8.1.0";
    private static final String DDP_VERSION = "device-discovery-protocol-version:00020020\n";
    static final int REQ_VERSION = 0x20000;

    private PS4PacketHandler() {
        // Don't instantiate
    }

    /**
     * Allocates a new ByteBuffer of exactly size.
     *
     * @param size The size of the packet.
     * @param cmd The command to add to the packet.
     * @return A ByteBuffer of exactly size number of bytes.
     */
    static ByteBuffer newPacketOfSize(int size, PS4Command cmd) {
        ByteBuffer packet = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size).putInt(cmd.value);
        return packet;
    }

    /**
     * Allocates a new ByteBuffer of size aligned to be a multiple of 16 bytes.
     *
     * @param size The size of the data in the packet.
     * @param cmd The command to add to the packet.
     * @return A ByteBuffer aligned to 16 byte size.
     */
    private static ByteBuffer newPacketForEncryption(int size, PS4Command cmd) {
        int realSize = (((size + 15) >> 4) << 4);
        ByteBuffer packet = ByteBuffer.allocate(realSize).order(ByteOrder.LITTLE_ENDIAN);
        packet.putInt(size).putInt(cmd.value);
        return packet;
    }

    static byte[] makeSearchPacket() {
        StringBuilder packet = new StringBuilder("SRCH * HTTP/1.1\n");
        packet.append(DDP_VERSION);
        return packet.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A packet to start up the PS4 from standby mode.
     *
     * @param userCredential A 64 character long hex string.
     * @return A wake-up packet.
     */
    static byte[] makeWakeupPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("WAKEUP * HTTP/1.1\n");
        packet.append("client-type:a\n"); // i or a
        packet.append("auth-type:C\n");
        packet.append("model:a\n");
        packet.append("app-type:g\n"); // c or g
        packet.append("user-credential:" + userCredential + "\n");
        packet.append(DDP_VERSION);
        return packet.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A packet to start up communication with the PS4.
     *
     * @param userCredential A 64 character long hex string
     * @return A launch packet.
     */
    static byte[] makeLaunchPacket(String userCredential) {
        StringBuilder packet = new StringBuilder("LAUNCH * HTTP/1.1\n");
        packet.append("user-credential:" + userCredential + "\n");
        packet.append(DDP_VERSION);
        return packet.toString().getBytes(StandardCharsets.UTF_8);
    }

    static ByteBuffer makeHelloPacket() {
        ByteBuffer packet = newPacketOfSize(28, PS4Command.HELLO_REQ);
        packet.putInt(REQ_VERSION);
        packet.put(new byte[16]); // Seed = 16 bytes
        packet.rewind();
        return packet;
    }

    /**
     * Make a login packet, also used when pairing the device to the PS4.
     *
     * @param userCredential
     * @param passCode
     * @param pairingCode
     * @return
     */
    static ByteBuffer makeLoginPacket(String userCredential, String passCode, String pairingCode) {
        ByteBuffer packet = newPacketForEncryption(16 + 64 + 256 + 16 + 16 + 16, PS4Command.LOGIN_REQ);
        if (passCode.length() == 4) {
            packet.put(passCode.getBytes(), 0, 4); // Pass-code
        }
        packet.position(12);
        packet.putInt(0x0F00); // Magic number (was 0x0201 before).
        if (userCredential.length() == 64) {
            packet.put(userCredential.getBytes(StandardCharsets.US_ASCII), 0, 64);
        }
        packet.position(16 + 64);
        packet.put(APPLICATION_NAME.getBytes(StandardCharsets.UTF_8)); // app_label
        packet.position(16 + 64 + 256);
        packet.put(OS_VERSION.getBytes()); // os_version
        packet.position(16 + 64 + 256 + 16);
        packet.put(DEVICE_NAME.getBytes(StandardCharsets.UTF_8)); // Model, name of paired unit, shown on the PS4
                                                                  // in the settings view.
        packet.position(16 + 64 + 256 + 16 + 16);
        if (pairingCode.length() == 8) {
            packet.put(pairingCode.getBytes(), 0, 8); // Pairing-code
        }
        return packet;
    }

    /**
     * Required for getting HPPTd status. Tell the PS4 who we are?
     *
     * @param clientID Example: "com.playstation.mobile2ndscreen".
     * @param clientVersion Example: "18.9.3"
     * @return A ClientID packet.
     */
    static ByteBuffer makeClientIDPacket(String clientID, String clientVersion) {
        ByteBuffer packet = newPacketForEncryption(8 + 128 + 32, PS4Command.CLIENT_IDENTITY_REQ);
        int length = clientID.length();
        if (length < 128) {
            packet.put(clientID.getBytes(StandardCharsets.UTF_8));
        }
        packet.position(8 + 128);
        length = clientVersion.length();
        if (length < 32) {
            packet.put(clientVersion.getBytes(StandardCharsets.UTF_8));
        }
        return packet;
    }

    /**
     * Ask for PS4 status.
     *
     * @param status Can be one of 0 or 1?
     * @return A ServerStatus packet.
     */
    static ByteBuffer makeStatusPacket(int status) {
        ByteBuffer packet = newPacketForEncryption(12, PS4Command.STATUS_REQ);
        packet.putInt(status); // status
        return packet;
    }

    /**
     * Makes a packet that puts the PS4 in standby mode.
     *
     * @return A standby-packet.
     */
    static ByteBuffer makeStandbyPacket() {
        return newPacketForEncryption(8, PS4Command.STANDBY_REQ);
    }

    /**
     * Tries to start an application on the PS4.
     *
     * @param applicationId The ID of the application.
     * @return An appStart-packet
     */
    static ByteBuffer makeApplicationPacket(String applicationId) {
        ByteBuffer packet = newPacketForEncryption(8 + 16, PS4Command.APP_START_REQ);
        packet.put(applicationId.getBytes(StandardCharsets.UTF_8)); // Application Id (CUSAxxxxx)
        return packet;
    }

    /**
     * Makes a packet that closes down the connection with the PS4.
     *
     * @return A ByeBye-packet.
     */
    static ByteBuffer makeByebyePacket() {
        return newPacketForEncryption(8, PS4Command.BYEBYE_REQ);
    }

    /**
     * This doesn't seem to do anything?
     *
     * @return A logout-packet.
     */
    static ByteBuffer makeLogoutPacket() {
        return newPacketForEncryption(8, PS4Command.LOGOUT_REQ);
    }

    /**
     *
     * @return A screenshot-packet?
     */
    static ByteBuffer makeScreenShotPacket() {
        ByteBuffer packet = newPacketForEncryption(12, PS4Command.SCREEN_SHOT_REQ);
        packet.putInt(1);
        return packet;
    }

    static String parseHTTPdPacket(ByteBuffer buffer) {
        buffer.position(0);
        int status = buffer.getInt(8);
        int port = buffer.getInt(12);
        int option = buffer.getInt(16);
        return String.format("status:%d, port:%d, option:%08x.", status, port, option);
    }

    /**
     * Tell the PS4 that we want to get info about the OnScreenKeyboard.
     *
     * @return An OSKStartPacket.
     */
    static ByteBuffer makeOSKStartPacket() {
        return newPacketForEncryption(8, PS4Command.OSK_START_REQ);
    }

    /**
     * Send text to the OSK on the PS4. Replaces all the text as it is now.
     *
     * @param text
     * @return An OSKStringChangePacket.
     */
    static ByteBuffer makeOSKStringChangePacket(String text) {
        byte[] chars = text.getBytes(StandardCharsets.UTF_16LE);
        ByteBuffer packet = newPacketForEncryption(28 + chars.length, PS4Command.OSK_CHANGE_STRING_REQ);
        packet.putInt(text.length()); // preEditIndex
        packet.putInt(0); // preEditLength
        packet.putInt(text.length()); // caretIndex
        packet.putInt(0); // editIndex
        packet.putInt(0); // editLength
        packet.put(chars);
        return packet;
    }

    /**
     * Parses out the text from an OSKStringChange-packet.
     *
     * @param buffer The received packet from the PS4.
     * @return The text in the packet.
     */
    static String parseOSKStringChangePacket(ByteBuffer buffer) {
        buffer.position(0);
        int length = buffer.getInt() - 28;
        byte[] chars = new byte[length];
        buffer.position(28);
        buffer.get(chars);
        return new String(chars, StandardCharsets.UTF_16LE);
    }

    /**
     *
     * @param command 0 = return, 1 = close.
     * @return
     */
    static ByteBuffer makeOSKControlPacket(int command) {
        ByteBuffer packet = newPacketForEncryption(12, PS4Command.OSK_CONTROL_REQ);
        packet.putInt(command);
        return packet;
    }

    static ByteBuffer makeRemoteControlPacket(int pushedKey) {
        ByteBuffer packet = newPacketForEncryption(16, PS4Command.REMOTE_CONTROL_REQ);
        packet.putInt(pushedKey);
        packet.putInt(0); // HoldTime in milliseconds
        return packet;
    }

    /**
     *
     * @param i only 0?
     * @return
     */
    static ByteBuffer makeCommentViewerStart(int i) {
        ByteBuffer packet = newPacketForEncryption(12, PS4Command.COMMENT_VIEWER_START_REQ);
        packet.putInt(i);
        return packet;
    }

    /**
     *
     * @param type Can be 5?
     * @param info If type is 5 only check bit 0.
     * @return
     */
    static ByteBuffer makeCommentViewerEvent(int type, int info) {
        ByteBuffer packet = newPacketForEncryption(16, PS4Command.COMMENT_VIEWER_EVENT);
        packet.putInt(type);
        packet.putInt(info);
        return packet;
    }

    static ByteBuffer makeCommentViewerSendPacket(int i, String text) {
        byte[] chars = (text.length() > 60 ? text.substring(0, 60) : text).getBytes(StandardCharsets.UTF_8);
        ByteBuffer packet = newPacketForEncryption(12 + chars.length, PS4Command.OSK_CHANGE_STRING_REQ);
        packet.putInt(i);
        packet.put(chars);
        return packet;
    }
}
