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
package org.openhab.binding.solarman.internal.modbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanAuthenticationException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanV5Protocol implements SolarmanProtocol {
    private final SolarmanLoggerConfiguration solarmanLoggerConfiguration;

    public SolarmanV5Protocol(SolarmanLoggerConfiguration solarmanLoggerConfiguration) {
        this.solarmanLoggerConfiguration = solarmanLoggerConfiguration;
    }

    @Override
    public Map<Integer, byte[]> readRegisters(SolarmanLoggerConnection solarmanLoggerConnection, byte mbFunctionCode,
            int firstReg, int lastReg) throws SolarmanException {
        byte[] solarmanV5Frame = buildSolarmanV5Frame(mbFunctionCode, firstReg, lastReg);
        byte[] respFrame = solarmanLoggerConnection.sendRequest(solarmanV5Frame);
        if (respFrame.length > 0) {
            byte[] modbusRespFrame = extractModbusResponseFrame(respFrame, solarmanV5Frame);
            return parseModbusReadHoldingRegistersResponse(modbusRespFrame, firstReg, lastReg);
        } else {
            throw new SolarmanConnectionException("Response frame was empty");
        }
    }

    /**
     * Builds a SolarMAN V5 frame to request data from firstReg to lastReg.
     * Frame format is based on
     * <a href="https://pysolarmanv5.readthedocs.io/en/latest/solarmanv5_protocol.html">Solarman V5 Protocol</a>
     *
     * @param mbFunctionCode
     * @param firstReg - the start register
     * @param lastReg - the end register
     * @return byte array containing the Solarman V5 frame
     */
    protected byte[] buildSolarmanV5Frame(byte mbFunctionCode, int firstReg, int lastReg) {
        byte[] requestPayload = buildSolarmanV5FrameRequestPayload(mbFunctionCode, firstReg, lastReg);
        byte[] header = buildSolarmanV5FrameHeader(requestPayload.length);
        byte[] trailer = buildSolarmanV5FrameTrailer(header, requestPayload);

        return ByteBuffer.allocate(header.length + requestPayload.length + trailer.length).put(header)
                .put(requestPayload).put(trailer).array();
    }

    private byte[] buildSolarmanV5FrameTrailer(byte[] header, byte[] requestPayload) {
        byte[] headerAndPayload = ByteBuffer.allocate(header.length + requestPayload.length).put(header)
                .put(requestPayload).array();
        // (one byte) – Denotes the V5 frame checksum. The checksum is computed on the entire V5 frame except for Start,
        // Checksum (obviously!) and End.
        // Note, that this field is completely separate to the Modbus RTU checksum, which coincidentally, is the two
        // bytes immediately preceding this field.
        byte[] checksum = new byte[] {
                computeChecksum(Arrays.copyOfRange(headerAndPayload, 1, headerAndPayload.length)) };

        // (one byte) – Denotes the end of the V5 frame. Always 0x15.
        byte[] end = new byte[] { (byte) 0x15 };

        return ByteBuffer.allocate(checksum.length + end.length).put(checksum).put(end).array();
    }

    private byte computeChecksum(byte[] frame) {
        // [-91, 23, 0, 16, 69, 0, 0, 46, -13, 90, 102, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 0, 0, 39,
        // 5, -48, 122, 21]
        int checksumValue = 0;
        for (byte b : frame) {
            checksumValue += Byte.toUnsignedInt(b);
        }
        return (byte) (checksumValue & 0xFF);
    }

    private byte[] buildSolarmanV5FrameHeader(int payloadSize) {
        // (one byte) Denotes the start of the V5 frame. Always 0xA5.
        byte[] start = new byte[] { (byte) 0xA5 };

        // (two bytes) Payload length
        byte[] length = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).putShort((short) payloadSize)
                .array();

        // (two bytes) – Describes the type of V5 frame. For Modbus RTU requests, the control code is 0x4510. For Modbus
        // RTU responses, the control code is 0x1510.
        byte[] controlCode = new byte[] { (byte) 0x10, (byte) 0x45 };

        // (two bytes) – This field acts as a two-way sequence number. On outgoing requests, the first byte of this
        // field is echoed back in the same position on incoming responses.
        // This is done by initialising this byte to a random value, and incrementing for each subsequent request.
        // The second byte is incremented by the data logging stick for every response sent (either to Solarman Cloud or
        // local requests).
        // Note: the increment part is not implemented yet
        byte[] serial = new byte[] { (byte) 0x00, (byte) 0x00 };

        // (four bytes) – Serial number of Solarman data logging stick
        byte[] loggerSerial = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                .putInt((int) Long.parseUnsignedLong(solarmanLoggerConfiguration.getSerialNumber())).array();

        // Append all fields into the header
        return ByteBuffer
                .allocate(start.length + length.length + controlCode.length + serial.length + loggerSerial.length)
                .put(start).put(length).put(controlCode).put(serial).put(loggerSerial).array();
    }

    protected byte[] buildSolarmanV5FrameRequestPayload(byte mbFunctionCode, int firstReg, int lastReg) {
        // (one byte) – Denotes the frame type.
        byte[] frameType = new byte[] { 0x02 };
        // (two bytes) – Denotes the sensor type.
        byte[] sensorType = new byte[] { 0x00, 0x00 };
        // (four bytes) – Denotes the frame total working time. See corresponding response field of same name for
        // further details.
        byte[] totalWorkingTime = new byte[] { 0x00, 0x00, 0x00, 0x00 };
        // (four bytes) – Denotes the frame power on time.
        byte[] powerOnTime = new byte[] { 0x00, 0x00, 0x00, 0x00 };
        // Denotes the frame offset time.
        byte[] offsetTime = new byte[] { 0x00, 0x00, 0x00, 0x00 };
        // (variable length) – Modbus RTU request frame.
        byte[] requestFrame = buildModbusReadHoldingRegistersRequestFrame((byte) 0x01, mbFunctionCode, firstReg,
                lastReg);

        return ByteBuffer
                .allocate(frameType.length + sensorType.length + totalWorkingTime.length + powerOnTime.length
                        + offsetTime.length + requestFrame.length)
                .put(frameType).put(sensorType).put(totalWorkingTime).put(powerOnTime).put(offsetTime).put(requestFrame)
                .array();
    }

    /**
     * Based on <a href="https://www.modbustools.com/modbus.html#function03">Function 03 (03hex) Read Holding
     * Registers</a>
     *
     * @param slaveId - Slave Address
     * @param mbFunctionCode -
     * @param firstReg - Starting Address
     * @param lastReg - Ending Address
     * @return byte array containing the Modbus request frame
     */
    protected byte[] buildModbusReadHoldingRegistersRequestFrame(byte slaveId, byte mbFunctionCode, int firstReg,
            int lastReg) {
        int regCount = lastReg - firstReg + 1;
        byte[] req = ByteBuffer.allocate(6).put(slaveId).put(mbFunctionCode).putShort((short) firstReg)
                .putShort((short) regCount).array();
        byte[] crc = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) CRC16Modbus.calculate(req)).array();

        return ByteBuffer.allocate(req.length + crc.length).put(req).put(crc).array();
    }

    protected Map<Integer, byte[]> parseModbusReadHoldingRegistersResponse(byte @Nullable [] frame, int firstReg,
            int lastReg) throws SolarmanProtocolException {
        int regCount = lastReg - firstReg + 1;
        Map<Integer, byte[]> registers = new HashMap<>();
        int expectedFrameDataLen = 2 + 1 + regCount * 2;
        if (frame == null || frame.length < expectedFrameDataLen + 2) {
            throw new SolarmanProtocolException("Modbus frame is too short or empty");
        }

        int actualCrc = ByteBuffer.wrap(frame, expectedFrameDataLen, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()
                & 0xFFFF;
        int expectedCrc = CRC16Modbus.calculate(Arrays.copyOfRange(frame, 0, expectedFrameDataLen));

        if (actualCrc != expectedCrc) {
            throw new SolarmanProtocolException(
                    String.format("Modbus frame crc is not valid. Expected %04x, got %04x", expectedCrc, actualCrc));
        }

        for (int i = 0; i < regCount; i++) {
            int p1 = 3 + (i * 2);
            ByteBuffer order = ByteBuffer.wrap(frame, p1, 2).order(ByteOrder.BIG_ENDIAN);
            byte[] array = new byte[] { order.get(), order.get() };
            registers.put(i + firstReg, array);
        }

        return registers;
    }

    protected byte[] extractModbusResponseFrame(byte @Nullable [] responseFrame, byte[] requestFrame)
            throws SolarmanException {
        if (responseFrame == null || responseFrame.length == 0) {
            throw new SolarmanProtocolException("No response frame");
        } else if (responseFrame.length == 29) {
            parseResponseErrorCode(responseFrame, requestFrame);
            throw new IllegalStateException("This should never be reached as previous method should always throw");
        } else if (responseFrame.length < (29 + 4)) {
            throw new SolarmanProtocolException("Response frame is too short");
        } else if (responseFrame[0] != (byte) 0xA5) {
            throw new SolarmanProtocolException("Response frame has invalid starting byte");
        } else if (responseFrame[responseFrame.length - 1] != (byte) 0x15) {
            throw new SolarmanProtocolException("Response frame has invalid ending byte");
        }

        return Arrays.copyOfRange(responseFrame, 25, responseFrame.length - 2);
    }

    protected void parseResponseErrorCode(byte[] responseFrame, byte[] requestFrame) throws SolarmanException {
        if (responseFrame[0] == (byte) 0xA5 && responseFrame[1] == (byte) 0x10
                && !Arrays.equals(Arrays.copyOfRange(responseFrame, 7, 11), Arrays.copyOfRange(requestFrame, 7, 11))) {
            String requestInverterId = parseInverterId(requestFrame);
            String responseInverterId = parseInverterId(responseFrame);

            String message = String
                    .format("There was a mismatch between the request logger ID: %s and the response logger ID: %s. "
                            + "Make sure you are using the logger ID and not the inverter ID. "
                            + "If in doubt, try the one in the response", requestInverterId, responseInverterId);

            throw new SolarmanAuthenticationException(message, requestInverterId, responseInverterId);
        }

        if (responseFrame[1] != (byte) 0x10 || responseFrame[2] != (byte) 0x45) {
            throw new SolarmanProtocolException("Unexpected control code in error response frame");
        }

        int errorCode = responseFrame[25];

        String message = switch (errorCode) {
            case 0x01 -> "Error response frame: Illegal Function";
            case 0x02 -> "Error response frame: Illegal Data Address";
            case 0x03 -> "Error response frame: Illegal Data Value";
            case 0x04 -> "Error response frame: Slave Device Failure";
            default -> String.format("Error response frame: Unknown error code %02x", errorCode);
        };
        throw new SolarmanProtocolException(message);
    }

    private static String parseInverterId(byte[] requestFrame) {
        byte[] inverterIdBytes = Arrays.copyOfRange(requestFrame, 7, 11);
        int inverterIdInt = ByteBuffer.wrap(inverterIdBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return String.valueOf(inverterIdInt & 0x00000000ffffffffL);
    }
}
