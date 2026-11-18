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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.bydbatterybox.internal.FakeBattery;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;
import org.openhab.binding.bydbatterybox.internal.dto.TowerData;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException.Reason;

/**
 * Tests {@link BydClient} and {@link BydConnection} against a {@link FakeBattery} replaying captured frames.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@Timeout(value = 20, unit = TimeUnit.SECONDS)
class BydClientTest {

    private static final int TIMEOUT_MILLIS = 2000;
    private static final BydClient.Sleeper NO_SLEEP = millis -> {
    };

    @Test
    void identityAndStatusFromRepliesSplitIntoSmallSegments() throws Exception {
        try (FakeBattery battery = FakeBattery.start(7, Fixtures.frame("identity"), Fixtures.frame("status"));
                BydConnection connection = open(battery)) {
            BydClient client = new BydClient(connection, NO_SLEEP);
            assertEquals(3, client.readIdentity().modulesPerTower());
            assertEquals(94, client.readStatus().stateOfHealth());
        }
    }

    @Test
    void towerMeasurementWaitsUntilReady() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, Fixtures.frame("measurement-start-echo"),
                Fixtures.frame("measurement-state-pending"), Fixtures.frame("measurement-state-ready"),
                Fixtures.frame("measurement-1"), Fixtures.frame("measurement-2"), Fixtures.frame("measurement-3"),
                Fixtures.frame("measurement-4")); BydConnection connection = open(battery)) {
            // with 96 cells, there is no second pass even if requested
            TowerData tower = new BydClient(connection, NO_SLEEP).readTower(1, identity(), true);

            assertEquals(96, tower.cellVoltages().size());
            assertEquals(3098, tower.cellVoltages().get(4));
            List<byte[]> requests = battery.requests();
            assertEquals(7, requests.size());
            assertArrayEquals(ModbusFrame.writeMultipleRegisters(0x0550, 1, 0x8100), requests.get(0));
            assertArrayEquals(ModbusFrame.readHoldingRegisters(0x0551, 1), requests.get(1));
            assertArrayEquals(ModbusFrame.readHoldingRegisters(0x0551, 1), requests.get(2));
            assertArrayEquals(ModbusFrame.readHoldingRegisters(0x0558, 0x41), requests.get(6));
        }
    }

    @Test
    void towerMeasurementGivesUpIfNeverReady() throws Exception {
        byte[][] replies = new byte[31][];
        Arrays.fill(replies, Fixtures.frame("measurement-state-pending"));
        replies[0] = Fixtures.frame("measurement-start-echo");
        try (FakeBattery battery = FakeBattery.start(64, replies); BydConnection connection = open(battery)) {
            BydCommunicationException e = assertThrows(BydCommunicationException.class,
                    () -> new BydClient(connection, NO_SLEEP).readTower(1, identity(), false));
            assertEquals(Reason.NOT_READY, e.getReason());
            // first check after 1 s, then every 0.5 s up to 10 s
            assertEquals(1 + 19, battery.requests().size());
        }
    }

    @Test
    void secondPassReadsTheCellsBeyond128() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64,
                secondTowerReplies(Fixtures.frame("second-pass-echo"), Fixtures.frame("measurement-start-echo"),
                        Fixtures.frame("measurement-state-pending"), Fixtures.frame("measurement-state-ready"),
                        Fixtures.frame("hvs2x5-t2-second-pass-1"), Fixtures.frame("hvs2x5-t2-second-pass-2")));
                BydConnection connection = open(battery)) {
            TowerData tower = new BydClient(connection, NO_SLEEP).readTower(2, twoTowerIdentity(), true);

            assertEquals(160, tower.cellVoltages().size());
            List<byte[]> requests = battery.requests();
            assertEquals(6 + 6, requests.size());
            assertArrayEquals(ModbusFrame.writeMultipleRegisters(0x0550, 2, 0x8100), requests.get(0));
            assertArrayEquals(HexFormat.of().parseHex("01100100000306444542554700176f"), requests.get(6));
            assertArrayEquals(ModbusFrame.writeMultipleRegisters(0x0550, 2, 0x8100), requests.get(7));
            assertArrayEquals(ModbusFrame.readHoldingRegisters(0x0558, 0x41), requests.get(11));
        }
    }

    @Test
    void secondPassIsOnlyReadIfEnabled() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, secondTowerReplies());
                BydConnection connection = open(battery)) {
            TowerData tower = new BydClient(connection, NO_SLEEP).readTower(2, twoTowerIdentity(), false);

            assertEquals(128, tower.cellVoltages().size());
            assertEquals(6, battery.requests().size());
        }
    }

    @Test
    void unmarkedSecondPassKeepsTheFirstPass() throws Exception {
        // the battery returns a first-pass buffer, as if it had ignored the second-pass command
        try (FakeBattery battery = FakeBattery.start(64,
                secondTowerReplies(Fixtures.frame("second-pass-echo"), Fixtures.frame("measurement-start-echo"),
                        Fixtures.frame("measurement-state-ready"), Fixtures.frame("hvs2x5-t2-measurement-1"),
                        Fixtures.frame("hvs2x5-t2-measurement-2")));
                BydConnection connection = open(battery)) {
            TowerData tower = new BydClient(connection, NO_SLEEP).readTower(2, twoTowerIdentity(), true);

            assertEquals(128, tower.cellVoltages().size());
            assertEquals(6 + 5, battery.requests().size());
        }
    }

    /**
     * @return the replies of the first pass of tower 2 of the 2-tower system, followed by {@code more}
     */
    private static byte[][] secondTowerReplies(byte[]... more) {
        List<byte[]> replies = new ArrayList<>(
                List.of(Fixtures.frame("measurement-start-echo"), Fixtures.frame("measurement-state-ready"),
                        Fixtures.frame("hvs2x5-t2-measurement-1"), Fixtures.frame("hvs2x5-t2-measurement-2"),
                        Fixtures.frame("hvs2x5-t2-measurement-3"), Fixtures.frame("hvs2x5-t2-measurement-4")));
        replies.addAll(List.of(more));
        return replies.toArray(byte[][]::new);
    }

    @Test
    void prematureBufferReadIsNotReady() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, Fixtures.frame("measurement-start-echo"),
                Fixtures.frame("measurement-state-ready"), Fixtures.frame("exception-not-ready"));
                BydConnection connection = open(battery)) {
            BydCommunicationException e = assertThrows(BydCommunicationException.class,
                    () -> new BydClient(connection, NO_SLEEP).readTower(1, identity(), false));
            assertEquals(Reason.NOT_READY, e.getReason());
        }
    }

    @Test
    void eventLog() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, Fixtures.frame("event-load-echo-bmu"),
                Fixtures.frame("event-state-ready"), Fixtures.frame("event-bmu-1"), Fixtures.frame("event-bmu-2"),
                Fixtures.frame("event-bmu-3"), Fixtures.frame("event-bmu-4"), Fixtures.frame("event-bmu-5"));
                BydConnection connection = open(battery)) {
            List<EventRecord> records = new BydClient(connection, NO_SLEEP).readEventLog(EventRecord.UNIT_BMU);
            assertEquals(20, records.size());
            assertEquals(38, records.getFirst().getCode());
        }
    }

    @Test
    void eventLogWithoutData() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, Fixtures.frame("event-load-echo-bmu"),
                withCrc(new byte[] { 0x01, 0x03, 0x02, (byte) 0x88, 0x00 }));
                BydConnection connection = open(battery)) {
            assertTrue(new BydClient(connection, NO_SLEEP).readEventLog(EventRecord.UNIT_BMU).isEmpty());
            assertEquals(2, battery.requests().size());
        }
    }

    @Test
    void connectionClosedBeforeAnyReplyMeansBusy() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, FakeBattery.CLOSE); BydConnection connection = open(battery)) {
            BydCommunicationException e = assertThrows(BydCommunicationException.class,
                    () -> new BydClient(connection, NO_SLEEP).readIdentity());
            assertEquals(Reason.BUSY, e.getReason());
        }
    }

    @Test
    void connectionClosedAfterAReplyIsAProtocolError() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, Fixtures.frame("identity"), FakeBattery.CLOSE);
                BydConnection connection = open(battery)) {
            BydClient client = new BydClient(connection, NO_SLEEP);
            client.readIdentity();
            BydCommunicationException e = assertThrows(BydCommunicationException.class, client::readStatus);
            assertEquals(Reason.PROTOCOL, e.getReason());
        }
    }

    @Test
    void missingReplyIsATimeout() throws Exception {
        try (FakeBattery battery = FakeBattery.start(64, FakeBattery.SILENCE);
                BydConnection connection = BydConnection.open("127.0.0.1", battery.port(), 300)) {
            BydCommunicationException e = assertThrows(BydCommunicationException.class,
                    () -> new BydClient(connection, NO_SLEEP).readIdentity());
            assertEquals(Reason.TIMEOUT, e.getReason());
        }
    }

    @Test
    void refusedConnectionIsUnreachable() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = socket.getLocalPort();
        }
        BydCommunicationException e = assertThrows(BydCommunicationException.class,
                () -> BydConnection.open("127.0.0.1", port, TIMEOUT_MILLIS));
        assertEquals(Reason.UNREACHABLE, e.getReason());
    }

    private static BydConnection open(FakeBattery battery) throws BydCommunicationException {
        return BydConnection.open("127.0.0.1", battery.port(), TIMEOUT_MILLIS);
    }

    private static SystemInfo identity() throws BydCommunicationException {
        return BydDecoder.decodeIdentity(Fixtures.frame("identity"));
    }

    private static SystemInfo twoTowerIdentity() throws BydCommunicationException {
        return BydDecoder.decodeIdentity(Fixtures.frame("hvs2x5-identity"));
    }

    private static byte[] withCrc(byte[] body) {
        byte[] frame = Arrays.copyOf(body, body.length + 2);
        int crc = ModbusFrame.crc(body, body.length);
        frame[body.length] = (byte) crc;
        frame[body.length + 1] = (byte) (crc >>> 8);
        return frame;
    }
}
