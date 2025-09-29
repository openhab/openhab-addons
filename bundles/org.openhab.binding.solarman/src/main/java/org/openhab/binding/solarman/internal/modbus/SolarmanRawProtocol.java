/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;

/**
 * @author Catalin Sanda - Initial contribution
 * @author Peter Kretz - Added RAW Modbus for LAN Stick
 */
@NonNullByDefault
public class SolarmanRawProtocol implements SolarmanProtocol {
    private final SolarmanLoggerConfiguration solarmanLoggerConfiguration;

    public SolarmanRawProtocol(SolarmanLoggerConfiguration solarmanLoggerConfiguration) {
        this.solarmanLoggerConfiguration = solarmanLoggerConfiguration;
    }

    @Override
    public Map<Integer, byte[]> readRegisters(SolarmanLoggerConnection solarmanLoggerConnection, byte mbFunctionCode,
            int firstReg, int lastReg) throws SolarmanException {
        int regCount = lastReg - firstReg + 1;
        byte[] solarmanRawFrame = buildSolarmanRawReadFrame(mbFunctionCode, firstReg, regCount);
        byte[] respFrame = solarmanLoggerConnection.sendRequest(solarmanRawFrame);
        if (respFrame.length > 0) {
            byte[] modbusRespFrame = extractModbusRawResponseFrame(respFrame, solarmanRawFrame);
            return parseRawModbusReadHoldingRegistersResponse(modbusRespFrame, firstReg, lastReg);
        } else {
            throw new SolarmanConnectionException("Response frame was empty");
        }
    }

    @Override
    public boolean writeRegisters(SolarmanLoggerConnection solarmanLoggerConnection, int firstReg, byte[] data)
            throws SolarmanException {
        if (data.length % 2 != 0) {
            throw new SolarmanException("Data to be written should be packed as two bytes per register!");
        }

        byte[] solarmanRawFrame = buildSolarmanRawWriteFrame(firstReg, data);
        byte[] respFrame = solarmanLoggerConnection.sendRequest(solarmanRawFrame);
        if (respFrame.length > 0) {
            byte[] modbusRespFrame = extractModbusRawResponseFrame(respFrame, solarmanRawFrame);
            parseRawModbusWriteHoldingRegistersResponse(modbusRespFrame, data);
            return true;
        } else {
            throw new SolarmanConnectionException("Response frame was empty");
        }
    }

    /**
     * Builds a SolarMAN Raw frame to request data write from firstReg.
     * Frame format is based on
     * <a href="https://github.com/StephanJoubert/home_assistant_solarman/issues/247">Solarman RAW Protocol</a>
     *
     * @param firstReg - the start register
     * @param data - the data to be written
     * @return byte array containing the Solarman Raw frame
     */
    private byte[] buildSolarmanRawWriteFrame(int firstReg, byte[] data) throws SolarmanException {
        byte[] requestPayload = buildSolarmanRawWriteFrameRequestPayload(DEFAULT_SLAVE_ID, firstReg, data);
        byte[] header = buildSolarmanRawFrameHeader(requestPayload.length);

        return ByteBuffer.allocate(header.length + requestPayload.length).put(header).put(requestPayload).array();
    }

