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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.binding.dreame.internal.model.DreameMap;
import org.openhab.binding.dreame.internal.model.DreameMapData;
import org.openhab.binding.dreame.internal.model.DreameMapGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPathGeometry;
import org.openhab.binding.dreame.internal.model.DreameMapPoint;
import org.openhab.binding.dreame.internal.model.DreameMapZoneGeometry;
import org.openhab.binding.dreame.internal.model.DreameMowingStatistics;
import org.openhab.binding.dreame.internal.model.DreameProperty;
import org.openhab.binding.dreame.internal.model.DreameStatus;
import org.openhab.binding.dreame.internal.model.DreameZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Converts Dreamehome API responses into binding model objects.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
final class DreameApiResponseParser {

    List<DreameDevice> parseDevices(JsonObject response) throws DreameCloudException {
        JsonObject data = objectValue(response, "data");
        JsonObject page = objectValue(data, "page");
        JsonArray records = page.has("records") && page.get("records").isJsonArray() ? page.getAsJsonArray("records")
                : new JsonArray();
        List<DreameDevice> devices = new ArrayList<>();
        for (JsonElement element : records) {
            if (element instanceof JsonObject device) {
                String id = stringValue(device, "did");
                String model = stringValue(device, "model");
                if (!id.isBlank() && isMowerModel(model)) {
                    String displayName = defaultIfBlank(stringValue(device, "name"), stringValue(device, "customName"));
                    if (displayName.isBlank() && device.get("deviceInfo") instanceof JsonObject deviceInfo) {
                        displayName = stringValue(deviceInfo, "displayName");
                    }
                    devices.add(new DreameDevice(id, defaultIfBlank(displayName, model), model,
                            stringValue(device, "ver"), stringValue(device, "masterUid"),
                            stringValue(device, "bindDomain"), stringValue(device, "property")));
                }
            }
        }
        return List.copyOf(devices);
    }

    private static boolean isMowerModel(String model) {
        return model.startsWith("dreame.mower.") || model.startsWith("mova.mower.");
    }

