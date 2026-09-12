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

    /**
     * Minimum required size of scanning data packet. This value must be aligned
     * with the decoding implementation in {@link #onScanRecordReceived}.
     *
     * <p>
     * This is the minimal size required for decoding, the actual package
     * might contain more data.
     * </p>
     *
     * @param model
     * @return size of payload of scanning data without the manufacturer id
     */
    private int scanPacketSize() {
        switch (this) {
            default:
                return 7;
            case H5072:
            case H5075:
                return 5;
            case H5179:
                return 8;
            case H5051:
            case H5052:
            case H5071:
            case H5074:
                return 7;
        }
    }

    @Nullable
    ManufacturerDataSet parseManufacturerData(byte[] scanData) {
        int dataPacketSize = scanPacketSize();

        if ((2 + dataPacketSize) > scanData.length || scanData[0] != SCAN_HEADER[0] || scanData[1] != SCAN_HEADER[1]) {
            return null;
        }

        ByteBuffer data = ByteBuffer.wrap(scanData, 2, dataPacketSize);

        short temperature;
        int humidity;
        int battery;
        int wifiLevel = 0;

        switch (this) {
            default:
                data.position(2);// we throw this away
                // fall through
            case H5072:
            case H5075:
                data.order(ByteOrder.BIG_ENDIAN);
                int l = data.getInt();
                l = l & 0xFFFFFF;

                boolean positive = (l & 0x800000) == 0;
                int tem = (short) ((l / 1000) * 10);
                if (!positive) {
                    tem = -tem;
                }
                temperature = (short) tem;
                humidity = (l % 1000) * 10;
                battery = data.get();
                break;
            case H5179:
                data.order(ByteOrder.LITTLE_ENDIAN);
                data.position(3);
                temperature = data.getShort();
                humidity = data.getShort();
                battery = Byte.toUnsignedInt(data.get());
                break;
            case H5051:
            case H5052:
            case H5071:
            case H5074:
                data.order(ByteOrder.LITTLE_ENDIAN);
                boolean hasWifi = data.get() == 0;
                temperature = data.getShort();
                humidity = Short.toUnsignedInt(data.getShort());
                battery = Byte.toUnsignedInt(data.get());
                wifiLevel = hasWifi ? Byte.toUnsignedInt(data.get()) : 0;
                break;
        }

        return new ManufacturerDataSet(temperature, humidity, battery, wifiLevel);
    }

    record ManufacturerDataSet(short temperature, int humidity, int battery, int wifiLevel) {
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
