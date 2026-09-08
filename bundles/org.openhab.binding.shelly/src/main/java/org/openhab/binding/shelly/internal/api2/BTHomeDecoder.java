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
package org.openhab.binding.shelly.internal.api2;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * {@link BTHomeDecoder} decodes a BTHome v2 sensor payload (https://bthome.io/format/).
 *
 * <p>
 * This used to run on-device inside {@code oh-blu-scanner.js}. It was ported here so the mJS script
 * only has to do cheap pre-filtering (BTHome service UUID match, device-info-byte encryption/version
 * check, packet-id peek for duplicate-packet dropping) and forward the raw payload, freeing up script
 * size/CPU time on the constrained Shelly gateway. The device-info byte (encryption flag + BTHome
 * version) is validated on-device before the raw payload is ever forwarded, so it is not re-checked
 * here.
 * </p>
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BTHomeDecoder {
    // Bump together with EVENT_DATA_VERSION in oh-blu-scanner.js whenever the wire format changes
    static final int SCRIPT_DATA_VERSION = 2;

    private static final int DIMMER_STEPS_ID = 0x3c;
    private static final int TEXT_ID = 0x53;
    private static final int RAW_ID = 0x54;

    private enum Type {
        UINT8,
        INT8,
        UINT16,
        INT16,
        UINT24,
        INT24,
        UINT32,
        INT32
    }

    private static final class BthObject {
        private final String name;
        private final Type type;
        private final double factor;

        BthObject(String name, Type type, double factor) {
            this.name = name;
            this.type = type;
            this.factor = factor;
        }
    }

    private static final Map<Integer, BthObject> BTH = new HashMap<>();

    private static void put(int id, String name, Type type, double factor) {
        BTH.put(id, new BthObject(name, type, factor));
    }

    static {
        // https://bthome.io/format/ - object id => {name, type, scale factor}. pid (0x00) must stay in this
        // table even though the script already peeked it, or decoding would fail as "unknown type" immediately.
        put(0x00, "pid", Type.UINT8, 1);
        put(0x01, "Battery", Type.UINT8, 1);
        put(0x02, "Temperature", Type.INT16, 0.01);
        put(0x03, "Humidity", Type.UINT16, 0.01);
        put(0x04, "Pressure", Type.UINT24, 0.01);
        put(0x05, "Illuminance", Type.UINT24, 0.01);
        put(0x06, "Mass_kg", Type.UINT16, 0.01);
        put(0x07, "Mass_lb", Type.UINT16, 0.01);
        put(0x08, "Dewpoint", Type.INT16, 0.01);
        put(0x09, "Count", Type.UINT8, 1);
        put(0x0a, "Energy", Type.UINT24, 0.001);
        put(0x0b, "Power_W", Type.UINT24, 0.01);
        put(0x0c, "Voltage", Type.UINT16, 0.001);
        put(0x0d, "PM2_5", Type.UINT16, 1);
        put(0x0e, "PM10", Type.UINT16, 1);
        put(0x0f, "GenericBoolean", Type.UINT8, 1);
        put(0x10, "Power", Type.UINT8, 1);
        put(0x11, "Opening", Type.UINT8, 1);
        put(0x12, "Co2", Type.UINT16, 1);
        put(0x13, "TVOC", Type.UINT16, 1);
        put(0x14, "Moisture16", Type.UINT16, 0.01);
        put(0x15, "BatteryLow", Type.UINT8, 1);
        put(0x16, "BatteryCharging", Type.UINT8, 1);
        put(0x17, "CarbonMonoxide", Type.UINT8, 1);
        put(0x18, "Cold", Type.UINT8, 1);
        put(0x19, "Connectivity", Type.UINT8, 1);
        put(0x1a, "Door", Type.UINT8, 1);
        put(0x1b, "GarageDoor", Type.UINT8, 1);
        put(0x1c, "Gas", Type.UINT8, 1);
        put(0x1d, "Heat", Type.UINT8, 1);
        put(0x1e, "Light", Type.UINT8, 1);
        put(0x1f, "Lock", Type.UINT8, 1);
        put(0x20, "Moisture", Type.UINT8, 1);
        put(0x21, "Motion", Type.UINT8, 1);
        put(0x22, "Moving", Type.UINT8, 1);
        put(0x23, "Occupancy", Type.UINT8, 1);
        put(0x24, "Plug", Type.UINT8, 1);
        put(0x25, "Presence", Type.UINT8, 1);
        put(0x26, "Problem", Type.UINT8, 1);
        put(0x27, "Running", Type.UINT8, 1);
        put(0x28, "Safety", Type.UINT8, 1);
        put(0x29, "Smoke", Type.UINT8, 1);
        put(0x2a, "Sound", Type.UINT8, 1);
        put(0x2b, "Tamper", Type.UINT8, 1);
        put(0x2c, "Vibration", Type.UINT8, 1);
        put(0x2d, "Window", Type.UINT8, 1);
        put(0x2e, "Humidity", Type.UINT8, 1);
        put(0x2f, "Moisture8", Type.UINT8, 1);
        put(0x3a, "Button", Type.UINT8, 1);
        put(0x3c, "Dimmer", Type.UINT16, 1); // special-cased below, see DIMMER_STEPS_ID
        put(0x3d, "Count", Type.UINT16, 1);
        put(0x3e, "Count", Type.UINT32, 1);
        put(0x3f, "Rotation", Type.INT16, 0.1);
        put(0x40, "Distance_mm", Type.UINT16, 1);
        put(0x41, "Distance_m", Type.UINT16, 0.1);
        put(0x42, "Duration", Type.UINT24, 0.001);
        put(0x43, "Current", Type.UINT16, 0.001);
        put(0x44, "Speed", Type.UINT16, 0.01);
        put(0x45, "Temperature", Type.INT16, 0.1);
        put(0x46, "UVIndex", Type.UINT8, 0.1);
        put(0x47, "Volume", Type.UINT16, 0.1);
        put(0x48, "Volume", Type.UINT16, 1);
        put(0x49, "VolumeFlowRate", Type.UINT16, 0.001);
        put(0x4a, "Voltage", Type.UINT16, 0.1);
        put(0x4b, "Gas", Type.UINT24, 0.001);
        put(0x4c, "Gas", Type.UINT32, 0.001);
        put(0x4d, "Energy", Type.UINT32, 0.001);
        put(0x4e, "Volume", Type.UINT32, 0.001);
        put(0x4f, "Water", Type.UINT32, 0.001);
        put(0x50, "Timestamp", Type.UINT32, 1);
        put(0x51, "Acceleration", Type.UINT16, 0.001);
        put(0x52, "Gyroscope", Type.UINT16, 0.001);
        // Variable-length (length-prefixed); Type/factor are unused placeholders, see decode()'s TEXT_ID/RAW_ID
        // handling
        put(TEXT_ID, "bthText", Type.UINT8, 1); // UTF-8 string
        put(RAW_ID, "bthRaw", Type.UINT8, 1); // lower-case hex string
        put(0x55, "VolumeStorage", Type.UINT32, 0.001);
        put(0x56, "Conductivity", Type.UINT16, 1);
        put(0x57, "Temperature", Type.INT8, 1);
        put(0x58, "Temperature", Type.INT8, 0.35);
        put(0x59, "Count", Type.INT8, 1);
        put(0x5a, "Count", Type.INT16, 1);
        put(0x5b, "Count", Type.INT32, 1);
        put(0x5c, "Power_W", Type.INT32, 0.01);
        put(0x5d, "Current", Type.INT16, 0.001);
        put(0x5e, "Direction", Type.UINT16, 0.01);
        put(0x5f, "Precipitation", Type.UINT16, 0.1);
        put(0x60, "Channel", Type.UINT8, 1);
        put(0x61, "RotationalSpeed", Type.UINT16, 1);
        put(0x62, "SpeedSigned", Type.INT32, 0.000001);
        put(0x63, "AccelerationSigned", Type.INT32, 0.000001);
        put(0x64, "LightLevel", Type.UINT8, 1);
        put(0x65, "SettingsRevision", Type.UINT8, 1);
        put(0xF0, "DeviceId", Type.UINT16, 1);
        put(0xF1, "Firmware32", Type.UINT32, 1);
        put(0xF2, "Firmware24", Type.UINT24, 1);
    }

    private BTHomeDecoder() {
    }

    private static int byteSize(Type type) {
        switch (type) {
            case UINT8:
            case INT8:
                return 1;
            case UINT16:
            case INT16:
                return 2;
            case UINT24:
            case INT24:
                return 3;
            case UINT32:
            case INT32:
                return 4;
            default:
                return 0;
        }
    }

    private static long readValue(byte[] buffer, int offset, int size) {
        long value = 0;
        for (int i = 0; i < size; i++) {
            value |= (buffer[offset + i] & 0xFFL) << (8 * i);
        }
        return value;
    }

    private static double decodeValue(Type type, byte[] buffer, int offset) {
        int size = byteSize(type);
        long raw = readValue(buffer, offset, size);
        switch (type) {
            case INT8:
                return (byte) raw;
            case INT16:
                return (short) raw;
            case INT24:
                return raw >= 0x800000 ? raw - 0x1000000 : raw;
            case INT32:
                return (int) raw;
            default: // unsigned types
                return raw;
        }
    }

    /**
     * Decodes a BTHome v2 payload (hex string, device-info byte already stripped by the caller) into a
     * {@link JsonObject} using the same field names {@code oh-blu-scanner.js} used to emit when it still
     * decoded on-device, so the result can be merged into {@link Shelly2NotifyBluEventData} via Gson and
     * reuses that class's existing scalar/array handling.
     *
     * @param hex raw BTHome object bytes (without the leading device-info byte), lower-case hex, no separators
     * @return decoded fields; includes {@code "code": "BTH_UNKNOWN_TYPE"} if decoding stopped on an
     *         unrecognized object id (matching what the on-device decoder used to report)
     */
    static JsonObject decode(String hex) {
        JsonObject result = new JsonObject();
        byte[] buffer = hexToBytes(hex);
        if (buffer == null) {
            return result;
        }

        int offset = 0;
        while (offset < buffer.length) {
            int id = buffer[offset++] & 0xFF;
            BthObject obj = BTH.get(id);
            if (obj == null) {
                result.addProperty("code", "BTH_UNKNOWN_TYPE");
                break;
            }

            int size = byteSize(obj.type);
            if (offset + size > buffer.length) {
                break;
            }

            if (id == DIMMER_STEPS_ID) {
                if (offset + 2 > buffer.length) {
                    break;
                }
                JsonObject dimmer = new JsonObject();
                dimmer.addProperty("direction", buffer[offset] & 0xFF);
                dimmer.addProperty("steps", buffer[offset + 1] & 0xFF);
                result.add(obj.name, dimmer);
                offset += 2;
                continue;
            }

            if (id == TEXT_ID || id == RAW_ID) {
                int len = buffer[offset] & 0xFF;
                if (offset + 1 + len > buffer.length) {
                    break;
                }
                String payload = id == TEXT_ID ? new String(buffer, offset + 1, len, StandardCharsets.UTF_8)
                        : bytesToHex(buffer, offset + 1, len);
                result.addProperty(obj.name, payload);
                offset += 1 + len;
                continue;
            }

            double value = decodeValue(obj.type, buffer, offset) * obj.factor;
            addValue(result, obj.name, value);
            offset += size;
        }

        return result;
    }

    // DTO fields with an array adapter (repeated BTHome objects, e.g. WS90 Speed/Direction); every other
    // name is a scalar field, so merging a JsonArray onto it would make Gson's fromJson throw.
    private static final Set<String> ARRAY_CAPABLE_NAMES = Set.of("Button", "Temperature", "Rotation", "Speed",
            "Direction");

    private static void addValue(JsonObject result, String name, double value) {
        JsonElement existing = result.get(name);
        JsonPrimitive number = isWholeNumber(value) ? new JsonPrimitive((long) value) : new JsonPrimitive(value);
        if (!ARRAY_CAPABLE_NAMES.contains(name)) {
            result.add(name, number); // scalar field: last reading wins
        } else if (existing == null) {
            result.add(name, number);
        } else if (existing.isJsonArray()) {
            existing.getAsJsonArray().add(number);
        } else {
            JsonArray array = new JsonArray();
            array.add(existing);
            array.add(number);
            result.add(name, array);
        }
    }

    private static boolean isWholeNumber(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value);
    }

    private static String bytesToHex(byte[] buffer, int offset, int len) {
        StringBuilder hex = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            hex.append(String.format("%02x", buffer[offset + i] & 0xFF));
        }
        return hex.toString();
    }

    private static byte @Nullable [] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            return null;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                return null;
            }
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }
}
