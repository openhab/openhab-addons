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
package org.openhab.binding.dreame.internal.api;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dreame.internal.model.DreameMowerHeartbeat;
import org.openhab.binding.dreame.internal.model.DreameMowerPose;
import org.openhab.binding.dreame.internal.model.DreameMowerTask;
import org.openhab.binding.dreame.internal.model.DreameMowerTaskStatus;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Decodes Dreame MQTT events and proprietary mower telemetry frames.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
final class DreameMqttMessageParser {
    DreameStatus parse(String payload, List<DreameProperty> requested) throws DreameCloudException {
        try {
            JsonElement parsed = JsonParser.parseString(payload);
            if (!(parsed instanceof JsonObject root) || !(root.get("data") instanceof JsonObject data)
                    || !data.has("params")) {
                return new DreameStatus();
            }
            DreameStatus status = new DreameStatus();
            if ("event_occured".equals(stringValue(data, "method")) && data.get("params") instanceof JsonObject event
                    && event.has("siid") && event.has("eiid") && event.get("siid").getAsInt() == 4
                    && event.get("eiid").getAsInt() == 1) {
                status.setMissionCompleted();
                return status;
            }
            if (!"properties_changed".equals(stringValue(data, "method")) || !data.get("params").isJsonArray()) {
                return status;
            }
            for (JsonElement element : data.getAsJsonArray("params")) {
                if (element instanceof JsonObject parameter && parameter.has("siid") && parameter.has("piid")
                        && parameter.has("value")) {
                    parseParameter(parameter, requested, status);
                }
            }
            return status;
        } catch (JsonParseException | IllegalStateException e) {
            throw new DreameCloudException("Dreame MQTT payload is invalid", e);
        }
    }

    private void parseParameter(JsonObject parameter, List<DreameProperty> requested, DreameStatus status) {
        int serviceId = parameter.get("siid").getAsInt();
        int propertyId = parameter.get("piid").getAsInt();
        JsonElement value = parameter.get("value");
        if (serviceId == 1 && propertyId == 1 && value.isJsonArray()) {
            parseHeartbeat(value.getAsJsonArray(), requested, status);
        } else if (serviceId == 1 && propertyId == 4 && value.isJsonArray()) {
            parsePose(value.getAsJsonArray(), status);
        } else if (serviceId == 2 && propertyId == 50 && value.isJsonObject()) {
            parseTaskStatus(value.getAsJsonObject(), status);
        } else if (serviceId == 2 && propertyId == 56 && value.isJsonObject()) {
            parseTaskActivity(value.getAsJsonObject(), status);
        } else {
            requested.stream().filter(p -> p.serviceId() == serviceId && p.propertyId() == propertyId).findFirst()
                    .ifPresent(property -> status.put(property, value));
        }
    }

    private void parseTaskActivity(JsonObject value, DreameStatus status) {
        if (!(value.get("status") instanceof JsonArray entries) || entries.isEmpty()
                || !(entries.get(0) instanceof JsonArray entry) || entry.size() < 2) {
            return;
        }
        try {
            int taskId = entry.get(0).getAsInt();
            int activity = entry.get(1).getAsInt();
            if (taskId == 1 && (activity == 0 || activity == 4)) {
                status.setMowerTaskActive(activity == 0);
            }
        } catch (NumberFormatException | IllegalStateException e) {
            // Ignore malformed proprietary task activity.
        }
    }

    private void parseTaskStatus(JsonObject value, DreameStatus status) {
        if (!"TASK".equals(stringValue(value, "t")) || !(value.get("d") instanceof JsonObject data) || !data.has("exe")
                || !data.has("o")) {
            return;
        }
        try {
            List<Integer> regionIds = data.has("region_id") && data.get("region_id").isJsonArray()
                    ? data.getAsJsonArray("region_id").asList().stream().map(JsonElement::getAsInt).toList()
                    : List.of();
            Long time = data.has("time") && !data.get("time").isJsonNull() ? data.get("time").getAsLong() : null;
            status.setMowerTaskStatus(new DreameMowerTaskStatus(data.get("exe").getAsBoolean(),
                    data.get("o").getAsInt(), time, regionIds));
        } catch (NumberFormatException | IllegalStateException e) {
            // Ignore malformed proprietary task status.
        }
    }

    private void parseHeartbeat(JsonArray value, List<DreameProperty> requested, DreameStatus status) {
        int[] bytes = frame(value, Set.of(20));
        if (bytes.length == 0) {
            return;
        }
        int battery = bytes[11] & 0x7F;
        if (battery <= 100 && requested.contains(DreameProperty.BATTERY_LEVEL)) {
            status.put(DreameProperty.BATTERY_LEVEL, new JsonPrimitive(battery));
        }
        if ((bytes[11] & 0x80) != 0 && requested.contains(DreameProperty.CHARGING_STATUS)) {
            status.put(DreameProperty.CHARGING_STATUS, new JsonPrimitive(1));
        }
        int robotState = bytes[14];
        status.setMowerHeartbeat(new DreameMowerHeartbeat(robotState & 3, (robotState & 0x1C) >> 2,
                signedByte(bytes[16]), signedByte(bytes[17]), signedByte(bytes[18])));
    }

    private void parsePose(JsonArray value, DreameStatus status) {
        int[] bytes = frame(value, Set.of(7, 8, 10, 13, 22, 33, 44));
        if (bytes.length == 0) {
            return;
        }
        int x = ((bytes[3] << 28) | (bytes[2] << 20) | (bytes[1] << 12)) >> 12;
        int y = ((bytes[5] << 24) | (bytes[4] << 16) | (bytes[3] << 8)) >> 12;
        double heading = Math.round(bytes[6] / 255.0 * 36000.0) / 100.0;
        status.setMowerPose(new DreameMowerPose(x * 10, y * 10, heading));
        if (bytes.length == 33 || bytes.length == 44) {
            status.setMowerTask(new DreameMowerTask(bytes[22], bytes[23], ((bytes[25] << 8) | bytes[24]) / 100.0,
                    ((bytes[28] << 16) | (bytes[27] << 8) | bytes[26]) / 100.0,
                    ((bytes[31] << 16) | (bytes[30] << 8) | bytes[29]) / 100.0));
        }
    }

    private int[] frame(JsonArray value, Set<Integer> lengths) {
        if (!lengths.contains(value.size())) {
            return new int[0];
        }
        int[] bytes = new int[value.size()];
        try {
            for (int i = 0; i < value.size(); i++) {
                bytes[i] = value.get(i).getAsInt();
                if (bytes[i] < 0 || bytes[i] > 255) {
                    return new int[0];
                }
            }
        } catch (NumberFormatException | IllegalStateException e) {
            return new int[0];
        }
        return bytes[0] == 0xCE && bytes[bytes.length - 1] == 0xCE ? bytes : new int[0];
    }

    private int signedByte(int value) {
        return value > 127 ? value - 256 : value;
    }

    private String stringValue(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }
}
