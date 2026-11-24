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
package org.openhab.binding.bydbatterybox.internal.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One entry of an event log. The timestamp is the battery's clock, which is neither time-zone aware nor
 * synchronised.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public final class EventRecord {

    /** Unit number of the BMU; BMS units are numbered by tower, starting at 1. */
    public static final int UNIT_BMU = 0;

    private final int unit;
    private final int code;
    private final LocalDateTime timestamp;
    private final byte[] data;

    public EventRecord(int unit, int code, LocalDateTime timestamp, byte[] data) {
        this.unit = unit;
        this.code = code;
        this.timestamp = timestamp;
        this.data = data.clone();
    }

    public int getUnit() {
        return unit;
    }

    public boolean isBmu() {
        return unit == UNIT_BMU;
    }

    public int getCode() {
        return code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int dataLength() {
        return data.length;
    }

    public int u8(int index) {
        return data[index] & 0xFF;
    }

    public int u16BigEndian(int index) {
        return (u8(index) << 8) | u8(index + 1);
    }

    public int u16LittleEndian(int index) {
        return u8(index) | (u8(index + 1) << 8);
    }

    public String dataHex() {
        return HexFormat.of().formatHex(data);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof EventRecord other && unit == other.unit && code == other.code
                && timestamp.equals(other.timestamp) && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, code, timestamp, Arrays.hashCode(data));
    }

    @Override
    public String toString() {
        return "EventRecord[unit=" + unit + ", code=" + code + ", timestamp=" + timestamp + ", data=" + dataHex() + "]";
    }
}
