/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.ByteUtils;

/**
 * The BaseRecord abstract class holds a database record
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class BaseRecord {
    private RecordType type;
    private byte group;
    private InsteonAddress address;
    private byte[] data;

    public BaseRecord(RecordType type, byte group, InsteonAddress address, byte[] data) {
        this.type = type;
        this.group = group;
        this.address = address;
        this.data = data;
    }

    public RecordType getType() {
        return type;
    }

    public int getGroup() {
        return group & 0xFF;
    }

    public InsteonAddress getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    public int getData1() {
        return data[0] & 0xFF;
    }

    public int getData2() {
        return data[1] & 0xFF;
    }

    public int getData3() {
        return data[2] & 0xFF;
    }

    public boolean isController() {
        return type == RecordType.CONTROLLER;
    }

    public boolean isResponder() {
        return type == RecordType.RESPONDER;
    }

    public boolean isLast() {
        return type == RecordType.LAST;
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
        BaseRecord other = (BaseRecord) obj;
        if (type != other.type) {
            return false;
        }
        if (group != other.group) {
            return false;
        }
        if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type.hashCode();
        result = prime * result + group;
        result = prime * result + address.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return address + " " + type.getId() + " group: " + ByteUtils.getHexString(group) + " data1: "
                + ByteUtils.getHexString(data[0]) + " data2: " + ByteUtils.getHexString(data[1]) + " data3: "
                + ByteUtils.getHexString(data[2]);
    }
}
