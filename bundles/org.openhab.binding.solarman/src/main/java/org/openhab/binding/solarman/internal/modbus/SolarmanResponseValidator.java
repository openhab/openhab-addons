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
package org.openhab.binding.solarman.internal.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;

/**
 * Validates transaction identity and Modbus acknowledgements before accepting device data.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public final class SolarmanResponseValidator {
    private SolarmanResponseValidator() {
    }

    public static void validateRead(byte[] frame, int function, int count, boolean crcRequired)
            throws SolarmanProtocolException {
        validateRead(frame, function, count, crcRequired, SolarmanProtocol.DEFAULT_SLAVE_ID);
    }

    public static void validateRead(byte[] frame, int function, int count, boolean crcRequired, int slaveId)
            throws SolarmanProtocolException {
        validateFunction(frame, function, slaveId);
        int dataLength = 3 + count * 2;
        if (frame.length < dataLength || Byte.toUnsignedInt(frame[2]) != count * 2) {
            throw new SolarmanProtocolException("Read response byte count does not match the request");
        }
        validateCrc(frame, dataLength, crcRequired);
    }

    public static void validateWrite(byte[] frame, int start, int count, boolean crcRequired)
            throws SolarmanProtocolException {
        validateWrite(frame, start, count, crcRequired, SolarmanProtocol.DEFAULT_SLAVE_ID);
    }

    public static void validateWrite(byte[] frame, int start, int count, boolean crcRequired, int slaveId)
            throws SolarmanProtocolException {
        validateFunction(frame, 16, slaveId);
        if (frame.length < 6 || Short.toUnsignedInt(ByteBuffer.wrap(frame, 2, 2).getShort()) != start
                || Short.toUnsignedInt(ByteBuffer.wrap(frame, 4, 2).getShort()) != count) {
            throw new SolarmanProtocolException("Write acknowledgement address or count does not match the request");
        }
        validateCrc(frame, 6, crcRequired);
    }

    private static void validateFunction(byte[] frame, int function, int slaveId) throws SolarmanProtocolException {
        if (frame.length < 3 || Byte.toUnsignedInt(frame[0]) != slaveId) {
            throw new SolarmanProtocolException("Invalid Modbus slave or truncated response");
        }
        if (Byte.toUnsignedInt(frame[1]) == (function | 128)) {
            throw new SolarmanProtocolException("Modbus exception response: " + Byte.toUnsignedInt(frame[2]));
        }
        if (Byte.toUnsignedInt(frame[1]) != function) {
            throw new SolarmanProtocolException("Unexpected Modbus response function");
        }
    }

    private static void validateCrc(byte[] frame, int dataLength, boolean required) throws SolarmanProtocolException {
        if (!required && frame.length == dataLength) {
            return;
        }
        if (frame.length < dataLength + 2) {
            throw new SolarmanProtocolException("Response CRC is missing");
        }
        int received = Short
                .toUnsignedInt(ByteBuffer.wrap(frame, dataLength, 2).order(ByteOrder.LITTLE_ENDIAN).getShort());
        if (received != CRC16Modbus.calculate(Arrays.copyOf(frame, dataLength))) {
            throw new SolarmanProtocolException("Invalid Modbus response CRC");
        }
        // Some V5 firmware appends zero padding inside the envelope after the RTU CRC.
        for (int i = dataLength + 2; i < frame.length; i++) {
            if (frame[i] != 0) {
                throw new SolarmanProtocolException("Unexpected data after the Modbus response CRC");
            }
        }
    }

    public static void validateEnvelope(byte[] response, byte[] request, boolean v5) throws SolarmanProtocolException {
        if (v5) {
            if (response.length < 27) {
                throw new SolarmanProtocolException(
                        "V5 response too short: expected at least 27 bytes, received=" + response.length);
            }
            if (response[0] != (byte) 0xA5) {
                throw new SolarmanProtocolException(
                        String.format(Locale.ROOT, "V5 response start marker mismatch: expected=0xA5, received=0x%02X",
                                Byte.toUnsignedInt(response[0])));
            }
            if (response[response.length - 1] != 0x15) {
                throw new SolarmanProtocolException(
                        String.format(Locale.ROOT, "V5 response end marker mismatch: expected=0x15, received=0x%02X",
                                Byte.toUnsignedInt(response[response.length - 1])));
            }
            int control = Short
                    .toUnsignedInt(ByteBuffer.wrap(response, 3, 2).order(ByteOrder.LITTLE_ENDIAN).getShort());
            if (control != 0x1510) {
                throw new SolarmanProtocolException(String.format(Locale.ROOT,
                        "V5 response control code mismatch: expected=0x1510, received=0x%04X", control));
            }
            if (response[5] != request[5]) {
                throw new SolarmanProtocolException(
                        String.format(Locale.ROOT, "V5 response sequence mismatch: expected=0x%02X, received=0x%02X",
                                Byte.toUnsignedInt(request[5]), Byte.toUnsignedInt(response[5])));
            }
            long expectedSerial = Integer
                    .toUnsignedLong(ByteBuffer.wrap(request, 7, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            long receivedSerial = Integer
                    .toUnsignedLong(ByteBuffer.wrap(response, 7, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            if (receivedSerial != expectedSerial) {
                throw new SolarmanProtocolException("V5 response logger serial mismatch: expected=" + expectedSerial
                        + ", received=" + receivedSerial);
            }
            int length = Short.toUnsignedInt(ByteBuffer.wrap(response, 1, 2).order(ByteOrder.LITTLE_ENDIAN).getShort());
            if (length + 13 != response.length) {
                throw new SolarmanProtocolException("V5 response length mismatch: declared payload=" + length
                        + ", expected frame=" + (length + 13) + ", received frame=" + response.length);
            }
            int checksum = 0;
            for (int i = 1; i < response.length - 2; i++) {
                checksum += Byte.toUnsignedInt(response[i]);
            }
            int receivedChecksum = Byte.toUnsignedInt(response[response.length - 2]);
            if ((checksum & 255) != receivedChecksum) {
                throw new SolarmanProtocolException(
                        String.format(Locale.ROOT, "V5 response checksum mismatch: expected=0x%02X, received=0x%02X",
                                checksum & 255, receivedChecksum));
            }
        } else if (response.length < 9 || response[0] != request[0] || response[1] != request[1] || response[2] != 0
                || response[3] != 0
                || Short.toUnsignedInt(ByteBuffer.wrap(response, 4, 2).getShort()) != response.length - 6) {
            throw new SolarmanProtocolException("Invalid raw Modbus TCP response header");
        }
    }

    public static void validateWriteRequest(int start, byte[] data) {
        if (start < 0 || start > 65535 || data.length == 0 || data.length % 2 != 0 || data.length > 246
                || start + data.length / 2 > 65536) {
            throw new IllegalArgumentException("Invalid Modbus write range or payload size");
        }
    }
}
