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
package org.openhab.binding.mideaac.internal.handler;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mideaac.internal.Utils;

/**
 * The {@link Packet} class for Midea AC creates the
 * byte array that is sent to the device
 *
 * @author Jacek Dobrowolski - Initial contribution
 */
@NonNullByDefault
public class Packet {
    private CommandBase command;
    private byte[] packet;
    private MideaACHandler mideaACHandler;

    /**
     * The Packet class parameters
     * 
     * @param command command from Command Base
     * @param deviceId the device ID
     * @param mideaACHandler the MideaACHandler class
     */
    public Packet(CommandBase command, String deviceId, MideaACHandler mideaACHandler) {
        this.command = command;
        this.mideaACHandler = mideaACHandler;

        packet = new byte[] {
                // 2 bytes - StaticHeader
                (byte) 0x5a, (byte) 0x5a,
                // 2 bytes - mMessageType
                (byte) 0x01, (byte) 0x11,
                // 2 bytes - PacketLength
                (byte) 0x00, (byte) 0x00,
                // 2 bytes
                (byte) 0x20, (byte) 0x00,
                // 4 bytes - MessageId
                0x00, 0x00, 0x00, 0x00,
                // 8 bytes - Date&Time
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // 6 bytes - mDeviceID
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // 14 bytes
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

        LocalDateTime now = LocalDateTime.now();
        byte[] datetimeBytes = { (byte) (now.getYear() / 100), (byte) (now.getYear() % 100), (byte) now.getMonthValue(),
                (byte) now.getDayOfMonth(), (byte) now.getHour(), (byte) now.getMinute(), (byte) now.getSecond(),
                (byte) System.currentTimeMillis() };

        System.arraycopy(datetimeBytes, 0, packet, 12, 8);

        byte[] idBytes = new BigInteger(deviceId).toByteArray();
        byte[] idBytesRev = Utils.reverse(idBytes);
        System.arraycopy(idBytesRev, 0, packet, 20, 6);
    }

    /**
     * Final composure of the byte array with the encrypted command
     */
    public void compose() {
        command.compose();

        // Append the command data(48 bytes) to the packet
        byte[] cmdEncrypted = mideaACHandler.getSecurity().aesEncrypt(command.getBytes());

        // Ensure 48 bytes
        if (cmdEncrypted.length < 48) {
            byte[] paddedCmdEncrypted = new byte[48];
            System.arraycopy(cmdEncrypted, 0, paddedCmdEncrypted, 0, cmdEncrypted.length);
            cmdEncrypted = paddedCmdEncrypted;
        }

        byte[] newPacket = new byte[packet.length + cmdEncrypted.length];
        System.arraycopy(packet, 0, newPacket, 0, packet.length);
        System.arraycopy(cmdEncrypted, 0, newPacket, packet.length, cmdEncrypted.length);
        packet = newPacket;

        // Override packet length bytes with actual values
        byte[] lenBytes = { (byte) (packet.length + 16), 0 };
        System.arraycopy(lenBytes, 0, packet, 4, 2);

        // calculate checksum data
        byte[] checksumData = mideaACHandler.getSecurity().encode32Data(packet);

        // Append a basic checksum data(16 bytes) to the packet
        byte[] newPacketTwo = new byte[packet.length + checksumData.length];
        System.arraycopy(packet, 0, newPacketTwo, 0, packet.length);
        System.arraycopy(checksumData, 0, newPacketTwo, packet.length, checksumData.length);
        packet = newPacketTwo;
    }

    /**
     * Returns the packet for sending
     * 
     * @return packet for socket writer
     */
    public byte[] getBytes() {
        return packet;
    }
}
