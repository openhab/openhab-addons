/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.salus.internal.rest;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.salus.internal.cloud.rest.AuthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The GsonMapper class is responsible for mapping JSON data to Java objects using the Gson library. It provides methods
 * for converting JSON strings to various types of objects, such as authentication tokens, devices, device properties,
 * and error messages.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class GsonMapper {
    public static final GsonMapper INSTANCE = new GsonMapper();
    private final Logger logger = LoggerFactory.getLogger(GsonMapper.class);
    private static final TypeToken<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeToken<>() {
    };
    private static final TypeToken<List<Object>> LIST_TYPE_REFERENCE = new TypeToken<>() {
    };
    private final Gson gson = new Gson();

    public String loginParam(String username, byte[] password) {
        return gson.toJson(Map.of("user", Map.of("email", username, "password", new String(password))));
    }

    public AuthToken authToken(String json) {
        return requireNonNull(gson.fromJson(json, AuthToken.class));
    }

    public List<Device> parseDevices(String json) {
        return tryParseBody(json, LIST_TYPE_REFERENCE, List.of()).stream().map(this::parseDevice)
                .filter(Optional::isPresent).map(Optional::get).toList();
    }

    @SuppressWarnings("unchecked")
    public List<Device> parseAwsDevices(String json) {
        var map = tryParseBody(json, MAP_TYPE_REFERENCE, Map.of());
        if (!map.containsKey("data")) {
            return List.of();
        }

        var rawData = map.get("data");
        Map<String, Object> data;
        if (rawData instanceof Map<?, ?> dataMap) {
            data = (Map<String, Object>) dataMap;
        } else {
            data = tryParseBody(rawData.toString(), MAP_TYPE_REFERENCE, Map.of());
        }
        if (!data.containsKey("items")) {
            return List.of();
        }

        var rawItems = data.get("items");
        List<Object> items;
        if (rawItems instanceof List<?>) {
            items = (List<Object>) rawItems;
        } else {
            items = tryParseBody(rawItems.toString(), LIST_TYPE_REFERENCE, List.of());
        }
        return items.stream()//
                .map(this::parseAwsDevice)//
                .filter(Optional::isPresent)//
                .map(Optional::get)//
                .toList();
    }

    private Optional<Device> parseDevice(Object obj) {
        if (!(obj instanceof Map<?, ?> firstLevelMap)) {
            logger.debug("Cannot parse device, because object is not type of map!\n{}", obj);
            return empty();
        }

        if (!firstLevelMap.containsKey("device")) {
            if (logger.isWarnEnabled()) {
                var str = firstLevelMap.entrySet().stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.debug("Cannot parse device, because firstLevelMap does not have [device] key!\n{}", str);
            }
            return empty();
        }
        var objLevel2 = firstLevelMap.get("device");

        if (!(objLevel2 instanceof Map<?, ?> map)) {
            logger.debug("Cannot parse device, because object is not type of map!\n{}", obj);
            return empty();
        }

        // parse `dns`
        if (!map.containsKey("dsn")) {
            if (logger.isWarnEnabled()) {
                var str = map.entrySet().stream().map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.debug("Cannot parse device, because map does not have [dsn] key!\n{}", str);
            }
            return empty();
        }
        var dsn = requireNonNull((String) map.get("dsn"));

        // parse `name`
        if (!map.containsKey("product_name")) {
            if (logger.isWarnEnabled()) {
                var str = map.entrySet().stream().map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.debug("Cannot parse device, because map does not have [product_name] key!\n{}", str);
            }
            return empty();
        }
        var name = requireNonNull((String) map.get("product_name"));

        // parse `properties`
        var list = map.entrySet().stream().filter(entry -> entry.getKey() != null)
                .filter(entry -> !"name".equals(entry.getKey())).filter(entry -> !"base_type".equals(entry.getKey()))
                .filter(entry -> !"read_only".equals(entry.getKey()))
                .filter(entry -> !"direction".equals(entry.getKey()))
                .filter(entry -> !"data_updated_at".equals(entry.getKey()))
                .filter(entry -> !"product_name".equals(entry.getKey()))
                .filter(entry -> !"display_name".equals(entry.getKey()))
                .filter(entry -> !"value".equals(entry.getKey()))
                .map(entry -> new Pair<>(requireNonNull(entry.getKey()).toString(), (Object) entry.getValue()))
                .toList();
        Map<@NotNull String, @Nullable Object> properties = new LinkedHashMap<>();
        for (var entry : list) {
            properties.put(entry.key, entry.value);
        }
        properties = Collections.unmodifiableMap(properties);

        return Optional.of(new Device(dsn.trim(), name.trim(), isConnected(properties), properties));
    }

    public boolean isConnected(Map<@NotNull String, @Nullable Object> properties) {
        if (properties.containsKey("connection_status")) {
            var connectionStatus = properties.get("connection_status");
            return connectionStatus != null && "online".equalsIgnoreCase(connectionStatus.toString());
        }
        return false;
    }

    private Optional<Device> parseAwsDevice(Object obj) {
        if (!(obj instanceof Map<?, ?> firstLevelMap)) {
            logger.debug("Cannot parse device, because object is not type of map!\n{}", obj);
            return empty();
        }

        var dsn = firstLevelMap.get("device_code");
        var name = firstLevelMap.get("name");
        if (dsn == null || name == null) {
            return empty();
        }
        Map<@Nullable String, @Nullable Object> properties = firstLevelMap.entrySet()//
                .stream()//
                .filter(entry -> !"device_code".equals(entry.getKey()))//
                .filter(entry -> !"name".equals(entry.getKey()))//
                .filter(entry -> entry.getKey() != null)//
                .filter(entry -> entry.getValue() != null)//
                .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))//
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

        return Optional.of(new Device(dsn.toString(), name.toString(), true, properties));
    }

    @SuppressWarnings("SameParameterValue")
    private <T> T tryParseBody(@Nullable String body, TypeToken<T> typeToken, T defaultValue) {
        if (body == null) {
            return defaultValue;
        }
        try {
            return gson.fromJson(body, typeToken);
        } catch (JsonSyntaxException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Error when parsing body!\n{}", body, e);
            } else {
                logger.debug("Error when parsing body! Turn on TRACE for more details", e);
            }
            return defaultValue;
        }
    }

    public List<DeviceProperty<?>> parseDeviceProperties(String json) {
        var deviceProperties = new ArrayList<DeviceProperty<?>>();
        var objects = tryParseBody(json, LIST_TYPE_REFERENCE, List.of());
        for (var obj : objects) {
            parseDeviceProperty(obj).ifPresent(deviceProperties::add);
        }
        return Collections.unmodifiableList(deviceProperties);
    }

    @SuppressWarnings("unchecked")
    public List<DeviceProperty<?>> parseAwsDeviceProperties(String json) {
        var obj = tryParseBody(json, MAP_TYPE_REFERENCE, Map.of());
        if (!obj.containsKey("state")) {
            return List.of();
        }

        var rawState = obj.get("state");
        Map<String, Object> state;
        if (rawState instanceof Map<?, ?> stateMap) {
            state = (Map<String, Object>) stateMap;
        } else {
            state = tryParseBody(rawState.toString(), MAP_TYPE_REFERENCE, Map.of());
        }
        if (!state.containsKey("reported")) {
            return List.of();
        }

        var rawReported = state.get("reported");
        Map<String, Object> reported;
        if (rawReported instanceof Map<?, ?> reportedMap) {
            reported = (Map<String, Object>) reportedMap;
        } else {
            reported = tryParseBody(rawReported.toString(), MAP_TYPE_REFERENCE, Map.of());
        }
        if (!reported.containsKey("11")) {
            return List.of();
        }

        var rawEleven = reported.get("11");
        Map<String, Object> eleven;
        if (rawEleven instanceof Map<?, ?> elevenMap) {
            eleven = (Map<String, Object>) elevenMap;
        } else {
            eleven = tryParseBody(rawEleven.toString(), MAP_TYPE_REFERENCE, Map.of());
        }
        if (!eleven.containsKey("properties")) {
            return List.of();
        }

        var deviceProperties = new ArrayList<DeviceProperty<?>>();
        var rawProperties = eleven.get("properties");
        Map<String, Object> properties;
        if (rawProperties instanceof Map<?, ?> propertiesMap) {
            properties = (Map<String, Object>) propertiesMap;
        } else {
            properties = tryParseBody(rawProperties.toString(), MAP_TYPE_REFERENCE, Map.of());
        }
        for (var entry : properties.entrySet()) {
            var deviceProperty = parseAwsDeviceProperty(entry.getKey(), entry.getValue());
            deviceProperties.add(deviceProperty);
        }
        return Collections.unmodifiableList(deviceProperties);
    }

    private Optional<DeviceProperty<?>> parseDeviceProperty(@Nullable Object obj) {
        if (!(obj instanceof Map<?, ?> firstLevelMap)) {
            logger.debug("Cannot parse device property, because object is not type of map!\n{}", obj);
            return empty();
        }

        if (!firstLevelMap.containsKey("property")) {
            if (logger.isWarnEnabled()) {
                var str = firstLevelMap.entrySet().stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.debug("Cannot parse device property, because firstLevelMap does not have [property] key!\n{}",
                        str);
            }
            return empty();
        }

        var objLevel2 = firstLevelMap.get("property");
        if (!(objLevel2 instanceof Map<?, ?> map)) {
            logger.debug("Cannot parse device property, because object is not type of map!\n{}", obj);
            return empty();
        }

        // name
        if (!map.containsKey("name")) {
            if (logger.isWarnEnabled()) {
                var str = map.entrySet().stream().map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.debug("Cannot parse device property, because map does not have [name] key!\n{}", str);
            }
            return empty();
        }
        var name = requireNonNull((String) map.get("name"));

        // other meaningful properties
        var baseType = findOrNull(map, "base_type");
        var readOnly = findBoolOrNull(map, "read_only");
        var direction = findOrNull(map, "direction");
        var dataUpdatedAt = findOrNull(map, "data_updated_at");
        var productName = findOrNull(map, "product_name");
        var displayName = findOrNull(map, "display_name");
        var value = findObjectOrNull(map, "value");

        // parse `properties`
        var list = map.entrySet().stream().filter(entry -> entry.getKey() != null)
                .filter(entry -> !"name".equals(entry.getKey())).filter(entry -> !"base_type".equals(entry.getKey()))
                .filter(entry -> !"read_only".equals(entry.getKey()))
                .filter(entry -> !"direction".equals(entry.getKey()))
                .filter(entry -> !"data_updated_at".equals(entry.getKey()))
                .filter(entry -> !"product_name".equals(entry.getKey()))
                .filter(entry -> !"display_name".equals(entry.getKey()))
                .filter(entry -> !"value".equals(entry.getKey()))
                .map(entry -> new Pair<>(requireNonNull(entry.getKey()).toString(), (Object) entry.getValue()))
                .toList();
        // this weird thing need to be done,
        // because `Collectors.toMap` does not support value=null
        // and in our case, sometimes the values are null
        SortedMap<@NotNull String, @Nullable Object> properties = new TreeMap<>();
        for (var entry : list) {
            properties.put(entry.key, entry.value);
        }
        properties = unmodifiableSortedMap(properties);

        return Optional.of(buildDeviceProperty(name, baseType, value, readOnly, direction, dataUpdatedAt, productName,
                displayName, properties));
    }

    private DeviceProperty<?> parseAwsDeviceProperty(String key, Object value) {
        if (value instanceof Boolean booleanValue) {
            return new DeviceProperty.BooleanDeviceProperty(key, true, null, null, null, null, booleanValue, null);
        }
        if (value instanceof Number numberValue) {
            return new DeviceProperty.LongDeviceProperty(key, true, null, null, null, null, numberValue.longValue(),
                    null);
        }
        return new DeviceProperty.StringDeviceProperty(key, true, null, null, null, null, value.toString(), null);
    }

    private DeviceProperty<?> buildDeviceProperty(String name, @Nullable String baseType, @Nullable Object value,
            @Nullable Boolean readOnly, @Nullable String direction, @Nullable String dataUpdatedAt,
            @Nullable String productName, @Nullable String displayName,
            SortedMap<String, @Nullable Object> properties) {
        if ("boolean".equalsIgnoreCase(baseType)) {
            Boolean bool;
            if (value == null) {
                bool = null;
            } else if (value instanceof Boolean typedValue) {
                bool = typedValue;
            } else if (value instanceof Number typedValue) {
                bool = typedValue.longValue() != 0;
            } else if (value instanceof String typedValue) {
                bool = parseBoolean(typedValue);
            } else {
                logger.debug("Cannot parse boolean from [{}]", value);
                bool = null;
            }
            return new DeviceProperty.BooleanDeviceProperty(name, readOnly, direction, dataUpdatedAt, productName,
                    displayName, bool, properties);
        }
        if ("integer".equalsIgnoreCase(baseType)) {
            Long longValue;
            if (value == null) {
                longValue = null;
            } else if (value instanceof Number typedValue) {
                longValue = typedValue.longValue();
            } else if (value instanceof String string) {
                try {
                    longValue = parseLong(string);
                } catch (NumberFormatException ex) {
                    logger.debug("Cannot parse long from [{}]", value, ex);
                    longValue = null;
                }
            } else {
                logger.debug("Cannot parse long from [{}]", value);
                longValue = null;
            }
            return new DeviceProperty.LongDeviceProperty(name, readOnly, direction, dataUpdatedAt, productName,
                    displayName, longValue, properties);
        }
        var string = value != null ? value.toString() : null;
        return new DeviceProperty.StringDeviceProperty(name, readOnly, direction, dataUpdatedAt, productName,
                displayName, string, properties);
    }

    @Nullable
    private String findOrNull(Map<?, ?> map, String name) {
        if (!map.containsKey(name)) {
            return null;
        }
        return (String) map.get(name);
    }

    @SuppressWarnings("SameParameterValue")
    @Nullable
    private Boolean findBoolOrNull(Map<?, ?> map, String name) {
        if (!map.containsKey(name)) {
            return null;
        }
        var value = map.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String string) {
            return parseBoolean(string);
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    @Nullable
    private Object findObjectOrNull(Map<?, ?> map, String name) {
        if (!map.containsKey(name)) {
            return null;
        }
        return map.get(name);
    }

    public String datapointParam(Object value) {
        return gson.toJson(Map.of("datapoint", Map.of("value", value)));
    }

    public Optional<Object> datapointValue(@Nullable String json) {
        if (json == null) {
            return empty();
        }
        var map = tryParseBody(json, MAP_TYPE_REFERENCE, Map.of());
        if (!map.containsKey("datapoint")) {
            return empty();
        }
        var datapoint = (Map<?, ?>) map.get("datapoint");
        if (datapoint == null || !datapoint.containsKey("value")) {
            return empty();
        }
        return Optional.ofNullable(datapoint.get("value"));
    }

    public List<String> parseAwsGatewayIds(String json) {
        var map = tryParseBody(json, MAP_TYPE_REFERENCE, Map.of());
        if (!map.containsKey("data")) {
            return List.of();
        }

        var data = map.get("data");
        List<Object> list;
        if (data instanceof Collection<?> collection) {
            list = new ArrayList<>(collection);
        } else {
            list = tryParseBody(data.toString(), LIST_TYPE_REFERENCE, List.of());
        }

        return list.stream()//
                .map(this::parseAwsGatewayId)//
                .filter(Optional::isPresent)//
                .map(Optional::get)//
                .toList();
    }

    @SuppressWarnings("unchecked")
    public Optional<String> parseAwsGatewayId(Object json) {
        Map<String, Object> map;
        if (json instanceof Map<?, ?>) {
            map = (Map<String, Object>) json;
        } else {
            map = tryParseBody(json.toString(), MAP_TYPE_REFERENCE, Map.of());
        }
        if (!map.containsKey("id")) {
            return empty();
        }
        return Optional.ofNullable(map.get("id")).map(Object::toString);
    }

    private static record Pair<K, @Nullable V> (K key, @Nullable V value) {
    }
}