    /**
     * Builds a SolarMAN Raw write frame payload
     * Frame format is based on
     * <a href="https://www.modbustools.com/modbus.html#function16">Modbus RTU Write Multiple Registers</a>
     *
     * @param slaveId - Modbus slave ID
     * @param firstReg - the start register
     * @param data - the data to be written
     * @return byte array containing the Modbus RTU Raw frame payload
     */
    private byte[] buildSolarmanRawWriteFrameRequestPayload(byte slaveId, int firstReg, byte[] data)
            throws SolarmanException {
        if (data.length % 2 != 0) {
            throw new SolarmanException("Data to be written should be packed as two bytes per register!");
        }

        // slaveId (1 byte)
        // mbFunction (1 byte)
        // firstRegister (2 bytes)
        // registerCount (2 bytes)
        // data length (1 byte)
        // data
        int bufferSize = 1 + 1 + 2 + 2 + 1 + data.length;
        int registerCount = data.length / 2;

        byte[] req = ByteBuffer.allocate(bufferSize).put(slaveId).put(WRITE_REGISTERS_FUNCTION_CODE)
                .putShort((short) firstReg).putShort((short) registerCount).put((byte) data.length).put(data).array();
        byte[] crc = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) CRC16Modbus.calculate(req)).array();

        return ByteBuffer.allocate(req.length + crc.length).put(req).put(crc).array();
    }

    protected byte[] extractModbusRawResponseFrame(byte @Nullable [] responseFrame, byte[] requestFrame)
            throws SolarmanException {
        if (responseFrame == null || responseFrame.length == 0) {
            throw new SolarmanProtocolException("No response frame");
        } else if (responseFrame.length < 11) {
            throw new SolarmanProtocolException("Response frame is too short");
        } else if (responseFrame[0] != (byte) 0x03) {
            throw new SolarmanProtocolException("Response frame has invalid starting byte");
        }

        return Arrays.copyOfRange(responseFrame, 6, responseFrame.length);
    }

    protected Map<Integer, byte[]> parseRawModbusReadHoldingRegistersResponse(byte @Nullable [] frame, int firstReg,
            int lastReg) throws SolarmanProtocolException {
        int regCount = lastReg - firstReg + 1;
        Map<Integer, byte[]> registers = new HashMap<>();
        int expectedFrameDataLen = 2 + 1 + regCount * 2;
        if (frame == null || frame.length < expectedFrameDataLen) {
            throw new SolarmanProtocolException("Modbus frame is too short or empty");
        }

        for (int i = 0; i < regCount; i++) {
            int p1 = 3 + (i * 2);
            ByteBuffer order = ByteBuffer.wrap(frame, p1, 2).order(ByteOrder.BIG_ENDIAN);
            byte[] array = new byte[] { order.get(), order.get() };
            registers.put(i + firstReg, array);
        }

        return registers;
    }

    private void parseRawModbusWriteHoldingRegistersResponse(byte[] frame, byte[] data)
            throws SolarmanProtocolException {
        int expectedRegistersCount = data.length / 2;

        // slaveId (1 byte)
        // modbusFunction (1 byte)
        // firstRegister (2 bytes)
        // registerCount (2 bytes)
        int expectedFrameDataLen = 1 + 1 + 2 + 2;
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

        short registersWrittenCount = ByteBuffer.wrap(frame, 4, 2).getShort();
        if (registersWrittenCount != expectedRegistersCount) {
            throw new SolarmanProtocolException(
                    String.format("Modbus written registers count is not valid. Expected %04x, got %04x",
                            expectedRegistersCount, registersWrittenCount));
        }
    }

    /**
     * Builds a SolarMAN Raw frame to request data from firstReg to lastReg.
     * Frame format is based on
     * <a href="https://github.com/StephanJoubert/home_assistant_solarman/issues/247">Solarman RAW Protocol</a>
     * Request send:
     * Header 03e8: Transaction identifier
     * Header 0000: Protocol identifier
     * Header 0006: Message length (w/o CRC)
     * Payload 01: Slave ID
     * Payload 03: Read function
     * Payload 0003: 1st register address
     * Payload 006e: Nb of registers to read
     * Trailer 3426: CRC-16 ModBus
     *
     * @param mbFunctionCode - Modbus function code
     * @param firstReg - the start register
     * @param regCount - the registers count
     * @return byte array containing the Solarman Raw frame
     */
    protected byte[] buildSolarmanRawReadFrame(byte mbFunctionCode, int firstReg, int regCount) {
        byte[] requestPayload = buildSolarmanRawReadFrameRequestPayload(mbFunctionCode, firstReg, regCount);
        byte[] header = buildSolarmanRawFrameHeader(requestPayload.length);

        return ByteBuffer.allocate(header.length + requestPayload.length).put(header).put(requestPayload).array();
    }

    /**
     * Builds a SolarMAN Raw frame Header
     * Frame format is based on
     * <a href="https://github.com/StephanJoubert/home_assistant_solarman/issues/247">Solarman RAW Protocol</a>
     * Request send:
     * Header 03e8: Transaction identifier
     * Header 0000: Protocol identifier
     * Header 0006: Message length (w/o CRC)
     *
     * @param payloadSize th
     * @return byte array containing the Solarman Raw frame header
     */
    private byte[] buildSolarmanRawFrameHeader(int payloadSize) {
        // (two byte) Denotes the start of the Raw frame. Always 0x03 0xE8.
        byte[] transactionId = new byte[] { (byte) 0x03, (byte) 0xE8 };

        // (two bytes) – Always 0x00 0x00
        byte[] protocolId = new byte[] { (byte) 0x00, (byte) 0x00 };

        // (two bytes) Payload length
        byte[] messageLength = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.BIG_ENDIAN)
                .putShort((short) payloadSize).array();

        // Append all fields into the header
        return ByteBuffer.allocate(transactionId.length + protocolId.length + messageLength.length).put(transactionId)
                .put(protocolId).put(messageLength).array();
    }

    /**
     * Builds a SolarMAN Raw read frame payload
     * Frame format is based on
     * <a href="https://github.com/StephanJoubert/home_assistant_solarman/issues/247">Solarman RAW Protocol</a>
     * Request send:
     * Payload 01: Slave ID
     * Payload 03: Read function
     * Payload 0003: 1st register address
     * Payload 006e: Nb of registers to read
     * Trailer 3426: CRC-16 ModBus
     *
     * @param mbFunctionCode - Modbus function code
     * @param firstReg - the start register
     * @param regCount - the registers count
     * @return byte array containing the Solarman Raw frame payload
     */
    protected byte[] buildSolarmanRawReadFrameRequestPayload(byte mbFunctionCode, int firstReg, int regCount) {
        byte[] req = ByteBuffer.allocate(6).put((byte) 0x01).put(mbFunctionCode).putShort((short) firstReg)
                .putShort((short) regCount).array();
        byte[] crc = ByteBuffer.allocate(Short.BYTES).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) CRC16Modbus.calculate(req)).array();

        return ByteBuffer.allocate(req.length + crc.length).put(req).put(crc).array();
    }
}
