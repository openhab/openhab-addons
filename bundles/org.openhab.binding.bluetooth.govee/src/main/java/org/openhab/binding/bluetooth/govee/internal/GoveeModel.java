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
package org.openhab.binding.bluetooth.govee.internal;

import static org.openhab.binding.bluetooth.govee.internal.GoveeBindingConstants.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Connor Petty - Initial contribution
 * @author Matthias Bläsing - Fix reading advertisement data
 */
@NonNullByDefault
public enum GoveeModel {
    H5051(THING_TYPE_HYGROMETER, "Govee Wi-Fi Temperature Humidity Monitor", false),
    H5052(THING_TYPE_HYGROMETER_MONITOR, "Govee Temperature Humidity Monitor", true),
    H5071(THING_TYPE_HYGROMETER, "Govee Temperature Humidity Monitor", false),
    H5072(THING_TYPE_HYGROMETER_MONITOR, "Govee Temperature Humidity Monitor", true),
    H5074(THING_TYPE_HYGROMETER_MONITOR, "Govee Mini Temperature Humidity Monitor", true),
    H5075(THING_TYPE_HYGROMETER_MONITOR, "Govee Temperature Humidity Monitor", true),
    H5101(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    H5102(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    H5177(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    H5179(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    B5175(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true),
    B5178(THING_TYPE_HYGROMETER_MONITOR, "Govee Smart Thermo-Hygrometer", true);

    private static final byte[] SCAN_HEADER = { (byte) 0x88, (byte) 0xEC };
    private static final byte[] SCAN_HEADER2 = { (byte) 0x01, (byte) 0x00 };

    private final ThingTypeUID thingTypeUID;
    private final String label;
    private final boolean supportsWarningBroadcast;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoveeModel.class);

    private GoveeModel(ThingTypeUID thingTypeUID, String label, boolean supportsWarningBroadcast) {
        this.thingTypeUID = thingTypeUID;
        this.label = label;
        this.supportsWarningBroadcast = supportsWarningBroadcast;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public String getLabel() {
        return label;
    }

    public boolean supportsWarningBroadcast() {
        return supportsWarningBroadcast;
    }

    @Nullable
    ManufacturerDataSet parseManufacturerData(byte[] scanData) {
        if (scanData.length < 2) {
            return null;
        }

        ByteBuffer data = ByteBuffer.wrap(scanData);

        if ((scanData[0] == SCAN_HEADER[0] && scanData[1] == SCAN_HEADER[1])) {
            switch (this) {
                case H5072:
                case H5075:
                    return readManufacturerDataAtOffset(data, 3);
                case H5051:
                case H5052:
                case H5071:
                case H5074:
                    if (scanData.length < 8) {
                        return null;
                    }
                    data.order(ByteOrder.LITTLE_ENDIAN);
                    short temperature = data.getShort(3);
                    int humidity = Short.toUnsignedInt(data.getShort(5));
                    int battery = Byte.toUnsignedInt(data.get(7));
                    return new ManufacturerDataSet(temperature, humidity, battery);
                default:
                    return null;
            }
        } else if (scanData[0] == SCAN_HEADER2[0] && scanData[1] == SCAN_HEADER2[1]) {
            switch (this) {
                case H5101:
                case H5102:
                case B5175:
                case H5177:
                case H5179: {
                    return readManufacturerDataAtOffset(data, 4);
                }
                case B5178: {
                    // byte 4 holds the sensor ID
                    return readManufacturerDataAtOffset(data, 5);
                }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    // Read packed manufacturer data in the form temperature / humidity / battery
    // 4 bytes will be read by this function
    @Nullable
    private static ManufacturerDataSet readManufacturerDataAtOffset(ByteBuffer buffer, int pos) {
        if (buffer.limit() <= (pos + 3)) {
            return null; // Buffer to small to decode
        }
        int l = readIntHighbitSign(buffer, pos, 3);
        short temperature = (short) (l / 1000 * 10);
        int humidity = Math.abs(l % 1000) * 10;
        int fourthByte = buffer.get(pos + 3);
        boolean error = (fourthByte & 0x80) == 0x80; // extract high bit, currently not reported
        int battery = fourthByte & (0xFF ^ 0x80); // mask out high bit
        return new ManufacturerDataSet(temperature, humidity, battery);
    }

    private static int readIntHighbitSign(ByteBuffer buffer, int pos, int length) {
        int result = 0;
        boolean invert = false;
        for (int i = 0; i < length; i++) {
            int value = 0xFF & buffer.get(pos + i);
            // highest bit of first value encodes the sign
            if (i == 0 && (value & 0x80) > 0) {
                invert = true;
                value ^= 0x80;
            }
            result <<= 8;
            result |= value;
        }
        if (invert) {
            result = result * -1;
        }
        return result;
    }

    record ManufacturerDataSet(short temperature, int humidity, int battery) {
    }

    public static @Nullable GoveeModel getGoveeModel(BluetoothDevice device) {
        String name = device.getName();
        if (name != null) {
            if ((name.startsWith("Govee") && name.length() >= 11) || name.startsWith("GVH")) {
                String uname = name.toUpperCase();
                for (GoveeModel model : GoveeModel.values()) {
                    if (uname.contains(model.name())) {
                        LOGGER.debug("detected model {}", model);
                        return model;
                    }
                }
            }
        }
        LOGGER.debug("Device {} is no Govee", name);
        return null;
    }
}
