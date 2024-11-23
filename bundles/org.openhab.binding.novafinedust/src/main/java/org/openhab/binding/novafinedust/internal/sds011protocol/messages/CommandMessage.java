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
package org.openhab.binding.novafinedust.internal.sds011protocol.messages;

import java.io.ByteArrayOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;

/**
 * Message to be send to the device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class CommandMessage {
    private static final byte HEAD = -86; // AA
    private static final byte COMMAND_ID = -76; // B4
    private static final byte TAIL = -85; // AB

    private static final int DATA_BYTES_AFTER_FIRST_DATA_BYTE = 12;

    private final byte firstDataByte;
    private byte[] payLoad = new byte[DATA_BYTES_AFTER_FIRST_DATA_BYTE];
    private byte[] targetDevice = new byte[] { -1, -1 }; // FF FF = all devices

    public CommandMessage(byte command, byte[] payLoad) {
        this.firstDataByte = command;
        this.payLoad = payLoad;
    }

    public CommandMessage(byte command, byte[] payLoad, byte[] targetDevice) {
        this.firstDataByte = command;
        this.payLoad = payLoad;
        this.targetDevice = targetDevice;
    }

    /**
     * Get the raw bytes to be send out to the device
     *
     * @return ByteArray containing the bytes for a message to the device
     */
    public byte[] getBytes() {
        ByteArrayOutputStream message = new ByteArrayOutputStream(19);

        message.write(HEAD);
        message.write(COMMAND_ID);
        message.write(firstDataByte);

        for (byte b : payLoad) {
            message.write(b);
        }
        int padding = DATA_BYTES_AFTER_FIRST_DATA_BYTE - payLoad.length;
        for (int i = 0; i < padding; i++) {
            message.write(0x00);
        }

        for (byte b : targetDevice) {
            message.write(b);
        }
        message.write(calculateCheckSum(message.toByteArray()));
        message.write(TAIL);

        return message.toByteArray();
    }

    private byte calculateCheckSum(byte[] data) {
        int checksum = 0;
        for (int i = 2; i <= 14; i++) {
            checksum += data[i];
        }
        checksum = (checksum - 2) % 256;

        return (byte) checksum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message: ");
        sb.append("Command=" + firstDataByte);
        sb.append(" Target Device=" + HexUtils.bytesToHex(targetDevice));
        sb.append(" Payload=" + HexUtils.bytesToHex(payLoad));
        return sb.toString();
    }
}
