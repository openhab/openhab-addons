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
package org.openhab.binding.bluetooth.bluegiga.internal.eir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines an EIR record used in the BLE advertisement packets.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
@NonNullByDefault
public class EirRecord {
    private EirDataType type = EirDataType.UNKNOWN;
    private Object record = new Object();

    EirRecord(int @Nullable [] data) {
        if (data == null || data.length == 0) {
            return;
        }

        type = EirDataType.getEirPacketType(data[0]);
        switch (type) {
            case EIR_FLAGS:
                record = processFlags(data);
                break;
            case EIR_MANUFACTURER_SPECIFIC:
                record = processManufacturer(data);
                break;
            case EIR_SVC_UUID16_COMPLETE:
            case EIR_SVC_UUID16_INCOMPLETE:
                record = processUuid16(data);
                break;
            case EIR_SVC_UUID32_COMPLETE:
            case EIR_SVC_UUID32_INCOMPLETE:
                record = processUuid32(data);
                break;
            case EIR_SVC_UUID128_COMPLETE:
            case EIR_SVC_UUID128_INCOMPLETE:
                record = processUuid128(data);
                break;
            case EIR_NAME_LONG:
            case EIR_NAME_SHORT:
                record = processString(data);
                break;
            case EIR_TXPOWER:
                record = processUInt8(data);
                break;
            case EIR_SLAVEINTERVALRANGE:
                record = processUInt16List(data);
                break;
            case EIR_DEVICE_CLASS:
                record = processUInt8(data);
                break;
            case EIR_SVC_DATA_UUID16:
                record = processUUID16ServiceData(data);
                break;
            case EIR_SVC_DATA_UUID32:
                record = processUUID32ServiceData(data);
                break;
            case EIR_SVC_DATA_UUID128:
                record = processUUID128ServiceData(data);
                break;
            default:
                record = processUnknown(data);
                break;
        }
    }

    private Map<Short, int[]> processManufacturer(int[] data) {
        short manufacturer = (short) (((data[2] & 0xFFFF) << 8) | (data[1] & 0xFFFF));
        return Collections.singletonMap(manufacturer, Arrays.copyOfRange(data, 3, data.length));
    }

    private Map<UUID, int[]> processUUID16ServiceData(int[] data) {
        return Collections.singletonMap(process16BitUUID(data, 1), Arrays.copyOfRange(data, 3, data.length));
    }

    private Map<UUID, int[]> processUUID32ServiceData(int[] data) {
        return Collections.singletonMap(process32BitUUID(data, 1), Arrays.copyOfRange(data, 5, data.length));
    }

    private Map<UUID, int[]> processUUID128ServiceData(int[] data) {
        return Collections.singletonMap(process128BitUUID(data, 1), Arrays.copyOfRange(data, 17, data.length));
    }

    private List<UUID> processUuid16(int[] data) {
        List<UUID> uuidList = new ArrayList<>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 2) {
            uuidList.add(process16BitUUID(data, cnt));
        }

        return uuidList;
    }

    private List<UUID> processUuid32(int[] data) {
        List<UUID> uuidList = new ArrayList<>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 4) {
            uuidList.add(process32BitUUID(data, cnt));
        }

        return uuidList;
    }

    private List<UUID> processUuid128(int[] data) {
        List<UUID> uuidList = new ArrayList<>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 16) {
            uuidList.add(process128BitUUID(data, cnt));
        }

        return uuidList;
    }

    private UUID process16BitUUID(int[] data, int index) {
        // 0000xxxx-0000-1000-8000-00805F9B34FB
        long high = ((long) data[index] << 32) + ((long) data[index + 1] << 40) + 0x00001000L;
        return new UUID(high, 0x800000805f9b34fbL);
    }

    private UUID process32BitUUID(int[] data, int index) {
        // xxxxxxxx-0000-1000-8000-00805F9B34FB
        long high = ((long) data[index] << 32) + ((long) data[index + 1] << 40) + ((long) data[index + 2] << 48)
                + ((long) data[index + 3] << 56) + 0x00001000L;
        return new UUID(high, 0x800000805f9b34fbL);
    }

    private UUID process128BitUUID(int[] data, int index) {
        long low = (data[index]) + ((long) data[index + 1] << 8) + ((long) data[index + 2] << 16)
                + ((long) data[index + 3] << 24) + ((long) data[index + 4] << 32) + ((long) data[index + 5] << 40)
                + ((long) data[index + 6] << 48) + ((long) data[index + 7] << 56);
        long high = (data[index + 8]) + ((long) data[index + 9] << 8) + ((long) data[index + 10] << 16)
                + ((long) data[index + 11] << 24) + ((long) data[index + 12] << 32) + ((long) data[index + 13] << 40)
                + ((long) data[index + 14] << 48) + ((long) data[index + 15] << 56);
        return new UUID(high, low);
    }

    private List<Integer> processUInt16List(int[] data) {
        List<Integer> intList = new ArrayList<>();

        for (int cnt = 1; cnt < data.length - 1; cnt += 2) {
            intList.add(Integer.valueOf(data[cnt] + (data[cnt + 1] << 8)));
        }

        return intList;
    }

    private List<EirFlags> processFlags(int[] data) {
        List<EirFlags> flags = new ArrayList<>();
        int flagBit = 0;
        for (int cnt = 1; cnt < data.length; cnt++) {
            for (int bitcnt = 0; bitcnt < 8; bitcnt++) {
                if ((data[cnt] & (1 << bitcnt)) != 0) {
                    flags.add(EirFlags.getEirFlag(flagBit));
                }
                flagBit++;
            }
        }

        return flags;
    }

    private String processString(int[] data) {
        StringBuilder builder = new StringBuilder();
        for (int cnt = 1; cnt < data.length; cnt++) {
            builder.append((char) data[cnt]);
        }
        return builder.toString();
    }

    private int processUInt8(int[] data) {
        if (data[1] > 127) {
            return data[1] - 256;
        } else {
            return data[1];
        }
    }

    private String processUnknown(int[] data) {
        StringBuilder builder = new StringBuilder();
        for (int cnt = 0; cnt < data.length; cnt++) {
            builder.append(String.format("%02X", data[cnt]));
        }
        return builder.toString();
    }

    public EirDataType getType() {
        return type;
    }

    public Object getRecord() {
        return record;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EirRecord [type=");
        builder.append(type);
        builder.append(", record=");
        builder.append(record);
        builder.append(']');
        return builder.toString();
    }
}
