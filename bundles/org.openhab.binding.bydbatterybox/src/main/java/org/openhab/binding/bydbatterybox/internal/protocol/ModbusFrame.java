/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal.protocol;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builds and checks the Modbus RTU frames the battery exchanges over plain TCP (no MBAP header, unit address 1,
 * CRC-16/MODBUS appended low byte first).
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public final class ModbusFrame {

    static final int UNIT_ADDRESS = 0x01;
    static final int FC_READ_HOLDING_REGISTERS = 0x03;
    static final int FC_WRITE_MULTIPLE_REGISTERS = 0x10;
    static final int EXCEPTION_FLAG = 0x80;
    static final int EXCEPTION_SERVER_DEVICE_BUSY_OR_FAILURE = 0x04;

    /** Length of a write-multiple-registers reply and of an exception reply. */
    static final int WRITE_REPLY_LENGTH = 8;
    static final int EXCEPTION_REPLY_LENGTH = 5;

    /** Returned by {@link #expectedLength(byte[], int)} while too few bytes have been received to decide. */
    static final int LENGTH_UNKNOWN = -1;

    private ModbusFrame() {
    }

    public static byte[] readHoldingRegisters(int register, int quantity) {
        return appendCrc(new byte[] { UNIT_ADDRESS, FC_READ_HOLDING_REGISTERS, (byte) (register >>> 8), (byte) register,
                (byte) (quantity >>> 8), (byte) quantity });
    }

    public static byte[] writeMultipleRegisters(int register, int... values) {
        byte[] frame = new byte[7 + 2 * values.length];
        frame[0] = UNIT_ADDRESS;
        frame[1] = FC_WRITE_MULTIPLE_REGISTERS;
        frame[2] = (byte) (register >>> 8);
        frame[3] = (byte) register;
        frame[4] = (byte) (values.length >>> 8);
        frame[5] = (byte) values.length;
        frame[6] = (byte) (2 * values.length);
        for (int i = 0; i < values.length; i++) {
            frame[7 + 2 * i] = (byte) (values[i] >>> 8);
            frame[8 + 2 * i] = (byte) values[i];
        }
        return appendCrc(frame);
    }

    /**
     * Returns the total length of the reply frame whose first {@code received} bytes are in {@code buffer}, or
     * {@link #LENGTH_UNKNOWN} if more bytes are needed to decide.
     *
     * @throws BydCommunicationException if the function code cannot start a valid reply
     */
    static int expectedLength(byte[] buffer, int received) throws BydCommunicationException {
        if (received < 2) {
            return LENGTH_UNKNOWN;
        }
        int functionCode = buffer[1] & 0xFF;
        if ((functionCode & EXCEPTION_FLAG) != 0) {
            return EXCEPTION_REPLY_LENGTH;
        }
        if (functionCode == FC_WRITE_MULTIPLE_REGISTERS) {
            return WRITE_REPLY_LENGTH;
        }
        if (functionCode == FC_READ_HOLDING_REGISTERS) {
            return received < 3 ? LENGTH_UNKNOWN : (buffer[2] & 0xFF) + 5;
        }
        throw new BydCommunicationException(BydCommunicationException.Reason.PROTOCOL,
                String.format("unexpected function code 0x%02x", functionCode));
    }

    /**
     * Checks a complete reply against the request it answers.
     *
     * @throws BydCommunicationException with {@link BydCommunicationException.Reason#NOT_READY} for exception 0x04
     *             and {@link BydCommunicationException.Reason#PROTOCOL} for any other violation
     */
    static void validateReply(byte[] request, byte[] reply) throws BydCommunicationException {
        if (crc(reply, reply.length) != 0) {
            throw protocolError("CRC error", reply);
        }
        if ((reply[0] & 0xFF) != UNIT_ADDRESS) {
            throw protocolError("unexpected unit address", reply);
        }
        int functionCode = reply[1] & 0xFF;
        if (functionCode == ((request[1] & 0xFF) | EXCEPTION_FLAG)) {
            int code = reply[2] & 0xFF;
            if (code == EXCEPTION_SERVER_DEVICE_BUSY_OR_FAILURE) {
                throw new BydCommunicationException(BydCommunicationException.Reason.NOT_READY,
                        String.format("exception 0x%02x for function 0x%02x", code, request[1] & 0xFF));
            }
            throw protocolError(String.format("exception 0x%02x", code), reply);
        }
        if (functionCode != (request[1] & 0xFF)) {
            throw protocolError("function code mismatch", reply);
        }
        // a write reply echoes unit, function code, start register and quantity of the request
        if (functionCode == FC_WRITE_MULTIPLE_REGISTERS && !Arrays.equals(reply, 0, 6, request, 0, 6)) {
            throw protocolError("write echo mismatch", reply);
        }
        if (functionCode == FC_READ_HOLDING_REGISTERS) {
            int quantity = ((request[4] & 0xFF) << 8) | (request[5] & 0xFF);
            if ((reply[2] & 0xFF) != 2 * quantity) {
                throw protocolError("unexpected byte count", reply);
            }
        }
    }

    static int crc(byte[] data, int length) {
        int crc = 0xFFFF;
        for (int i = 0; i < length; i++) {
            crc ^= data[i] & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 1) != 0 ? (crc >>> 1) ^ 0xA001 : crc >>> 1;
            }
        }
        return crc;
    }

    private static byte[] appendCrc(byte[] body) {
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        int crc = crc(body, body.length);
        frame[body.length] = (byte) crc;
        frame[body.length + 1] = (byte) (crc >>> 8);
        return frame;
    }

    private static BydCommunicationException protocolError(String what, byte[] reply) {
        return new BydCommunicationException(BydCommunicationException.Reason.PROTOCOL,
                what + " in reply of " + reply.length + " bytes");
    }
}
