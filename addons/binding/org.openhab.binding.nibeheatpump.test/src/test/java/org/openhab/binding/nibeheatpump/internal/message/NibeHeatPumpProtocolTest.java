/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeheatpump.internal.message;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.util.HexUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolContext;
import org.openhab.binding.nibeheatpump.internal.protocol.NibeHeatPumpProtocolDefaultContext;

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

    @Before
    public void Before() {
        ackRequestCount = 0;
        nakRequestCount = 0;
        sendWriteMsgCount = 0;
        sendReadMsgCount = 0;
        receivedMsgs = new ArrayList<>();
        mockupContext.buffer().clear();
        mockupContext.msg().clear();
    }

    @Test(timeout = 1000)
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
                + "5C00206850449C2600489CF6004C9CF1004E9CD6014D9C0C024F9C4500509C3F00519CF100529C0401569CD500C9AF000001A80C01FDA799FAFAA9020098A91A1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000C5";
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

        assertEquals(7, ackRequestCount);
        assertEquals(2, nakRequestCount);
        assertEquals(1, sendWriteMsgCount);
        assertEquals(1, sendReadMsgCount);
        assertEquals(7, receivedMsgs.size());

        String expect;

        expect = "5C001962189600E1010200000000800000000000020914340001000005B8";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(0));

        expect = "5C00206850449C9600489C88014C9C2D014E9CCF004D9CE0014F9C3200509C0400519C8201529C6B02569C3E00C9AF000001A8F600FDA77E02FAA90F0098A9DC27FFFF0000A0A93A04FFFF00009CA9FD19FFFF000081";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(1));

        expect = "5C001962189600DF01020000000080000000000002091434000100000586";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(2));

        expect = "5C0019600079";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(3));

        expect = "5C00206851449C2500489CFC004C9CF1004E9CC7014D9C0B024F9C2500509C3300519C0B01529C5C5C01569C3100C9AF000001A80C01FDA716FAFAA9070098A91B1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000BE";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(4));

        expect = "5C00206852449C2500489CFE004C9CF2004E9CD4014D9CFB014F9C2500509C3700519C0D01529C5C5C01569C3200C9AF000001A80C01FDA712FAFAA9070098A95C5C1BFFFF0000A0A9D102FFFF00009CA9B412FFFF00007F";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(5));

        expect = "5C00206850449C2600489CF6004C9CF1004E9CD6014D9C0C024F9C4500509C3F00519CF100529C0401569CD500C9AF000001A80C01FDA799FAFAA9020098A91A1BFFFF0000A0A9CA02FFFF00009CA99212FFFF0000C5";
        Assert.assertArrayEquals(HexUtils.hexToBytes(expect), receivedMsgs.get(6));
    }
}
