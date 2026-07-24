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
package org.openhab.binding.rachio.internal.api.json;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * DTOs for the modern Rachio Property Service. The service may include undocumented fields, so these DTOs keep only
 * stable top-level values and retain room for future entity-specific expansion.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioPropertyGsonDTO {
    private static final Gson GSON = new Gson();

    public static class RachioProperty {
        public String id = "";
        public String propertyId = "";
        public String name = "";
        public String nickname = "";
        public String timeZone = "";
        public String latitude = "";
        public String longitude = "";
        public ArrayList<RachioPropertyEntity> entities = new ArrayList<>();
        public ArrayList<RachioPropertyEntity> resources = new ArrayList<>();
        public @Nullable JsonObject address;
        public @Nullable JsonObject resourceId;

        public String getId() {
            return firstNonBlank(id, propertyId, readString(resourceId, "propertyId"), readString(resourceId, "id"));
        }

        public String getName() {
            return firstNonBlank(name, nickname, getId());
        }
    }

    public static class RachioPropertyEntity {
        public String id = "";
        public String entityId = "";
        public String type = "";
        public String entityType = "";
        public String resourceType = "";
        public String name = "";
        public @Nullable JsonObject resourceId;
    }

    public static class RachioPropertyListResponse {
        public ArrayList<RachioProperty> properties = new ArrayList<>();

        public static RachioPropertyListResponse fromJson(String json) {
            RachioPropertyListResponse response = new RachioPropertyListResponse();
            JsonElement root = JsonParser.parseString(json);
            if (root.isJsonArray()) {
                response.properties.addAll(parseProperties(root.getAsJsonArray()));
            } else if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                for (String arrayName : List.of("properties", "items", "data", "results")) {
                    JsonElement arrayElement = object.get(arrayName);
                    if (arrayElement != null && arrayElement.isJsonArray()) {
                        response.properties.addAll(parseProperties(arrayElement.getAsJsonArray()));
                    }
                }
                if (response.properties.isEmpty()) {
                    RachioProperty property = parsePropertyObjectOrWrapper(object);
                    if (property != null) {
                        response.properties.add(property);
                    }
                }
            }
            return response;
        }

        private static List<RachioProperty> parseProperties(JsonArray array) {
            List<RachioProperty> properties = new ArrayList<>();
            for (JsonElement element : array) {
                if (element != null && element.isJsonObject()) {
                    RachioProperty property = parsePropertyObject(element.getAsJsonObject());
                    if (property != null) {
                        properties.add(property);
                    }
                }
            }
            return properties;
        }
    }

    public static class RachioPropertyEntityLookupResponse {
        public ArrayList<RachioProperty> properties = new ArrayList<>();
        public @Nullable RachioProperty property;

        public static RachioPropertyEntityLookupResponse fromJson(String json) {
            RachioPropertyEntityLookupResponse response = new RachioPropertyEntityLookupResponse();
            RachioPropertyListResponse listResponse = RachioPropertyListResponse.fromJson(json);
            response.properties.addAll(listResponse.properties);
            if (!response.properties.isEmpty()) {
                response.property = response.properties.get(0);
            }
            return response;
        }

        public @Nullable RachioProperty getProperty() {
            return property;
        }
    }

    public static @Nullable RachioProperty parseProperty(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            return null;
        }
        return parsePropertyObjectOrWrapper(root.getAsJsonObject());
    }

    private static @Nullable RachioProperty parsePropertyObjectOrWrapper(JsonObject object) {
        JsonElement propertyElement = object.get("property");
        if (propertyElement != null && propertyElement.isJsonObject()) {
            return parsePropertyObject(propertyElement.getAsJsonObject());
        }
        JsonElement dataElement = object.get("data");
        if (dataElement != null && dataElement.isJsonObject()) {
            return parsePropertyObject(dataElement.getAsJsonObject());
        }
        return parsePropertyObject(object);
    }

    private static @Nullable RachioProperty parsePropertyObject(JsonObject object) {
        return GSON.fromJson(object, RachioProperty.class);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String readString(@Nullable JsonObject object, String memberName) {
        if (object == null) {
            return "";
        }
        JsonElement element = object.get(memberName);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
    }
}
