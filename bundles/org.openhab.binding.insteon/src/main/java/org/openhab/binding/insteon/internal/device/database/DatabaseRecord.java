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
package org.openhab.binding.insteon.internal.device.database;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * The {@link DatabaseRecord} holds a link database record
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DatabaseRecord {
    public static final int LOCATION_ZERO = 0;

    private final int location;
    private final RecordType type;
    private final int group;
    private final InsteonAddress address;
    private final byte[] data;

    public DatabaseRecord(int location, RecordType type, int group, InsteonAddress address, byte[] data) {
        this.location = location;
        this.type = type;
        this.group = group;
        this.address = address;
        this.data = data;
    }

    public DatabaseRecord(DatabaseRecord record) {
        this.location = record.location;
        this.type = record.type;
        this.group = record.group;
        this.address = record.address;
        this.data = record.data;
    }

    public int getLocation() {
        return location;
    }

    public RecordType getType() {
        return type;
    }

    public int getFlags() {
        return type.getFlags();
    }

    public int getGroup() {
        return group;
    }

    public InsteonAddress getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    public int getData1() {
        return Byte.toUnsignedInt(data[0]);
    }

    public int getData2() {
        return Byte.toUnsignedInt(data[1]);
    }

    public int getData3() {
        return Byte.toUnsignedInt(data[2]);
    }

    public boolean isController() {
        return type.isController();
    }

    public boolean isResponder() {
        return type.isResponder();
    }

    public boolean isActive() {
        return type.isActive();
    }

    public boolean isAvailable() {
        return !type.isActive();
    }

    public boolean isLast() {
        return type.isHighWaterMark();
    }

    public byte[] getBytes() {
        return new byte[] { (byte) type.getFlags(), (byte) group, address.getHighByte(), address.getMiddleByte(),
                address.getLowByte(), data[0], data[1], data[2] };
    }

    @Override
    public String toString() {
        String s = "";
        if (location != LOCATION_ZERO) {
            s += HexUtils.getHexString(location, 4) + " ";
        }
        s += address + " " + type;
        s += " group: " + HexUtils.getHexString(group);
        s += " data1: " + HexUtils.getHexString(data[0]);
        s += " data2: " + HexUtils.getHexString(data[1]);
        s += " data3: " + HexUtils.getHexString(data[2]);
        return s;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DatabaseRecord other = (DatabaseRecord) obj;
        return group == other.group && address.equals(other.address) && type.equals(other.type)
                && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + group;
        result = prime * result + address.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + Arrays.hashCode(data);
        return result;
    }
}
