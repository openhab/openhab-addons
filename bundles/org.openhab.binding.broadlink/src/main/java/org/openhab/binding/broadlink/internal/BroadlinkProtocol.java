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
package org.openhab.binding.broadlink.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.crypto.spec.IvParameterSpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;

/**
 * Static methods for working with the Broadlink network prototcol.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkProtocol {

    public static byte[] buildMessage(byte command, byte[] payload, int count, byte[] mac, byte[] deviceId, byte[] iv,
            byte[] key, int deviceType, Logger logger) {
        byte packet[] = new byte[0x38];
        packet[0x00] = 0x5a;
        packet[0x01] = (byte) 0xa5; // https://stackoverflow.com/questions/20026942/type-mismatch-cannot-convert-int-to-byte
        /*
         * int 0b10000000 is 128 byte 0b10000000 is -128
         */
        packet[0x02] = (byte) 0xaa;
        packet[0x03] = 0x55;
        packet[0x04] = 0x5a;
        packet[0x05] = (byte) 0xa5;
        packet[0x06] = (byte) 0xaa;
        packet[0x07] = 0x55;
        packet[0x24] = (byte) (deviceType & 0xff);
        packet[0x25] = (byte) (deviceType >> 8);
        packet[0x26] = command;
        packet[0x28] = (byte) (count & 0xff);
        packet[0x29] = (byte) (count >> 8);
        packet[0x2a] = mac[5];
        packet[0x2b] = mac[4];
        packet[0x2c] = mac[3];
        packet[0x2d] = mac[2];
        packet[0x2e] = mac[1];
        packet[0x2f] = mac[0];
        packet[0x30] = deviceId[0];
        packet[0x31] = deviceId[1];
        packet[0x32] = deviceId[2];
        packet[0x33] = deviceId[3];
        int checksum = 0xBEAF;
        int i = 0;
        byte abyte0[];
        int k = (abyte0 = payload).length;
        for (int j = 0; j < k; j++) {
            byte b = abyte0[j];
            i = Byte.toUnsignedInt(b);
            checksum += i;
            checksum &= 0xffff;
        }
        packet[0x34] = (byte) (checksum & 0xff);
        packet[0x35] = (byte) (checksum >> 8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(packet);
            outputStream.write(Utils.encrypt(key, new IvParameterSpec(iv), payload));
        } catch (IOException e) {
            logger.warn("IOException while building message: {}", e.getMessage());
            return packet;
        }
        byte data[] = outputStream.toByteArray();
        checksum = 0xBEAF;
        byte abyte1[];
        int i1 = (abyte1 = data).length;
        for (int l = 0; l < i1; l++) {
            byte b = abyte1[l];
            i = Byte.toUnsignedInt(b);
            checksum += i;
            checksum &= 0xffff;
        }
        data[0x20] = (byte) (checksum & 0xff);
        data[0x21] = (byte) (checksum >> 8);
        return data;
    }

    public static byte[] buildAuthenticationPayload() {
        // https://github.com/mjg59/python-broadlink/blob/master/protocol.md
        byte payload[] = new byte[0x50];
        payload[0x04] = 0x31;
        payload[0x05] = 0x31;
        payload[0x06] = 0x31;
        payload[0x07] = 0x31;
        payload[0x08] = 0x31;
        payload[0x09] = 0x31;
        payload[0x0a] = 0x31;
        payload[0x0b] = 0x31;
        payload[0x0c] = 0x31;
        payload[0x0d] = 0x31;
        payload[0x0e] = 0x31;
        payload[0x0f] = 0x31;
        payload[0x10] = 0x31;
        payload[0x11] = 0x31;
        payload[0x12] = 0x31;
        payload[0x13] = 0x31;
        payload[0x14] = 0x31;
        payload[0x1e] = 0x01;
        payload[0x2d] = 0x01;
        payload[0x30] = (byte) 'T';
        payload[0x31] = (byte) 'e';
        payload[0x32] = (byte) 's';
        payload[0x33] = (byte) 't';
        payload[0x34] = (byte) ' ';
        payload[0x35] = (byte) ' ';
        payload[0x36] = (byte) '1';

        return payload;
    }

    public static byte[] buildDiscoveryPacket(String host, int port) {
        String localAddress[] = null;
        localAddress = host.toString().split("\\.");
        int ipAddress[] = new int[4];
        for (int i = 0; i < 4; i++) {
            ipAddress[i] = Integer.parseInt(localAddress[i]);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        TimeZone timeZone = TimeZone.getDefault();
        int timezone = timeZone.getRawOffset() / 0x36ee80;
        byte packet[] = new byte[48];
        if (timezone < 0) {
            packet[8] = (byte) ((255 + timezone) - 1);
            packet[9] = -1;
            packet[10] = -1;
            packet[11] = -1;
        } else {
            packet[8] = 8;
            packet[9] = 0;
            packet[10] = 0;
            packet[11] = 0;
        }
        packet[12] = (byte) (calendar.get(1) & 0xff);
        packet[13] = (byte) (calendar.get(1) >> 8);
        packet[14] = (byte) calendar.get(12);
        packet[15] = (byte) calendar.get(11);
        packet[16] = (byte) (calendar.get(1) - 2000);
        packet[17] = (byte) (calendar.get(7) + 1);
        packet[18] = (byte) calendar.get(5);
        packet[19] = (byte) (calendar.get(2) + 1);
        packet[24] = (byte) ipAddress[0];
        packet[25] = (byte) ipAddress[1];
        packet[26] = (byte) ipAddress[2];
        packet[27] = (byte) ipAddress[3];
        packet[28] = (byte) (port & 0xff);
        packet[29] = (byte) (port >> 8);
        packet[38] = 6;
        int checksum = 0xBEAF;
        byte abyte0[];
        int k = (abyte0 = packet).length;
        for (int j = 0; j < k; j++) {
            byte b = abyte0[j];
            checksum += Byte.toUnsignedInt(b);
        }

        checksum &= 0xffff;
        packet[32] = (byte) (checksum & 0xff);
        packet[33] = (byte) (checksum >> 8);
        return packet;
    }

    public static final int MIN_RESPONSE_PACKET_LENGTH = 0x24;

    public static byte[] decodePacket(byte[] packet, byte[] authorizationKey, String initializationVector)
            throws IOException {
        if (packet.length < MIN_RESPONSE_PACKET_LENGTH) {
            throw new ProtocolException("Unexpectedly short packet; length " + packet.length
                    + " is shorter than protocol minimum " + MIN_RESPONSE_PACKET_LENGTH);
        }
        boolean error = packet[0x22] != 0 || packet[0x23] != 0;
        if (error) {
            throw new ProtocolException(String.format("Response from device is not valid. (0x22=0x%02X,0x23=0x%02X)",
                    packet[0x22], packet[0x23]));
        }

        try {
            IvParameterSpec ivSpec = new IvParameterSpec(HexUtils.hexToBytes(initializationVector));
            return Utils.decrypt(authorizationKey, ivSpec, Utils.padTo(Utils.slice(packet, 56, packet.length), 16));
        } catch (Exception ex) {
            throw new IOException("Failed while getting device status", ex);
        }
    }

    public static byte[] getDeviceId(byte response[]) {
        return Utils.slice(response, 0, 4);
    }

    public static byte[] getDeviceKey(byte response[]) {
        return Utils.slice(response, 4, 20);
    }
}
