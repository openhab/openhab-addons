/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nibeheatpump.internal.message;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolContext;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolDefaultContext;
import org.openhab.core.util.HexUtils;

/**
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpProtocolTest {

    int ackRequestCount = 0;
    int nakRequestCount = 0;
    int sendWriteMsgCount = 0;
    int sendReadMsgCount = 0;
    List<byte[]> receivedMsgs = null;

    final NibeHeatPumpProtocolContext mockupContext = new NibeHeatPumpProtocolDefaultContext() {
        @Override
        public void sendAck() {
            ackRequestCount++;
        }

        @Override
        public void sendNak() {
            nakRequestCount++;
        }

        @Override
        public void msgReceived(byte[] data) {
            receivedMsgs.add(Arrays.copyOf(data, data.length));
        }

        @Override
        public void sendWriteMsg() {
            sendWriteMsgCount++;
        }

        @Override
        public void sendReadMsg() {
            sendReadMsgCount++;
        }
    };

    @BeforeEach
    public void Before() {
        ackRequestCount = 0;
        nakRequestCount = 0;
        sendWriteMsgCount = 0;
        sendReadMsgCount = 0;
        receivedMsgs = new ArrayList<>();
        mockupContext.buffer().clear();
        mockupContext.msg().clear();
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    public void test() {
        //@formatter:off
        final String strTestData =
                // RMU40 message, acknowledge should be send
                "5C001962189600E1010200000000800000000000020914340001000005B8"
                // RMU40 message, CRC failure, negative acknowledge should be send
                + "5C001962189600E1010200000000800000000000020914340001000005B9"
                // MODBUS40 write request
                + "5C00206B004B"
                // nonsense
                + "3EAABB"
                // MODBUS40 read request
                + "5C0020690049"
                // nonsense
                + "F0561939F6"
                // MODBUS40 data read out, acknowledge should be send
                + "5C00206850449C9600489C88014C9C2D014E9CCF004D9CE0014F9C3200509C0400519C8201529C6B02569C3E00C9AF000001A8F600FDA77E02FAA90F0098A9DC27FFFF0000A0A93A04FFFF00009CA9FD19FFFF000081"
                // nonsense
                + "F0349823"
                // MODBUS40 data read out, CRC failure, negative acknowledge should be send
                + "5C00206850449C9600489C88014C9C2D014E9CCF004D9CE0014F9C3200509C0400519C8201529C6B02569C3E00C9AF000001A8F600FDA77E02FAA90F0098A9DC27FFFF0000A0A93A04FFFF00009CA9FD19FFFF000080"
                // RMU40 message, acknowledge should be send
                + "5C001962189600DF01020000000080000000000002091434000100000586"
                // nonsense
                + "123490"
                // unknown RMU40 message, acknowledge should be send
                + "5C0019600079"
                // MODBUS40 data read out, special len, acknowledge should be send
                + "5C00206851449C2500489CFC004C9CF1004E9CC7014D9C0B024F9C2500509C3300519C0B01529C5C5C01569C3100C9AF000001A80C01FDA716FAFAA9070098A91B1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000BE"
                // MODBUS40 data read out, special len, acknowledge should be send
                + "5C00206852449C2500489CFE004C9CF2004E9CD4014D9CFB014F9C2500509C3700519C0D01529C5C5C01569C3200C9AF000001A80C01FDA712FAFAA9070098A95C5C1BFFFF0000A0A9D102FFFF00009CA9B412FFFF00007F"
                // MODBUS40 data read out, special checksum, acknowledge should be send
                + "5C00206850449C2600489CF6004C9CF1004E9CD6014D9C0C024F9C4500509C3F00519CF100529C0401569CD500C9AF000001A80C01FDA799FAFAA9020098A91A1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000C5"
                // 16-bit address (e.g. model F2120 heatpumps), acknowledge should be send
                + "5C41C9F7007F";
        //@formatter:on

        // create byte data from hex string
        final byte[] rawData = HexUtils.hexToBytes(strTestData);

        // put byte data to protocol state machine
        mockupContext.buffer().put(rawData);
        mockupContext.buffer().flip();

        // run protocol state machine to process test data
        while (mockupContext.state().process(mockupContext)) {
        }

        // test results

        assertEquals(8, ackRequestCount);
        assertEquals(2, nakRequestCount);
        assertEquals(1, sendWriteMsgCount);
        assertEquals(1, sendReadMsgCount);
        assertEquals(8, receivedMsgs.size());

        String expect;

        expect = "5C001962189600E1010200000000800000000000020914340001000005B8";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(0));

        expect = "5C00206850449C9600489C88014C9C2D014E9CCF004D9CE0014F9C3200509C0400519C8201529C6B02569C3E00C9AF000001A8F600FDA77E02FAA90F0098A9DC27FFFF0000A0A93A04FFFF00009CA9FD19FFFF000081";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(1));

        expect = "5C001962189600DF01020000000080000000000002091434000100000586";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(2));

        expect = "5C0019600079";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(3));

        expect = "5C00206851449C2500489CFC004C9CF1004E9CC7014D9C0B024F9C2500509C3300519C0B01529C5C5C01569C3100C9AF000001A80C01FDA716FAFAA9070098A91B1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000BE";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(4));

        expect = "5C00206852449C2500489CFE004C9CF2004E9CD4014D9CFB014F9C2500509C3700519C0D01529C5C5C01569C3200C9AF000001A80C01FDA712FAFAA9070098A95C5C1BFFFF0000A0A9D102FFFF00009CA9B412FFFF00007F";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(5));

        expect = "5C00206850449C2600489CF6004C9CF1004E9CD6014D9C0C024F9C4500509C3F00519CF100529C0401569CD500C9AF000001A80C01FDA799FAFAA9020098A91A1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000C5";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(6));

        expect = "5C41C9F7007F";
        assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(7));
    }
}