    DreameStatus parseProperties(JsonArray result, List<DreameProperty> requested) {
        Map<Integer, DreameProperty> propertiesById = requested.stream()
                .collect(java.util.stream.Collectors.toMap(DreameProperty::id, property -> property));
        Map<String, DreameProperty> propertiesByAddress = requested.stream().collect(java.util.stream.Collectors
                .toMap(property -> propertyAddress(property.serviceId(), property.propertyId()), property -> property));
        DreameStatus status = new DreameStatus();
        for (JsonElement element : result) {
            if (element instanceof JsonObject property && property.has("value")
                    && (!property.has("code") || property.get("code").getAsInt() == 0)) {
                try {
                    DreameProperty mappedProperty = property.has("siid") && property.has("piid")
                            ? propertiesByAddress.get(
                                    propertyAddress(property.get("siid").getAsInt(), property.get("piid").getAsInt()))
                            : null;
                    if (mappedProperty == null && property.has("did")) {
                        mappedProperty = propertiesById.get(Integer.parseInt(property.get("did").getAsString()));
                    }
                    if (mappedProperty != null) {
                        status.put(mappedProperty, property.get("value"));
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore properties not represented by a numeric Dreame property id.
                }
            }
        }
        return status;
    }

    private static String propertyAddress(int serviceId, int propertyId) {
        return serviceId + ":" + propertyId;
    }

    DreameMowingStatistics parseMowingStatistics(JsonObject response) throws DreameCloudException {
        JsonObject data = objectValue(response, "data");
        JsonArray events = data.get("list") instanceof JsonArray list ? list : new JsonArray();
        int sessions = 0;
        int totalMinutes = 0;
        long totalAreaHundredths = 0;
        for (JsonElement element : events) {
            if (!(element instanceof JsonObject event)) {
                continue;
            }
            JsonArray arguments = historyArguments(event.has("history") ? event.get("history") : event.get("value"));
            if (arguments == null) {
                continue;
            }
            sessions++;
            for (JsonElement argumentElement : arguments) {
                if (argumentElement instanceof JsonObject argument && argument.has("piid") && argument.has("value")) {
                    int propertyId = argument.get("piid").getAsInt();
                    if (propertyId == 2) {
                        totalMinutes += argument.get("value").getAsInt();
                    } else if (propertyId == 3) {
                        totalAreaHundredths += argument.get("value").getAsLong();
                    }
                }
            }
        }
        return new DreameMowingStatistics(sessions, totalMinutes, BigDecimal.valueOf(totalAreaHundredths, 2));
    }

    DreameMapData parseMapData(JsonObject batchData, JsonElement mapListResult) throws DreameCloudException {
        StringBuilder raw = new StringBuilder();
        batchData.entrySet().stream().filter(entry -> entry.getKey().matches("MAP\\.\\d+"))
                .sorted(java.util.Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().substring(4))))
                .forEach(entry -> raw.append(entry.getValue().getAsString()));
        String info = stringValue(batchData, "MAP.info");
        if (info.matches("\\d+") && Integer.parseInt(info) < raw.length()) {
            raw.setLength(Integer.parseInt(info));
        }
        List<DreameMap> maps = new ArrayList<>();
        Map<Integer, List<DreameZone>> zonesByMapId = new LinkedHashMap<>();
        List<DreameMapGeometry> geometries = new ArrayList<>();
        try {
            JsonElement assembled = raw.isEmpty() ? new JsonArray() : JsonParser.parseString(raw.toString());
            if (assembled instanceof JsonArray mapEntries) {
                for (JsonElement mapEntry : mapEntries) {
                    JsonObject map = JsonParser.parseString(mapEntry.getAsString()).getAsJsonObject();
                    int index = map.has("mapIndex") ? map.get("mapIndex").getAsInt() : maps.size();
                    DreameMap descriptor = new DreameMap(index + 1, index, stringValue(map, "name"),
                            decimalValue(map, "totalArea"));
                    List<DreameMapZoneGeometry> mapZones = new ArrayList<>();
                    List<DreameZone> mapZoneDescriptors = new ArrayList<>();
                    if (map.get("mowingAreas") instanceof JsonObject areas
                            && areas.get("value") instanceof JsonArray values) {
                        for (JsonElement value : values) {
                            if (value instanceof JsonArray pair && pair.size() >= 2
                                    && pair.get(1) instanceof JsonObject zone) {
                                mapZoneDescriptors.add(new DreameZone(pair.get(0).getAsInt(), stringValue(zone, "name"),
                                        decimalValue(zone, "area")));
                                mapZones.add(new DreameMapZoneGeometry(pair.get(0).getAsInt(),
                                        stringValue(zone, "name"), parsePoints(zone.get("path"))));
                            }
                        }
                    }
                    List<DreameMapZoneGeometry> forbiddenAreas = parseZoneGeometries(map, "forbiddenAreas");
                    List<DreameMapPathGeometry> paths = parsePathGeometries(map);
                    DreameMapGeometry geometry = parseGeometry(index + 1, map, mapZones, forbiddenAreas, paths);
                    if (!isEmptyMapPlaceholder(descriptor, geometry)) {
                        maps.add(descriptor);
                        zonesByMapId.put(descriptor.id(), mapZoneDescriptors);
                        geometries.add(geometry);
                    }
                }
            }
        } catch (JsonParseException | IllegalStateException | NumberFormatException e) {
            throw new DreameCloudException("Cloud service returned invalid map data", e);
        }
        int currentMapId = currentMapId(mapListResult);
        List<DreameZone> activeMapZones = zonesByMapId.getOrDefault(currentMapId, List.of());
        return new DreameMapData(currentMapId, maps, activeMapZones, geometries);
    }

    private boolean isEmptyMapPlaceholder(DreameMap map, DreameMapGeometry geometry) {
        return map.name().isBlank() && map.area().signum() == 0 && geometry.zones().isEmpty()
                && geometry.forbiddenAreas().isEmpty() && geometry.paths().isEmpty();
    }

    private DreameMapGeometry parseGeometry(int mapId, JsonObject map, List<DreameMapZoneGeometry> zones,
            List<DreameMapZoneGeometry> forbiddenAreas, List<DreameMapPathGeometry> paths) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        if (map.get("boundary") instanceof JsonObject boundary) {
            minX = boundary.get("x1").getAsInt();
            minY = boundary.get("y1").getAsInt();
            maxX = boundary.get("x2").getAsInt();
            maxY = boundary.get("y2").getAsInt();
        } else {
            for (DreameMapZoneGeometry zone : zones) {
                for (DreameMapPoint point : zone.points()) {
                    minX = Math.min(minX, point.x());
                    minY = Math.min(minY, point.y());
                    maxX = Math.max(maxX, point.x());
                    maxY = Math.max(maxY, point.y());
                }
            }
        }
        return new DreameMapGeometry(mapId, minX, minY, maxX, maxY, zones, forbiddenAreas, paths);
    }

    private List<DreameMapZoneGeometry> parseZoneGeometries(JsonObject map, String property) {
        if (!(map.get(property) instanceof JsonObject areas) || !(areas.get("value") instanceof JsonArray values)) {
            return List.of();
        }
        List<DreameMapZoneGeometry> result = new ArrayList<>();
        for (JsonElement value : values) {
            if (value instanceof JsonArray pair && pair.size() >= 2 && pair.get(1) instanceof JsonObject area) {
                result.add(new DreameMapZoneGeometry(pair.get(0).getAsInt(), stringValue(area, "name"),
                        parsePoints(area.get("path"))));
            }
        }
        return result;
    }

    private List<DreameMapPathGeometry> parsePathGeometries(JsonObject map) {
        if (!(map.get("paths") instanceof JsonObject paths) || !(paths.get("value") instanceof JsonArray values)) {
            return List.of();
        }
        List<DreameMapPathGeometry> result = new ArrayList<>();
        for (JsonElement value : values) {
            if (value instanceof JsonArray pair && pair.size() >= 2 && pair.get(1) instanceof JsonObject path) {
                int type = path.has("type") ? path.get("type").getAsInt() : 0;
                result.add(new DreameMapPathGeometry(pair.get(0).getAsInt(), type, parsePoints(path.get("path"))));
            }
        }
        return result;
    }

    private List<DreameMapPoint> parsePoints(@Nullable JsonElement path) {
        if (!(path instanceof JsonArray points)) {
            return List.of();
        }
        List<DreameMapPoint> result = new ArrayList<>();
        for (JsonElement point : points) {
            if (point instanceof JsonObject value && value.has("x") && value.has("y")) {
                result.add(new DreameMapPoint(value.get("x").getAsInt(), value.get("y").getAsInt()));
            }
        }
        return result;
    }

    private int currentMapId(JsonElement result) {
        if (!(result instanceof JsonObject object) || !(object.get("out") instanceof JsonArray out)) {
            return 0;
        }
        for (JsonElement entry : out) {
            if (entry instanceof JsonObject item && item.get("d") instanceof JsonArray mapEntries) {
                for (JsonElement mapEntry : mapEntries) {
                    if (mapEntry instanceof JsonArray values && values.size() >= 2 && isTrue(values.get(1))) {
                        return values.get(0).getAsInt() + 1;
                    }
                }
            }
        }
        return 0;
    }

    private boolean isTrue(JsonElement value) {
        if (!value.isJsonPrimitive()) {
            return false;
        }
        if (value.getAsJsonPrimitive().isNumber()) {
            return value.getAsInt() != 0;
        }
        return value.getAsBoolean();
    }

    private BigDecimal decimalValue(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? BigDecimal.ZERO : value.getAsBigDecimal();
    }

    private @Nullable JsonArray historyArguments(@Nullable JsonElement payload) {
        if (payload == null || payload.isJsonNull()) {
            return null;
        }
        try {
            JsonElement parsed = payload.isJsonPrimitive() && payload.getAsJsonPrimitive().isString()
                    ? JsonParser.parseString(payload.getAsString())
                    : payload;
            return parsed instanceof JsonArray array ? array : null;
        } catch (JsonParseException e) {
            return null;
        }
    }

    private JsonObject objectValue(JsonObject object, String name) throws DreameCloudException {
        if (object.has(name) && object.get(name) instanceof JsonObject value) {
            return value;
        }
        throw new DreameCloudException("Cloud response is missing " + name);
    }

    private String stringValue(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value.isBlank() ? fallback : value;
    }
}
