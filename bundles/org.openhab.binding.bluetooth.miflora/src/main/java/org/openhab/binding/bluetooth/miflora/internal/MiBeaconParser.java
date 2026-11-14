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
package org.openhab.binding.bluetooth.miflora.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Decodes the unencrypted Xiaomi "MiBeacon" service data (UUID 0xFE95) a Mi Flora sensor broadcasts.
 *
 * Layout, little endian: frame control (2 bytes), product ID (2), frame counter (1), then, as the frame control
 * flags say, the sensor's address (6), a capability byte (1, plus 1 if it has bit 0x20 set), and objects of type
 * (2), length (1) and value. A Mi Flora (product 0x0098) sends one object per frame.
 *
 * @author Lukas Brinias - Initial contribution
 */
@NonNullByDefault
final class MiBeaconParser {

    static final int OBJECT_TEMPERATURE = 0x1004;
    static final int OBJECT_ILLUMINANCE = 0x1007;
    static final int OBJECT_MOISTURE = 0x1008;
    static final int OBJECT_CONDUCTIVITY = 0x1009;

    private static final int FLAG_ENCRYPTED = 0x0008;
    private static final int FLAG_MAC_INCLUDED = 0x0010;
    private static final int FLAG_CAPABILITY_INCLUDED = 0x0020;
    private static final int FLAG_OBJECT_INCLUDED = 0x0040;
    private static final int FLAG_MESH = 0x0080;
    private static final int CAPABILITY_IO = 0x20;
    private static final int HEADER_LENGTH = 5;

    /**
     * A decoded frame.
     *
     * @param productId the product ID, 0x0098 for a Mi Flora
     * @param frameCounter changes with each new reading, not with the repeats of a frame
     * @param readings the objects in the frame
     */
    record Frame(int productId, int frameCounter, List<Reading> readings) {
    }

    /**
     * One object from a frame, with its value as the little endian number its bytes hold.
     *
     * @param type the object type, such as {@link #OBJECT_TEMPERATURE}
     * @param value the raw value; signed for temperature, unsigned otherwise
     */
    record Reading(int type, long value) {
    }

    private MiBeaconParser() {
    }

    /**
     * Decodes MiBeacon service data.
     *
     * @param data the service data
     * @return the frame, or null if the data is malformed, encrypted, from a mesh device, or carries no readings
     */
    static @Nullable Frame parse(byte[] data) {
        if (data.length < HEADER_LENGTH) {
            return null;
        }
        int frameControl = u16(data, 0);
        if ((frameControl & (FLAG_ENCRYPTED | FLAG_MESH)) != 0 || (frameControl & FLAG_OBJECT_INCLUDED) == 0) {
            return null;
        }
        int i = HEADER_LENGTH;
        if ((frameControl & FLAG_MAC_INCLUDED) != 0) {
            i += 6;
        }
        if ((frameControl & FLAG_CAPABILITY_INCLUDED) != 0) {
            if (data.length <= i) {
                return null;
            }
            i += (data[i] & CAPABILITY_IO) != 0 ? 2 : 1;
        }
        List<Reading> readings = new ArrayList<>();
        while (i + 3 <= data.length) {
            int type = u16(data, i);
            int length = data[i + 2] & 0xFF;
            int valueAt = i + 3;
            if (length == 0 || valueAt + length > data.length) {
                return null;
            }
            i = valueAt + length;
            if (length > 4) {
                // Not a Mi Flora reading
                continue;
            }
            long value = 0;
            for (int b = length - 1; b >= 0; b--) {
                value = (value << 8) | (data[valueAt + b] & 0xFF);
            }
            if (type == OBJECT_TEMPERATURE && length == 2) {
                value = (short) value;
            }
            readings.add(new Reading(type, value));
        }
        if (readings.isEmpty() || i != data.length) {
            return null;
        }
        return new Frame(u16(data, 2), data[4] & 0xFF, List.copyOf(readings));
    }

    private static int u16(byte[] data, int index) {
        return (data[index] & 0xFF) | (data[index + 1] & 0xFF) << 8;
    }
}
