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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.binding.bydbatterybox.internal.dto.BatteryType;
import org.openhab.binding.bydbatterybox.internal.dto.CellStatistics;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;
import org.openhab.binding.bydbatterybox.internal.dto.SystemStatus;
import org.openhab.binding.bydbatterybox.internal.dto.TowerData;

/**
 * Tests {@link BydDecoder} with frames captured from an HVS 7.7 (1 tower, 3 modules) and from an HVS with 2 towers of 5
 * modules each. The expected values of the HVS 7.7 were cross-checked against the BYD-Logger tool at the time of
 * capture.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BydDecoderTest {

    @Test
    void identity() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("identity"));

        assertEquals("P03XXXXXXXXXXXXXXXXXXXXX", info.serialNumber());
        assertEquals("3.24", info.bmuFirmwareA());
        assertEquals("3.26", info.bmuFirmwareB());
        assertEquals('B', info.activeBmuSection());
        assertEquals("3.26-B", info.activeBmuFirmware());
        assertEquals("3.31", info.bmsFirmware());
        assertEquals('B', info.activeBmsSection());
        assertEquals(1, info.towers());
        assertEquals(3, info.modulesPerTower());
        assertEquals(BatteryType.HVS, info.batteryType());
        assertEquals(0, info.inverterType());
        assertEquals(0, info.application());
        assertEquals(0, info.phase());
        assertEquals(LocalDateTime.of(2026, 11, 16, 18, 37, 8), info.clock());
        assertEquals(96, info.firstPassCellsPerTower());
        assertEquals(36, info.temperatureSensorsPerTower());
    }

    @Test
    void identityWithUnknownTypeAndZeroTowers() throws BydCommunicationException {
        byte[] frame = Fixtures.frame("identity");
        frame[36] = 0x05;
        frame[37] = 0x07;
        frame[202] = 0;
        SystemInfo info = BydDecoder.decodeIdentity(frame);

        assertEquals(1, info.towers());
        assertEquals(5, info.modulesPerTower());
        assertEquals(BatteryType.UNSUPPORTED, info.batteryType());
        assertEquals(7, info.batteryTypeCode());
        assertEquals(0, info.firstPassCellsPerTower());
        assertNull(info.clock());
    }

    @Test
    void identityOfTwoTowersWithFiveModules() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("hvs2x5-identity"));

        assertEquals("P03XXXXXXXXXXXXXXXXXXXXX", info.serialNumber());
        assertEquals("3.27-B", info.activeBmuFirmware());
        assertEquals("3.32", info.bmsFirmware());
        assertEquals(2, info.towers());
        assertEquals(5, info.modulesPerTower());
        assertEquals(BatteryType.HVS, info.batteryType());
        assertEquals(19, info.inverterType());
        assertEquals(1, info.application());
        assertEquals(1, info.phase());
        assertEquals(160, info.cellsPerTower());
        assertEquals(128, info.firstPassCellsPerTower());
        assertEquals(32, info.secondPassCellsPerTower());
        assertEquals(60, info.temperatureSensorsPerTower());
    }

    @Test
    void secondTowerMeasurement() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("hvs2x5-identity"));
        TowerData tower = BydDecoder.decodeMeasurement(2, Fixtures.chunkPayloads("hvs2x5-t2-measurement", 4), info);

        assertEquals(2, tower.tower());
        assertDecimal("15.8", tower.stateOfCharge());
        assertEquals(98, tower.stateOfHealth());
        assertDecimal("512.6", tower.voltage());
        assertDecimal("513.3", tower.outputVoltage());
        assertEquals(1_829_495, tower.chargedEnergyWh());
        assertEquals(1_628_405, tower.dischargedEnergyWh());
        assertEquals("3.32", tower.bmsFirmwareA());
        assertEquals("3.31", tower.bmsFirmwareB());
        assertEquals("P03XXXXXXXXXXXXXXXXXXXXX", tower.serialNumber());

        List<Integer> cells = tower.cellVoltages();
        assertEquals(128, cells.size());
        assertEquals(3205, cells.getFirst());
        assertEquals(3205, cells.getLast());
        // all 60 sensors of the 5 modules are in the first pass
        assertEquals(60, tower.temperatures().size());
        assertEquals(15, tower.temperatures().getFirst());
        assertEquals(14, tower.temperatures().getLast());
    }

    @Test
    void secondPassAddsTheCellsBeyond128() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("hvs2x5-identity"));
        for (int tower = 1; tower <= 2; tower++) {
            TowerData firstPass = BydDecoder.decodeMeasurement(tower,
                    Fixtures.chunkPayloads("hvs2x5-t" + tower + "-measurement", 4), info);
            TowerData data = BydDecoder.decodeSecondPass(firstPass,
                    Fixtures.chunkPayloads("hvs2x5-t" + tower + "-second-pass", 2), info);

            List<Integer> cells = data.cellVoltages();
            assertEquals(160, cells.size());
            assertEquals(firstPass.cellVoltages(), cells.subList(0, 128));
            assertEquals(firstPass.temperatures(), data.temperatures());
            assertEquals(firstPass.serialNumber(), data.serialNumber());
            assertEquals(0, data.balancingCells());
            CellStatistics module5 = CellStatistics.of(cells, 128, 160);
            assertNotNull(module5);
            assertTrue(module5.min() >= 3202 && module5.max() <= 3206, () -> "module 5: " + cells.subList(128, 160));
        }
    }

    @Test
    void secondPassBalancingFlagsAreCountedForTheExtraCellsOnly() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("hvs2x5-identity"));
        byte[] firstPassBuffer = Fixtures.chunkPayloads("hvs2x5-t1-measurement", 4);
        firstPassBuffer[12] = 0x01;
        TowerData firstPass = BydDecoder.decodeMeasurement(1, firstPassBuffer, info);
        byte[] buffer = Fixtures.chunkPayloads("hvs2x5-t1-second-pass", 2);
        buffer[12] = (byte) 0x81;
        buffer[15] = 0x10;
        // beyond the flags of cells 129-160
        buffer[16] = (byte) 0xFF;

        assertEquals(1 + 3, BydDecoder.decodeSecondPass(firstPass, buffer, info).balancingCells());
    }

    @Test
    void firstPassBufferIsRejectedAsSecondPass() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("hvs2x5-identity"));
        byte[] firstPassBuffer = Fixtures.chunkPayloads("hvs2x5-t1-measurement", 4);
        TowerData firstPass = BydDecoder.decodeMeasurement(1, firstPassBuffer, info);

        BydCommunicationException e = assertThrows(BydCommunicationException.class, () -> BydDecoder
                .decodeSecondPass(firstPass, Arrays.copyOf(firstPassBuffer, BydDecoder.SECOND_PASS_LENGTH), info));
        assertEquals(BydCommunicationException.Reason.PROTOCOL, e.getReason());
    }

    @Test
    void status() throws BydCommunicationException {
        SystemStatus status = BydDecoder.decodeStatus(Fixtures.frame("status"));

        assertEquals(6, status.stateOfCharge());
        assertEquals(94, status.stateOfHealth());
        assertDecimal("3.19", status.maxCellVoltage());
        assertDecimal("3.10", status.minCellVoltage());
        assertDecimal("0.0", status.current());
        assertDecimal("305.30", status.voltage());
        assertDecimal("305.30", status.outputVoltage());
        assertEquals(26, status.maxCellTemperature());
        assertEquals(22, status.minCellTemperature());
        assertEquals(24, status.bmuTemperature());
        assertEquals(0, status.errorBits());
        assertEquals("9.2", status.parameterTable());
        assertDecimal("5515.4", status.chargedEnergy());
        assertDecimal("5055.1", status.dischargedEnergy());
    }

    @Test
    void negativeValuesAreSigned() throws BydCommunicationException {
        byte[] frame = Fixtures.frame("status");
        // current -0.8 A (charging) and a minimum temperature of -5 °C
        frame[11] = (byte) 0xFF;
        frame[12] = (byte) 0xF8;
        frame[17] = (byte) 0xFF;
        frame[18] = (byte) 0xFB;
        SystemStatus status = BydDecoder.decodeStatus(frame);
        assertDecimal("-0.8", status.current());
        assertEquals(-5, status.minCellTemperature());
    }

    @Test
    void balancingFlagsAreWordsWithBitZeroForTheFirstCell() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("identity"));
        byte[] buffer = Fixtures.chunkPayloads("measurement", 4);
        // flags at 12: word 0 = 0x8001 (cells 1 and 16), word 1 = 0x0002 (cell 18), word 7 = 0x4000 (cell 127)
        buffer[12] = (byte) 0x80;
        buffer[13] = 0x01;
        buffer[15] = 0x02;
        buffer[26] = 0x40;

        TowerData tower = BydDecoder.decodeMeasurement(1, buffer, info);

        assertEquals(List.of(1, 16, 18, 127), tower.balancingCellNumbers());
        assertEquals(4, tower.balancingCells());
    }

    @Test
    void measurementStatusWords() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("identity"));
        byte[] buffer = Fixtures.chunkPayloads("measurement", 4);
        // big-endian words: warnings 1 at 54, warnings 2 at 56, warnings 3 at 58, errors at 94
        buffer[55] = 0x10;
        buffer[56] = (byte) 0x80;
        buffer[59] = 0x01;
        buffer[94] = 0x01;
        buffer[95] = 0x02;

        TowerData tower = BydDecoder.decodeMeasurement(1, buffer, info);

        assertEquals(0x0010, tower.warningBits1());
        assertEquals(0x8000, tower.warningBits2());
        assertEquals(0x0001, tower.warningBits3());
        assertEquals(0x0102, tower.errorBits());
        // the cell voltages behind the error word are unchanged
        assertEquals(3121, tower.cellVoltages().get(0));
    }

    @Test
    void measurement() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("identity"));
        TowerData tower = BydDecoder.decodeMeasurement(1, Fixtures.chunkPayloads("measurement", 4), info);

        assertEquals(1, tower.tower());
        assertDecimal("6.3", tower.stateOfCharge());
        assertEquals(94, tower.stateOfHealth());
        assertDecimal("305.2", tower.voltage());
        assertDecimal("305.7", tower.outputVoltage());
        assertDecimal("0.0", tower.current());
        assertEquals(5_515_448, tower.chargedEnergyWh());
        assertEquals(5_055_100, tower.dischargedEnergyWh());
        assertEquals(0, tower.warningBits1());
        assertEquals(0, tower.warningBits2());
        assertEquals(0, tower.warningBits3());
        assertEquals(0, tower.errorBits());
        assertEquals(0, tower.balancingCells());
        assertEquals("3.30", tower.bmsFirmwareA());
        assertEquals("3.31", tower.bmsFirmwareB());
        assertEquals("P03XXXXXXXXXXXXXXXXXXXXX", tower.serialNumber());

        List<Integer> cells = tower.cellVoltages();
        assertEquals(96, cells.size());
        assertEquals(3121, cells.get(0));
        assertEquals(3098, cells.get(4));
        assertEquals(3195, cells.get(58));
        assertEquals(3191, cells.get(95));

        List<Integer> temperatures = tower.temperatures();
        assertEquals(36, temperatures.size());
        assertEquals(26, temperatures.get(0));
        assertEquals(22, temperatures.get(28));
        assertEquals(23, temperatures.get(35));
    }

    @Test
    void measurementStatisticsMatchTheReferenceTool() throws BydCommunicationException {
        SystemInfo info = BydDecoder.decodeIdentity(Fixtures.frame("identity"));
        List<Integer> cells = BydDecoder.decodeMeasurement(1, Fixtures.chunkPayloads("measurement", 4), info)
                .cellVoltages();

        CellStatistics all = CellStatistics.of(cells, 0, cells.size());
        assertNotNull(all);
        assertEquals(3098, all.min());
        assertEquals(5, all.minPosition());
        assertEquals(3195, all.max());
        assertEquals(59, all.maxPosition());
        assertEquals(97, all.spread());
        assertDecimal("3179.6", all.average());

        // module sums and spreads as shown per module by the reference tool (within its later sample)
        long[] moduleSums = { 100_979, 102_157, 102_109 };
        int[] moduleSpreads = { 86, 7, 9 };
        for (int m = 0; m < 3; m++) {
            CellStatistics module = CellStatistics.of(cells, m * 32, (m + 1) * 32);
            assertNotNull(module);
            assertEquals(moduleSums[m], module.sum());
            assertEquals(moduleSpreads[m], module.spread());
        }
    }

    @Test
    void bmuEventLog() throws BydCommunicationException {
        List<EventRecord> records = BydDecoder.decodeEventLog(EventRecord.UNIT_BMU,
                Fixtures.chunkPayloads("event-bmu", 5));

        assertEquals(20, records.size());
        EventRecord newest = records.getFirst();
        assertTrue(newest.isBmu());
        assertEquals(38, newest.getCode());
        assertEquals(LocalDateTime.of(2026, 11, 16, 7, 42, 4), newest.getTimestamp());
        // SOP info: max charge voltage 350.4 V = 96 x 3.65 V, min discharge voltage 240.0 V = 96 x 2.5 V
        assertEquals(3504, newest.u16BigEndian(4));
        assertEquals(2400, newest.u16BigEndian(6));

        EventRecord statusChange = records.get(2);
        assertEquals(32, statusChange.getCode());
        assertEquals(3, statusChange.u8(0));
        assertEquals(7, statusChange.u8(1));

        assertEquals(LocalDateTime.of(2026, 11, 15, 17, 41, 31), records.getLast().getTimestamp());
    }

    @Test
    void bmsEventLog() throws BydCommunicationException {
        List<EventRecord> records = BydDecoder.decodeEventLog(1, Fixtures.chunkPayloads("event-bms1", 5));

        assertEquals(20, records.size());
        EventRecord stopDischarging = records.getFirst();
        assertFalse(stopDischarging.isBmu());
        assertEquals(1, stopDischarging.getUnit());
        assertEquals(7, stopDischarging.getCode());
        assertEquals(LocalDateTime.of(2026, 11, 16, 14, 28, 14), stopDischarging.getTimestamp());
        assertEquals(7, stopDischarging.u8(9));
        assertEquals(3053, stopDischarging.u16LittleEndian(11));

        EventRecord startCharging = records.get(4);
        assertEquals(4, startCharging.getCode());
        assertEquals(-18, (short) startCharging.u16LittleEndian(15));
    }

    @Test
    void emptyEventSlotsAreSkipped() throws BydCommunicationException {
        assertTrue(BydDecoder.decodeEventLog(1, new byte[BydDecoder.EVENT_LOG_LENGTH]).isEmpty());
    }

    @Test
    void wrongLengthIsAProtocolError() {
        BydCommunicationException e = assertThrows(BydCommunicationException.class,
                () -> BydDecoder.decodeIdentity(Fixtures.frame("status")));
        assertEquals(BydCommunicationException.Reason.PROTOCOL, e.getReason());
        assertThrows(BydCommunicationException.class, () -> BydDecoder.decodeStatus(Fixtures.frame("identity")));
        assertThrows(BydCommunicationException.class, () -> BydDecoder.decodeEventLog(0, new byte[10]));
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> expected + " != " + actual);
    }
}
