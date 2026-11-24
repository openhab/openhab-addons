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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException.Reason;

/**
 * Tests {@link ModbusFrame}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class ModbusFrameTest {

    private static final HexFormat HEX = HexFormat.of();

    @Test
    void requestsMatchTheFramesUsedByTheBattery() {
        assertEquals("010300000066c5e0", HEX.formatHex(ModbusFrame.readHoldingRegisters(0x0000, 102)));
        assertEquals("01030500001984cc", HEX.formatHex(ModbusFrame.readHoldingRegisters(0x0500, 25)));
        assertEquals("010305510001d517", HEX.formatHex(ModbusFrame.readHoldingRegisters(0x0551, 1)));
        assertEquals("01030558004104e5", HEX.formatHex(ModbusFrame.readHoldingRegisters(0x0558, 0x41)));
        assertEquals("010305a8004104d6", HEX.formatHex(ModbusFrame.readHoldingRegisters(0x05A8, 0x41)));
        assertEquals("0110055000020400018100f853",
                HEX.formatHex(ModbusFrame.writeMultipleRegisters(0x0550, 1, 0x8100)));
        assertEquals("01100550000204000281000853",
                HEX.formatHex(ModbusFrame.writeMultipleRegisters(0x0550, 2, 0x8100)));
        assertEquals("011005a000020400008100a6d7",
                HEX.formatHex(ModbusFrame.writeMultipleRegisters(0x05A0, 0, 0x8100)));
    }

    @Test
    void crcOfACompleteFrameIsZero() {
        byte[] frame = Fixtures.frame("status");
        assertEquals(0, ModbusFrame.crc(frame, frame.length));
    }

    @Test
    void expectedLengthFollowsTheFunctionCode() throws BydCommunicationException {
        byte[] read = Fixtures.frame("status");
        assertEquals(ModbusFrame.LENGTH_UNKNOWN, ModbusFrame.expectedLength(read, 1));
        assertEquals(ModbusFrame.LENGTH_UNKNOWN, ModbusFrame.expectedLength(read, 2));
        assertEquals(55, ModbusFrame.expectedLength(read, 3));
        assertEquals(8, ModbusFrame.expectedLength(Fixtures.frame("measurement-start-echo"), 2));
        assertEquals(5, ModbusFrame.expectedLength(Fixtures.frame("exception-not-ready"), 2));

        BydCommunicationException e = assertThrows(BydCommunicationException.class,
                () -> ModbusFrame.expectedLength(new byte[] { 1, 0x06 }, 2));
        assertEquals(Reason.PROTOCOL, e.getReason());
    }

    @Test
    void validReplyIsAccepted() {
        assertDoesNotThrow(() -> ModbusFrame.validateReply(ModbusFrame.readHoldingRegisters(0x0500, 25),
                Fixtures.frame("status")));
        assertDoesNotThrow(() -> ModbusFrame.validateReply(ModbusFrame.writeMultipleRegisters(0x0550, 1, 0x8100),
                Fixtures.frame("measurement-start-echo")));
    }

    @Test
    void exception4MeansNotReady() {
        BydCommunicationException e = assertThrows(BydCommunicationException.class, () -> ModbusFrame
                .validateReply(ModbusFrame.readHoldingRegisters(0x0558, 0x41), Fixtures.frame("exception-not-ready")));
        assertEquals(Reason.NOT_READY, e.getReason());
    }

    @Test
    void otherExceptionsAreProtocolErrors() {
        byte[] exception = new byte[] { 0x01, (byte) 0x83, 0x02, 0, 0 };
        int crc = ModbusFrame.crc(exception, 3);
        exception[3] = (byte) crc;
        exception[4] = (byte) (crc >>> 8);
        assertProtocolError(ModbusFrame.readHoldingRegisters(0x0500, 25), exception);
    }

    @Test
    void corruptedReplyIsRejected() {
        byte[] reply = Fixtures.frame("status");
        reply[10] ^= 0x01;
        assertProtocolError(ModbusFrame.readHoldingRegisters(0x0500, 25), reply);
    }

    @Test
    void replyToADifferentRequestIsRejected() {
        // the status reply has 50 data bytes, a read of 102 registers expects 204
        assertProtocolError(ModbusFrame.readHoldingRegisters(0x0000, 102), Fixtures.frame("status"));
        // the echo of tower 1 does not answer a write for the event log
        assertProtocolError(ModbusFrame.writeMultipleRegisters(0x05A0, 1, 0x8100),
                Fixtures.frame("measurement-start-echo"));
        // a write echo does not answer a read
        assertProtocolError(ModbusFrame.readHoldingRegisters(0x0500, 25), Fixtures.frame("measurement-start-echo"));
    }

    private static void assertProtocolError(byte[] request, byte[] reply) {
        BydCommunicationException e = assertThrows(BydCommunicationException.class,
                () -> ModbusFrame.validateReply(request, reply));
        assertEquals(Reason.PROTOCOL, e.getReason());
    }
}
