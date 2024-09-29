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
package org.openhab.binding.nibeheatpump.internal.protocol;

import java.io.ByteArrayOutputStream;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;

/**
 * This class contains useful Nibe heat pump protocol utils.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpProtocol {

    public static final byte PDU_MIN_LEN = 3;

    public static final byte PDU_CHECKSUM_LEN = 1;

    public static final byte FRAME_START_CHAR_RES = (byte) 0x5C;
    public static final byte FRAME_START_CHAR_REQ = (byte) 0xC0;

    public static final byte OFFSET_START = 0;
    public static final byte RES_OFFS_ADR = 2;
    public static final byte RES_OFFS_CMD = 3;
    public static final byte RES_OFFS_LEN = 4;
    public static final byte RES_OFFS_DATA = 5;

    public static final byte REQ_OFFS_CMD = 1;
    public static final byte REQ_OFFS_LEN = 2;

    public static final byte RES_HEADER_LEN = 5;
    public static final byte REQ_HEADER_LEN = 3;

    public static final byte CMD_RMU_DATA_MSG = (byte) 0x62;
    public static final byte CMD_MODBUS_DATA_MSG = (byte) 0x68;
    public static final byte CMD_MODBUS_READ_REQ = (byte) 0x69;
    public static final byte CMD_MODBUS_READ_RESP = (byte) 0x6A;
    public static final byte CMD_MODBUS_WRITE_REQ = (byte) 0x6B;
    public static final byte CMD_MODBUS_WRITE_RESP = (byte) 0x6C;

    public static final byte ADR_SMS40 = (byte) 0x16;
    public static final byte ADR_RMU40 = (byte) 0x19;
    public static final byte ADR_MODBUS40 = (byte) 0x20;

    public static boolean isModbus40DataReadOut(byte[] data) {
        if (data[OFFSET_START] == FRAME_START_CHAR_RES && data[1] == (byte) 0x00
                && data[RES_OFFS_ADR] == ADR_MODBUS40) {
            return data[RES_OFFS_CMD] == CMD_MODBUS_DATA_MSG && data[RES_OFFS_LEN] >= (byte) 0x50;
        }

        return false;
    }

    public static boolean isModbus40ReadResponse(byte[] data) {
        if (data[OFFSET_START] == FRAME_START_CHAR_RES && data[1] == (byte) 0x00
                && data[RES_OFFS_ADR] == ADR_MODBUS40) {
            return data[RES_OFFS_CMD] == CMD_MODBUS_READ_RESP && data[RES_OFFS_LEN] >= (byte) 0x06;
        }

        return false;
    }

    public static boolean isRmu40DataReadOut(byte[] data) {
        if (data[0] == FRAME_START_CHAR_RES && data[1] == (byte) 0x00 && data[RES_OFFS_ADR] == ADR_RMU40) {
            return data[RES_OFFS_CMD] == CMD_RMU_DATA_MSG && data[RES_OFFS_LEN] >= (byte) 0x18;
        }

        return false;
    }

    public static boolean isModbus40WriteResponsePdu(byte[] data) {
        return data[OFFSET_START] == FRAME_START_CHAR_RES && data[1] == (byte) 0x00
                && data[RES_OFFS_ADR] == ADR_MODBUS40 && data[RES_OFFS_CMD] == CMD_MODBUS_WRITE_RESP;
    }

    public static boolean isModbus40WriteTokenPdu(byte[] data) {
        return data[0] == FRAME_START_CHAR_RES && data[1] == (byte) 0x00 && data[RES_OFFS_ADR] == ADR_MODBUS40
                && data[RES_OFFS_CMD] == CMD_MODBUS_WRITE_REQ && data[RES_OFFS_LEN] == 0x00;
    }

    public static boolean isModbus40ReadTokenPdu(byte[] data) {
        return data[OFFSET_START] == FRAME_START_CHAR_RES && data[1] == (byte) 0x00
                && data[RES_OFFS_ADR] == ADR_MODBUS40 && data[RES_OFFS_CMD] == CMD_MODBUS_READ_REQ
                && data[RES_OFFS_LEN] == 0x00;
    }

    public static boolean isModbus40WriteRequestPdu(byte[] data) {
        return data[OFFSET_START] == FRAME_START_CHAR_REQ && data[REQ_OFFS_CMD] == CMD_MODBUS_WRITE_REQ;
    }

    public static boolean isModbus40ReadRequestPdu(byte[] data) {
        return data[OFFSET_START] == FRAME_START_CHAR_REQ && data[REQ_OFFS_CMD] == CMD_MODBUS_READ_REQ;
    }

    public static byte calculateChecksum(byte[] data) {
        return calculateChecksum(data, 0, data.length);
    }

    public static byte calculateChecksum(byte[] data, int startIndex, int stopIndex) {
        byte checksum = 0;
        // calculate XOR checksum
        for (int i = startIndex; i < stopIndex; i++) {
            checksum ^= data[i];
        }
        return checksum;
    }

    public static byte getMessageType(byte[] data) {
        byte messageType = 0;

        if (data[NibeHeatPumpProtocol.OFFSET_START] == NibeHeatPumpProtocol.FRAME_START_CHAR_RES) {
            messageType = data[RES_OFFS_CMD];
        } else if (data[NibeHeatPumpProtocol.OFFSET_START] == NibeHeatPumpProtocol.FRAME_START_CHAR_REQ) {
            messageType = data[REQ_OFFS_CMD];
        }

        return messageType;
    }

    public static byte[] checkMessageChecksumAndRemoveDoubles(byte[] data) throws NibeHeatPumpException {
        int msglen;
        int startIndex;
        int stopIndex;

        if (NibeHeatPumpProtocol.isModbus40ReadRequestPdu(data)
                || NibeHeatPumpProtocol.isModbus40WriteRequestPdu(data)) {
            msglen = REQ_HEADER_LEN + data[REQ_OFFS_LEN];
            startIndex = 0;
            stopIndex = msglen;
        } else {
            msglen = RES_HEADER_LEN + data[RES_OFFS_LEN];
            startIndex = 2;
            stopIndex = msglen;
        }

        final byte checksum = calculateChecksum(data, startIndex, stopIndex);
        final byte msgChecksum = data[msglen];

        // if checksum is 0x5C (start character), heat pump seems to send 0xC5 checksum

        if (checksum == msgChecksum || (checksum == FRAME_START_CHAR_RES && msgChecksum == (byte) 0xC5)) {
            // if data contains 0x5C (start character), data seems to contains double 0x5C characters
            return removeEscapedDuplicates(data, msglen);
        } else {
            throw new NibeHeatPumpException(
                    "Checksum does not match. Checksum=" + (msgChecksum & 0xFF) + ", expected=" + (checksum & 0xFF));
        }
    }

    private static byte[] removeEscapedDuplicates(byte[] data, int msglen) {
        if (dataContainsEscapedDuplicates(data, msglen)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(msglen);
            byte newlen = data[RES_OFFS_LEN];

            // write start char
            out.write(FRAME_START_CHAR_RES);

            // remove all duplicates between start char and checksum bytes
            // checksum byte can't be 0x5C as it's set to 0xC5 in this case by the heat pump
            for (int i = 1; i < msglen; i++) {
                if (data[i] == FRAME_START_CHAR_RES && data[i + 1] == FRAME_START_CHAR_RES) {
                    // write one 0x5C
                    out.write(FRAME_START_CHAR_RES);

                    // skip next 0x5C and decrease the length
                    i++;
                    newlen--;
                } else {
                    out.write(data[i]);
                }
            }

            // write checksum
            out.write(data[msglen]);

            // return modified data
            byte[] newdata = out.toByteArray();
            newdata[RES_OFFS_LEN] = newlen;
            return newdata;
        }

        return data;
    }

    private static boolean dataContainsEscapedDuplicates(byte[] data, int msglen) {
        for (int i = 1; i < msglen; i++) {
            if (data[i] == FRAME_START_CHAR_RES && data[i + 1] == FRAME_START_CHAR_RES) {
                return true;
            }
        }
        return false;
    }
}
