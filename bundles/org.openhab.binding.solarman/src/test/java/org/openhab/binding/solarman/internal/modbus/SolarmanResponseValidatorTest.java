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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanProtocolException;

/**
 * Tests write acknowledgements and strict envelope validation with generated protocol responses.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SolarmanResponseValidatorTest {
    @Test
    void rejectsWrongSlaveFunctionAddressCountAndCrc() throws Exception {
        byte[] valid = crc(new byte[] { 1, 16, 1, 18, 0, 1 });
        SolarmanResponseValidator.validateWrite(valid, 274, 1, true);
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateWrite(valid, 275, 1, true));
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateWrite(valid, 274, 2, true));
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateWrite(crc(new byte[] { 2, 16, 1, 18, 0, 1 }), 274, 1, true));
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateWrite(crc(new byte[] { 1, 3, 1, 18, 0, 1 }), 274, 1, true));
        valid[6] ^= 1;
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateWrite(valid, 274, 1, true));
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateWrite(new byte[] { 1, (byte) 0x90, 2 }, 274, 1, true));
        assertThrows(IllegalArgumentException.class,
                () -> SolarmanResponseValidator.validateWriteRequest(65535, new byte[4]));
        assertThrows(IllegalArgumentException.class,
                () -> SolarmanResponseValidator.validateWriteRequest(0, new byte[1]));
    }

    @Test
    void strictV5SupportsConfiguredSlaveAndRejectsCorruptedEnvelopes() throws Exception {
        SolarmanLoggerConfiguration configuration = new SolarmanLoggerConfiguration();
        configuration.serialNumber = "1234567890";
        configuration.slaveId = 7;
        SolarmanV5Protocol protocol = new SolarmanV5Protocol(configuration, true);
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.sendRequest(any())).thenAnswer(call -> {
            byte[] request = call.getArgument(0);
            assertEquals(7, request[26]);
            return envelope(request, crc(new byte[] { 7, 16, 1, 18, 0, 1 }));
        });
        assertTrue(protocol.writeRegisters(connection, 274, new byte[] { 0, 1 }));
        doAnswer(call -> {
            byte[] response = envelope(call.getArgument(0), crc(new byte[] { 7, 16, 1, 18, 0, 1 }));
            response[response.length - 2] ^= 1;
            return response;
        }).when(connection).sendRequest(any());
        assertThrows(SolarmanProtocolException.class,
                () -> protocol.writeRegisters(connection, 274, new byte[] { 0, 1 }));
    }

    @Test
    void strictReadChecksByteCountAndNativeTcpAcknowledgementsNeedNoRtuCrc() throws Exception {
        SolarmanResponseValidator.validateRead(crc(new byte[] { 1, 3, 2, 0, 10 }), 3, 1, true);
        assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateRead(crc(new byte[] { 1, 3, 2, 0, 10 }), 3, 2, true));
        SolarmanLoggerConfiguration configuration = new SolarmanLoggerConfiguration();
        configuration.slaveId = 12;
        SolarmanRawProtocol protocol = new SolarmanRawProtocol(configuration, true);
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.sendRequest(any())).thenAnswer(call -> {
            byte[] request = call.getArgument(0);
            assertEquals(12, request[6]);
            return new byte[] { 3, (byte) 232, 0, 0, 0, 6, 12, 16, 1, 18, 0, 1 };
        });
        assertTrue(protocol.writeRegisters(connection, 274, new byte[] { 0, 1 }));
    }

    private static byte[] crc(byte[] data) {
        return ByteBuffer.allocate(data.length + 2).put(data).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) CRC16Modbus.calculate(data)).array();
    }

    @Test
    void strictReadsRejectStaleSequencesAndDecodeShortExceptionFrames() throws Exception {
        SolarmanLoggerConfiguration configuration = new SolarmanLoggerConfiguration();
        configuration.serialNumber = "1234567890";
        SolarmanV5Protocol protocol = new SolarmanV5Protocol(configuration, true);
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        doAnswer(call -> envelope(call.getArgument(0), crc(new byte[] { 1, 3, 4, 0, 10, 0, 20 }))).when(connection)
                .sendRequest(any());
        var registers = protocol.readRegisters(connection, (byte) 3, 100, 101);
        assertArrayEquals(new byte[] { 0, 10 }, registers.get(100));
        assertArrayEquals(new byte[] { 0, 20 }, registers.get(101));
        doAnswer(call -> {
            byte[] request = call.getArgument(0);
            assertEquals(1, request[5]);
            byte[] stale = request.clone();
            stale[5] = 0;
            return envelope(stale, crc(new byte[] { 1, 3, 4, 0, 10, 0, 20 }));
        }).when(connection).sendRequest(any());
        assertTrue(message(assertThrows(SolarmanProtocolException.class,
                () -> protocol.readRegisters(connection, (byte) 3, 100, 101)))
                .contains("sequence mismatch: expected=0x01, received=0x00"));
        doAnswer(call -> envelope(call.getArgument(0), crc(new byte[] { 1, (byte) 0x83, 2 }))).when(connection)
                .sendRequest(any());
        assertTrue(message(assertThrows(SolarmanProtocolException.class,
                () -> protocol.readRegisters(connection, (byte) 3, 100, 101))).contains("exception response: 2"));
        SolarmanRawProtocol raw = new SolarmanRawProtocol(configuration, true);
        doReturn(new byte[] { 3, (byte) 232, 0, 0, 0, 3, 1, (byte) 0x83, 2 }).when(connection).sendRequest(any());
        assertTrue(message(
                assertThrows(SolarmanProtocolException.class, () -> raw.readRegisters(connection, (byte) 3, 100, 101)))
                .contains("exception response: 2"));
    }

    @ParameterizedTest
    @CsvSource({ "empty,too short", "truncated,too short", "start,start marker mismatch", "end,end marker mismatch",
            "control-low,control code mismatch", "control-high,control code mismatch", "sequence,sequence mismatch",
            "serial,logger serial mismatch", "length,length mismatch", "checksum,checksum mismatch" })
    void identifiesTheRejectedV5FieldAndExpectedReceivedValues(String field, String diagnostic) {
        byte[] request = new byte[11];
        request[5] = (byte) 0xA5;
        ByteBuffer.wrap(request, 7, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(0xFEDCBA98);
        byte[] response = envelope(request, crc(new byte[] { 1, 16, 1, 18, 0, 1 }));
        switch (field) {
            case "empty" -> response = new byte[0];
            case "truncated" -> response = Arrays.copyOf(response, 26);
            case "start" -> response[0] = 0;
            case "end" -> response[response.length - 1] = 0;
            case "control-low" -> response[3] ^= 1;
            case "control-high" -> response[4] ^= 1;
            case "sequence" -> response[5] ^= 1;
            case "serial" -> response[7] ^= 1;
            case "length" -> response[1]++;
            case "checksum" -> response[response.length - 2] ^= 1;
            default -> fail("Unknown field");
        }
        byte[] rejected = response;
        String detail = message(assertThrows(SolarmanProtocolException.class,
                () -> SolarmanResponseValidator.validateEnvelope(rejected, request, true)));
        assertTrue(detail.contains(diagnostic), detail);
        assertTrue(detail.contains("expected"), detail);
        assertTrue(detail.contains("received"), detail);
        if ("sequence".equals(field)) {
            assertTrue(detail.contains("expected=0xA5, received=0xA4"), detail);
        } else if ("serial".equals(field)) {
            assertTrue(detail.contains("expected=4275878552, received=4275878553"), detail);
        }
    }

    @ParameterizedTest
    @CsvSource({ "read,false", "read,true", "write,false", "write,true" })
    void responseDiagnosticsPreserveOperationCauseAndSingleRequest(String operation, boolean reject) throws Exception {
        SolarmanLoggerConfiguration configuration = new SolarmanLoggerConfiguration();
        configuration.serialNumber = "1234567890";
        SolarmanV5Protocol protocol = new SolarmanV5Protocol(configuration, true);
        SolarmanLoggerConnection connection = mock(SolarmanLoggerConnection.class);
        when(connection.sendRequest(any())).thenAnswer(call -> {
            byte[] request = call.getArgument(0);
            byte[] response = envelope(request,
                    crc("write".equals(operation) ? new byte[] { 1, 16, 1, 18, 0, 1 } : new byte[] { 1, 3, 2, 0, 20 }));
            if (reject) {
                response[5] ^= 1;
            }
            return response;
        });
        if (reject) {
            SolarmanProtocolException failure = assertThrows(SolarmanProtocolException.class, () -> {
                if ("write".equals(operation)) {
                    protocol.writeRegisters(connection, 274, new byte[] { 0, 20 });
                } else {
                    protocol.readRegisters(connection, (byte) 3, 274, 274);
                }
            });
            assertTrue(message(failure).contains("V5 " + operation + " response rejected"));
            assertTrue(message(failure).contains("0x0112"));
            assertTrue(message(failure).contains("sequence mismatch: expected=0x00, received=0x01"));
            assertInstanceOf(SolarmanProtocolException.class, failure.getCause());
        } else if ("write".equals(operation)) {
            assertTrue(protocol.writeRegisters(connection, 274, new byte[] { 0, 20 }));
        } else {
            assertArrayEquals(new byte[] { 0, 20 }, protocol.readRegisters(connection, (byte) 3, 274, 274).get(274));
        }
        verify(connection).sendRequest(any());
        verifyNoMoreInteractions(connection);
    }

    @Test
    void loggerResponseSequenceByteRemainsIndependentOfTheEchoedRequestByte() throws Exception {
        byte[] request = new byte[11];
        request[5] = (byte) 0xFE;
        byte[] response = envelope(request, crc(new byte[] { 1, 3, 2, 0, 20 }));
        response[6] = (byte) 0xFF;
        int checksum = 0;
        for (int i = 1; i < response.length - 2; i++) {
            checksum += Byte.toUnsignedInt(response[i]);
        }
        response[response.length - 2] = (byte) checksum;
        SolarmanResponseValidator.validateEnvelope(response, request, true);
    }

    private static String message(SolarmanProtocolException exception) {
        return Objects.requireNonNull(exception.getMessage());
    }

    private static byte[] envelope(byte[] request, byte[] pdu) {
        byte[] response = new byte[25 + pdu.length + 2];
        response[0] = (byte) 165;
        ByteBuffer.wrap(response, 1, 2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) (response.length - 13));
        response[3] = 16;
        response[4] = 21;
        response[5] = request[5];
        System.arraycopy(request, 7, response, 7, 4);
        response[11] = 2;
        System.arraycopy(pdu, 0, response, 25, pdu.length);
        int checksum = 0;
        for (int i = 1; i < response.length - 2; i++) {
            checksum += Byte.toUnsignedInt(response[i]);
        }
        response[response.length - 2] = (byte) checksum;
        response[response.length - 1] = 21;
        return response;
    }
}
