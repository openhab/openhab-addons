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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bydbatterybox.internal.dto.BatteryType;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;
import org.openhab.binding.bydbatterybox.internal.dto.SystemStatus;
import org.openhab.binding.bydbatterybox.internal.dto.TowerData;

/**
 * Decodes the battery's replies. All offsets of the identity and status blocks are positions in the complete reply
 * frame; the measurement buffer and the event log are decoded from the concatenated chunk payloads (see
 * {@link BydClient}).
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public final class BydDecoder {

    static final int IDENTITY_FRAME_LENGTH = 209;
    static final int STATUS_FRAME_LENGTH = 55;
    static final int MEASUREMENT_LENGTH = 4 * BydClient.CHUNK_PAYLOAD_LENGTH;
    static final int SECOND_PASS_LENGTH = 2 * BydClient.CHUNK_PAYLOAD_LENGTH;
    static final int EVENT_LOG_LENGTH = 5 * BydClient.CHUNK_PAYLOAD_LENGTH;

    static final int EVENT_RECORDS = 20;
    static final int EVENT_RECORD_LENGTH = 30;
    static final int EVENT_HEADER_LENGTH = 7;

    // measurement buffer layout
    private static final int MEAS_BALANCING_FLAGS = 12;
    private static final int MEAS_BALANCING_FLAGS_LENGTH = 16;
    private static final int MEAS_CHARGED_ENERGY = 28;
    private static final int MEAS_DISCHARGED_ENERGY = 32;
    private static final int MEAS_VOLTAGE = 40;
    private static final int MEAS_OUTPUT_VOLTAGE = 46;
    private static final int MEAS_STATE_OF_CHARGE = 48;
    private static final int MEAS_STATE_OF_HEALTH = 50;
    private static final int MEAS_CURRENT = 52;
    private static final int MEAS_STATE_BITS = 54;
    private static final int MEAS_BMS_FIRMWARE_A = 60;
    private static final int MEAS_BMS_FIRMWARE_B = 62;
    /** 0 in the first and 1 in the second measurement pass. */
    private static final int MEAS_PASS = 65;
    private static final int MEAS_SERIAL_NUMBER = 66;
    private static final int MEAS_CELL_VOLTAGES = 96;
    private static final int MEAS_TEMPERATURES = 354;

    private static final int SERIAL_NUMBER_LENGTH = 24;
    private static final int MAX_TOWERS = 3;

    private BydDecoder() {
    }

    public static SystemInfo decodeIdentity(byte[] frame) throws BydCommunicationException {
        requireLength(frame, IDENTITY_FRAME_LENGTH, "identity");
        int topology = u8(frame, 36);
        int typeCode = u8(frame, 37);
        return new SystemInfo(ascii(frame, 3, SERIAL_NUMBER_LENGTH), version(u8(frame, 27), u8(frame, 28)),
                version(u8(frame, 29), u8(frame, 30)), u8(frame, 33) == 0 ? 'A' : 'B',
                version(u8(frame, 31), u8(frame, 32)), u8(frame, 34) == 0 ? 'A' : 'B', u8(frame, 35),
                towers(topology >>> 4), topology & 0x0F, BatteryType.fromCode(typeCode), typeCode, u8(frame, 38),
                dateTime(frame, 201));
    }

    public static SystemStatus decodeStatus(byte[] frame) throws BydCommunicationException {
        requireLength(frame, STATUS_FRAME_LENGTH, "status");
        return new SystemStatus(s16(frame, 3), BigDecimal.valueOf(s16(frame, 5), 2),
                BigDecimal.valueOf(s16(frame, 7), 2), s16(frame, 9), BigDecimal.valueOf(s16(frame, 11), 1),
                BigDecimal.valueOf(u16(frame, 13), 2), s16(frame, 15), s16(frame, 17), s16(frame, 19), u16(frame, 29),
                version(u8(frame, 31), u8(frame, 32)), BigDecimal.valueOf(u16(frame, 35), 2),
                BigDecimal.valueOf(u32WordSwapped(frame, 37), 1), BigDecimal.valueOf(u32WordSwapped(frame, 41), 1));
    }

    /**
     * Decodes the concatenated payloads of the four measurement buffer chunks of one tower.
     */
    public static TowerData decodeMeasurement(int tower, byte[] buffer, SystemInfo info)
            throws BydCommunicationException {
        requireLength(buffer, MEASUREMENT_LENGTH, "measurement buffer");
        List<Integer> cells = new ArrayList<>();
        for (int i = 0; i < info.firstPassCellsPerTower(); i++) {
            cells.add(s16(buffer, MEAS_CELL_VOLTAGES + 2 * i));
        }
        // temperatures are unsigned bytes in all known implementations; negative values cannot occur in that format
        List<Integer> temperatures = new ArrayList<>();
        for (int i = 0; i < info.temperatureSensorsPerTower(); i++) {
            temperatures.add(u8(buffer, MEAS_TEMPERATURES + i));
        }
        int balancing = bitCount(buffer, MEAS_BALANCING_FLAGS, MEAS_BALANCING_FLAGS_LENGTH);
        // firmware versions are stored as (minor, major)
        return new TowerData(tower, BigDecimal.valueOf(s16(buffer, MEAS_STATE_OF_CHARGE), 1),
                s16(buffer, MEAS_STATE_OF_HEALTH), BigDecimal.valueOf(s16(buffer, MEAS_VOLTAGE), 1),
                BigDecimal.valueOf(s16(buffer, MEAS_OUTPUT_VOLTAGE), 1),
                BigDecimal.valueOf(s16(buffer, MEAS_CURRENT), 1), u32WordSwapped(buffer, MEAS_CHARGED_ENERGY),
                u32WordSwapped(buffer, MEAS_DISCHARGED_ENERGY), u16(buffer, MEAS_STATE_BITS), balancing, cells,
                temperatures, version(u8(buffer, MEAS_BMS_FIRMWARE_A + 1), u8(buffer, MEAS_BMS_FIRMWARE_A)),
                version(u8(buffer, MEAS_BMS_FIRMWARE_B + 1), u8(buffer, MEAS_BMS_FIRMWARE_B)),
                ascii(buffer, MEAS_SERIAL_NUMBER, SERIAL_NUMBER_LENGTH));
    }

    /**
     * Adds the cells beyond 128 from the first two chunks of the second measurement pass, which has the layout of the
     * first pass.
     * <p>
     * The balancing flags of these cells are assumed to start at the position of the first-pass flags; this is not
     * verified yet. Under both bit orders discussed for the first pass, the flags of cells 129-160 are in the first 4
     * bytes, and only their number is used.
     *
     * @throws BydCommunicationException if the buffer is not marked as second pass
     */
    public static TowerData decodeSecondPass(TowerData firstPass, byte[] buffer, SystemInfo info)
            throws BydCommunicationException {
        requireLength(buffer, SECOND_PASS_LENGTH, "second measurement buffer");
        if (u8(buffer, MEAS_PASS) != 1) {
            throw new BydCommunicationException(BydCommunicationException.Reason.PROTOCOL,
                    "measurement buffer is not marked as second pass (" + u8(buffer, MEAS_PASS) + ")");
        }
        int count = info.secondPassCellsPerTower();
        List<Integer> cells = new ArrayList<>(firstPass.cellVoltages());
        for (int i = 0; i < count; i++) {
            cells.add(s16(buffer, MEAS_CELL_VOLTAGES + 2 * i));
        }
        int balancing = firstPass.balancingCells() + bitCount(buffer, MEAS_BALANCING_FLAGS, (count + 7) / 8);
        return firstPass.withCells(cells, balancing);
    }

    /**
     * Decodes the concatenated payloads of the five event log chunks of one unit. The battery lists the newest entry
     * first; the returned list keeps that order. Empty slots are skipped.
     */
    public static List<EventRecord> decodeEventLog(int unit, byte[] buffer) throws BydCommunicationException {
        requireLength(buffer, EVENT_LOG_LENGTH, "event log");
        List<EventRecord> records = new ArrayList<>();
        for (int r = 0; r < EVENT_RECORDS; r++) {
            int offset = r * EVENT_RECORD_LENGTH;
            LocalDateTime timestamp = dateTime(buffer, offset + 1);
            if (timestamp != null) {
                records.add(new EventRecord(unit, u8(buffer, offset), timestamp,
                        Arrays.copyOfRange(buffer, offset + EVENT_HEADER_LENGTH, offset + EVENT_RECORD_LENGTH)));
            }
        }
        return records;
    }

    /**
     * The tower count is the high nibble of the topology byte. Only towers 1..3 can be addressed by the measurement
     * command; 0 is treated as a single tower, as in the smarthomeNG implementation.
     */
    private static int towers(int raw) {
        return Math.max(1, Math.min(MAX_TOWERS, raw));
    }

    private static @Nullable LocalDateTime dateTime(byte[] b, int offset) {
        try {
            return LocalDateTime.of(2000 + u8(b, offset), u8(b, offset + 1), u8(b, offset + 2), u8(b, offset + 3),
                    u8(b, offset + 4), u8(b, offset + 5));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static int bitCount(byte[] b, int offset, int length) {
        int count = 0;
        for (int i = 0; i < length; i++) {
            count += Integer.bitCount(u8(b, offset + i));
        }
        return count;
    }

    private static String version(int major, int minor) {
        return major + "." + minor;
    }

    private static String ascii(byte[] b, int offset, int length) {
        return new String(b, offset, length, StandardCharsets.US_ASCII).replace('\0', ' ').trim();
    }

    private static void requireLength(byte[] b, int length, String what) throws BydCommunicationException {
        if (b.length != length) {
            throw new BydCommunicationException(BydCommunicationException.Reason.PROTOCOL,
                    what + " has " + b.length + " bytes instead of " + length);
        }
    }

    static int u8(byte[] b, int i) {
        return b[i] & 0xFF;
    }

    static int u16(byte[] b, int i) {
        return (u8(b, i) << 8) | u8(b, i + 1);
    }

    static int s16(byte[] b, int i) {
        return (short) u16(b, i);
    }

    /** 32-bit value stored as two registers, low word first. */
    static long u32WordSwapped(byte[] b, int i) {
        return ((long) u16(b, i + 2) << 16) | u16(b, i);
    }
}
