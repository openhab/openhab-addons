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
package org.openhab.binding.hasslink.internal.api.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.entity.EntityId;
import org.openhab.binding.hasslink.internal.util.EntityUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link EntityState} DTO represents a single Home Assistant entity state, as received in the
 * {@code openhab_bridge/get_entities} snapshot response or in a {@code state_changed} event payload.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record EntityState(String entityId, String state, Map<String, JsonElement> attributes) {

    /**
     * Standard Home Assistant internal metadata attribute keys that should not be auto-discovered as channels.
     */
    public static final Set<String> DEFAULT_METADATA_ATTRIBUTES = Set.of( //
            "friendly_name", //
            "icon", //
            "supported_features", //
            "supported_color_modes", //
            "unit_of_measurement", //
            "min", "max", "step", //
            "options", //
            "device_class", //
            "state_class", //
            "restored", //
            "entity_picture", //
            "assumed_state" //
    );

    /**
     * Compact constructor: Runs automatically during Gson deserialization.
     * Enforces non null entityId and state at the parsing boundary.
     */
    public EntityState {
        Objects.requireNonNull(entityId, "entity_id must not be null");
        if (entityId.isBlank()) {
            throw new IllegalArgumentException("entity_id cannot be blank");
        }

        state = Objects.requireNonNullElse(state, "");
        attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }

    public String getDomain() {
        EntityId parsedId = EntityUtils.parseEntityId(entityId);
        return parsedId != null ? parsedId.domain() : "";
    }

    public String getObjectId() {
        EntityId parsedId = EntityUtils.parseEntityId(entityId);
        return parsedId != null ? parsedId.objectId() : "";
    }

    /**
     * Checks if the entity state is absent, empty, or set to Home Assistant's special non-operational states
     * ({@code "unavailable"} or {@code "unknown"}).
     *
     * @return {@code true} if state is null, blank, "unavailable", or "unknown"; {@code false} otherwise.
     */
    static public boolean isUnavailableOrUnknown(String stringState) {
        return stringState.isBlank() || "unavailable".equalsIgnoreCase(stringState)
                || "unknown".equalsIgnoreCase(stringState);
    }

    public boolean isUnavailableOrUnknown() {
        return isUnavailableOrUnknown(state);
    }

    public @Nullable BigDecimal getStateAsBigDecimal() {
        if (state.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(state.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Retrieves the unit of measurement for the main entity state.
     */
    public @Nullable String getUnitOfMeasurement() {
        return getAttributeAsString("unit_of_measurement");
    }

    /**
     * Retrieves a unit string from a specific attribute key (or defaults to {@code unit_of_measurement} if null).
     *
     * @param attribute the attribute key (e.g., "target_temp_step")
     * @return the unit string, or {@code null} if not defined
     */
    public @Nullable String getUnitOfMeasurement(String attribute) {
        String unit = getAttributeAsString(attribute);
        return unit != null ? unit : getUnitOfMeasurement();
    }

    /**
     * Checks whether an attribute key is standard HA metadata rather than a device state property.
     */
    /**
     * Checks if an attribute key is considered Home Assistant metadata rather than state telemetry.
     */
    public static boolean isMetadataAttribute(String attribute) {
        if (attribute.isBlank()) {
            return true;
        }

        // Standard Home Assistant structural metadata keys
        if (DEFAULT_METADATA_ATTRIBUTES.contains(attribute)) {
            return true;
        }

        String lower = attribute.toLowerCase();

        // Mode and Option list metadata (e.g., fan_speed_list, hvac_modes, preset_modes, sound_mode_list)
        if (lower.endsWith("_list") || lower.endsWith("_modes") || lower.endsWith("_options")
                || "options".equals(lower)) {
            return true;
        }

        // Numeric range / step constraints (e.g., min, max, step, target_temp_step, min_temp, max_temp)
        if ("min".equals(lower) || lower.endsWith("_min") || "max".equals(lower) || lower.endsWith("_max")
                || "step".equals(lower) || lower.endsWith("_step")) {
            return true;
        }

        return false;
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public boolean isAttributeUnavailableOrUnknown(String attribute) {
        String value = getAttributeAsString(attribute);
        return value == null ? true : isUnavailableOrUnknown(value);
    }

    public @Nullable JsonElement getAttribute(String name) {
        return attributes.get(name);
    }

    // Keeping the single-argument version for performance reasons
    public @Nullable String getAttributeAsString(String name) {
        JsonElement element = attributes.get(name);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    public @Nullable String getAttributeAsString(String firstName, String... fallbackNames) {
        String value = getAttributeAsString(firstName);
        if (value != null) {
            return value;
        }

        for (String fallbackName : fallbackNames) {
            String fallbackValue = getAttributeAsString(fallbackName);
            if (fallbackValue != null) {
                return fallbackValue;
            }
        }

        return null;
    }

    /**
     * Retrieves an attribute as a raw JSON string representation.
     * <p>
     * Serializes objects, arrays, and primitive values to their valid JSON string format
     * (e.g., {@code ["a", "b"]} or {@code {"key": "value"}}).
     *
     * @param name the attribute name
     * @return the JSON string representation of the attribute, or {@code null} if missing or null
     */
    public @Nullable String getAttributeAsJson(String name) {
        JsonElement element = attributes.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.toString();
    }

    public @Nullable Double getAttributeAsDouble(String name) {
        JsonElement element = attributes.get(name);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                ? element.getAsDouble()
                : null;
    }

    public @Nullable BigDecimal getAttributeAsBigDecimal(String name) {
        JsonElement element = attributes.get(name);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return new BigDecimal(element.getAsString());
        }
        return null;
    }

    /**
     * Retrieves an attribute as a Long without IEEE 754 floating-point precision loss.
     * Ideal for timestamps, exact counters, and raw bitmasks/IDs.
     */
    public @Nullable Long getAttributeAsLong(String name) {
        JsonElement element = attributes.get(name);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
                ? element.getAsLong()
                : null;
    }

    public @Nullable Integer getAttributeAsInt(String name) {
        JsonElement element = attributes.get(name);
        return (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber())
                ? element.getAsInt()
                : null;
    }

    public @Nullable Boolean getAttributeAsBoolean(String name) {
        JsonElement element = attributes.get(name);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                ? element.getAsBoolean()
                : null;
    }

    /**
     * Retrieves an attribute as a List of Objects, converting JSON arrays into Java Lists.
     * <p>
     * Each element is converted to its corresponding Java type (String, Number, Boolean, Map, List).
     *
     * @param name the attribute name
     * @return the List of Objects, or {@code null} if the attribute is missing or not an array
     */
    public @Nullable List<Object> getAttributeAsList(String name) {
        Object val = attributes.get(name);
        if (val instanceof List<?> list) {
            @SuppressWarnings("unchecked")
            List<Object> typedList = (List<Object>) list;
            return typedList;
        }
        if (val instanceof JsonElement jsonElement && jsonElement.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement elem : jsonElement.getAsJsonArray()) {
                list.add(convertJsonElement(elem));
            }
            return list;
        }
        return null;
    }

    private Object convertJsonElement(JsonElement elem) {
        if (elem.isJsonPrimitive()) {
            JsonPrimitive primitive = elem.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isString()) {
                return primitive.getAsString();
            }
        } else if (elem.isJsonObject()) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : elem.getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), convertJsonElement(entry.getValue()));
            }
            return map;
        } else if (elem.isJsonArray()) {
            List<Object> subList = new ArrayList<>();
            for (JsonElement subElem : elem.getAsJsonArray()) {
                subList.add(convertJsonElement(subElem));
            }
            return subList;
        }
        return elem.toString();
    }

    /**
     * Retrieves an attribute as a list of strings.
     * <p>
     * Supports both standard Java {@link List} instances and {@link com.google.gson.JsonArray} instances:
     * <ul>
     * <li>For Java lists, non-null elements are converted via {@link Object#toString()}.</li>
     * <li>For JSON arrays, primitive values (strings, numbers, booleans) are unwrapped to their string representation,
     * while complex JSON objects or arrays are serialized to JSON strings.</li>
     * </ul>
     *
     * @param key the attribute key
     * @return a list of string values, or an empty list if the attribute is missing, null, or not a list/JSON array
     */
    public List<String> getAttributeAsStringList(String key) {
        Object value = attributes.get(key);

        if (value instanceof List<?> list) {
            return list.stream().filter(Objects::nonNull).map(Object::toString).toList();
        }

        if (value instanceof JsonElement jsonElement && jsonElement.isJsonArray()) {
            List<String> list = new ArrayList<>();
            for (JsonElement elem : jsonElement.getAsJsonArray()) {
                if (elem != null && !elem.isJsonNull()) {
                    if (elem.isJsonPrimitive()) {
                        list.add(elem.getAsString());
                    } else {
                        list.add(elem.toString());
                    }
                }
            }
            return list;
        }

        return List.of();
    }

    /**
     * Checks if a specific feature bit is supported.
     *
     * @param featureBit the bitmask flag for the feature
     * @return true if supported; false otherwise
     */
    public boolean isSupportedFeature(long featureBit) {
        Long features = getAttributeAsLong("supported_features");
        if (features != null) {
            return (features & featureBit) != 0;
        }
        return false;
    }
}
