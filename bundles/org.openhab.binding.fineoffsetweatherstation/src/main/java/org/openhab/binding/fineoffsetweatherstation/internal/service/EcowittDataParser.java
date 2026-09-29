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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.HttpBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.HttpGroup;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasurandRegistry;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Parses the JSON responses of the Ecowitt HTTP API into the same domain objects the TCP parser produces.
 * <p>
 * The measurand/unit knowledge lives in
 * {@link org.openhab.binding.fineoffsetweatherstation.internal.domain.Measurand} and
 * {@link org.openhab.binding.fineoffsetweatherstation.internal.domain.MeasureType}; this class only walks the JSON
 * structure and dispatches each value to the matching measurand.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class EcowittDataParser {

    private static final String DISABLED_SENSOR = "FFFFFFFE";
    private static final String REGISTERING_SENSOR = "FFFFFFFF";

    private final Logger logger = LoggerFactory.getLogger(EcowittDataParser.class);
    private final MeasurandRegistry registry = MeasurandRegistry.standard();

    /**
     * Parses a {@code get_livedata_info} response into the measured values.
     */
    public List<MeasuredValue> parseLiveData(String json) {
        List<MeasuredValue> result = new ArrayList<>();
        JsonObject root = parseObject(json);
        if (root == null) {
            return result;
        }
        for (HttpGroup group : HttpGroup.values()) {
            JsonElement element = root.get(group.getJsonKey());
            if (element == null) {
                continue;
            }
            switch (group.getKeying()) {
                case CODE:
                    if (element.isJsonArray()) {
                        for (JsonElement entry : element.getAsJsonArray()) {
                            parseCodeEntry(group, entry.getAsJsonObject(), result);
                        }
                    }
                    break;
                case FIELD:
                    forEachObject(element, obj -> parseFields(group, obj, null, result));
                    break;
                case CHANNEL:
                    forEachObject(element, obj -> {
                        Integer channel = obj.has("channel") ? parseIntOrNull(asString(obj, "channel")) : null;
                        parseFields(group, obj, channel, result);
                    });
                    break;
            }
        }
        return result;
    }

    private void parseCodeEntry(HttpGroup group, JsonObject entry, List<MeasuredValue> result) {
        if (!entry.has("id") || !entry.has("val")) {
            return;
        }
        String id = entry.get("id").getAsString();
        String val = entry.get("val").getAsString();
        @Nullable
        String unit = entry.has("unit") ? entry.get("unit").getAsString() : null;
        @Nullable
        HttpBinding binding = registry.http(group, id);
        if (binding == null) {
            logger.debug("no measurand for id '{}' in group '{}'", id, group.getJsonKey());
            return;
        }
        addMeasuredValue(binding, val, unit, null, result, group, id);
    }

    private void parseFields(HttpGroup group, JsonObject obj, @Nullable Integer channel, List<MeasuredValue> result) {
        @Nullable
        String unit = obj.has("unit") ? obj.get("unit").getAsString() : null;
        for (Map.Entry<String, JsonElement> field : obj.entrySet()) {
            @Nullable
            HttpBinding binding = registry.http(group, field.getKey());
            if (binding == null) {
                // structural fields like "channel", "name", "unit", "battery" simply have no measurand
                continue;
            }
            JsonElement value = field.getValue();
            if (!value.isJsonPrimitive()) {
                continue;
            }
            addMeasuredValue(binding, value.getAsString(), unit, channel, result, group, field.getKey());
        }
    }

    private void addMeasuredValue(HttpBinding binding, String val, @Nullable String unit, @Nullable Integer channel,
            List<MeasuredValue> result, HttpGroup group, String key) {
        @Nullable
        MeasuredValue measuredValue = binding.parse(val, unit, channel, null);
        if (measuredValue == null) {
            logger.debug("could not parse value '{}' (unit '{}') for '{}' in group '{}'", val, unit, key,
                    group.getJsonKey());
            return;
        }
        result.add(measuredValue);
    }

    /**
     * Parses the {@code get_sensors_info} pages into the registered sensors.
     *
     * @param pages the JSON of each requested page
     * @param useWh24 whether the gateway uses a WH24 (vs. WH65) for the sensor sharing index 0
     */
    public Map<SensorGatewayBinding, SensorDevice> parseSensors(List<String> pages, boolean useWh24) {
        Map<SensorGatewayBinding, SensorDevice> result = new LinkedHashMap<>();
        for (String page : pages) {
            JsonArray array;
            try {
                array = JsonParser.parseString(page).getAsJsonArray();
            } catch (RuntimeException e) {
                logger.debug("failed to parse get_sensors_info page", e);
                continue;
            }
            for (JsonElement element : array) {
                parseSensor(element.getAsJsonObject(), useWh24, result);
            }
        }
        return result;
    }

    private void parseSensor(JsonObject obj, boolean useWh24, Map<SensorGatewayBinding, SensorDevice> result) {
        String idString = asString(obj, "id");
        if (idString.isEmpty() || DISABLED_SENSOR.equalsIgnoreCase(idString)
                || REGISTERING_SENSOR.equalsIgnoreCase(idString) || "0".equals(asString(obj, "idst"))) {
            return;
        }
        Integer type = parseIntOrNull(asString(obj, "type"));
        if (type == null) {
            return;
        }
        List<SensorGatewayBinding> candidates = SensorGatewayBinding.forIndex((byte) (int) type);
        if (candidates == null || candidates.isEmpty()) {
            logger.debug("unknown sensor type {}", type);
            return;
        }
        SensorGatewayBinding sensorGatewayBinding;
        if (candidates.size() == 1) {
            sensorGatewayBinding = candidates.getFirst();
        } else if (candidates.size() == 2 && type == 0) {
            sensorGatewayBinding = useWh24 ? SensorGatewayBinding.WH24 : SensorGatewayBinding.WH65;
        } else {
            logger.debug("ambiguous sensor type {}: {}", type, candidates);
            return;
        }
        int id;
        try {
            id = (int) Long.parseLong(idString, 16);
        } catch (NumberFormatException e) {
            return;
        }
        BatteryStatus batteryStatus = sensorGatewayBinding.getHttpBatteryStatus((byte) parseInt(asString(obj, "batt")));
        int signal = parseInt(asString(obj, "signal"));
        result.put(sensorGatewayBinding, new SensorDevice(id, sensorGatewayBinding, batteryStatus, signal));
    }

    /**
     * Parses the {@code get_device_info} response into the system information.
     */
    public @Nullable SystemInfo parseSystemInfo(String json) {
        JsonObject root = parseObject(json);
        if (root == null) {
            return null;
        }
        Integer frequency = switch (asString(root, "rf_freq")) {
            case "0" -> 433;
            case "1" -> 868;
            case "2" -> 915;
            case "3" -> 920;
            default -> null;
        };
        boolean useWh24 = "0".equals(asString(root, "sensorType"));
        boolean dst = "1".equals(asString(root, "dst_stat"));
        LocalDateTime dateTime = LocalDateTime.now();
        String date = asString(root, "date");
        if (!date.isEmpty()) {
            try {
                dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            } catch (RuntimeException e) {
                logger.debug("could not parse device date '{}'", date);
            }
        }
        return new SystemInfo(frequency, dateTime, dst, useWh24);
    }

    /**
     * Extracts the firmware version from a {@code get_version} response, normalized to the same format the TCP
     * protocol reports (e.g. {@code "GW1200A_V1.4.7"}).
     */
    public @Nullable String parseFirmwareVersion(String json) {
        JsonObject root = parseObject(json);
        if (root == null) {
            return null;
        }
        String version = asString(root, "version");
        if (version.isEmpty()) {
            return null;
        }
        // strip a leading "Version: " prefix
        int colon = version.indexOf(':');
        return colon >= 0 ? version.substring(colon + 1).trim() : version.trim();
    }

    /**
     * @return the number of {@code get_sensors_info} pages advertised by a {@code get_version} response, or 1
     */
    public int parseSensorPageCount(String json) {
        JsonObject root = parseObject(json);
        if (root == null) {
            return 1;
        }
        Integer pages = parseIntOrNull(asString(root, "sensorid_page"));
        return pages == null || pages < 1 ? 1 : pages;
    }

    /**
     * @return whether a {@code get_version} response identifies an Ecowitt HTTP API capable gateway
     */
    public boolean isEcowittPlatform(String json) {
        JsonObject root = parseObject(json);
        return root != null && "ecowitt".equalsIgnoreCase(asString(root, "platform"));
    }

    private @Nullable JsonObject parseObject(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.debug("failed to parse JSON response", e);
            return null;
        }
    }

    private void forEachObject(JsonElement element, java.util.function.Consumer<JsonObject> consumer) {
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                if (entry.isJsonObject()) {
                    consumer.accept(entry.getAsJsonObject());
                }
            }
        } else if (element.isJsonObject()) {
            consumer.accept(element.getAsJsonObject());
        }
    }

    private static String asString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }

    private static int parseInt(String value) {
        Integer parsed = parseIntOrNull(value);
        return parsed == null ? 0 : parsed;
    }

    private static @Nullable Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
