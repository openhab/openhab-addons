/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain.dto;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.converter.JsonStateConverter;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

/**
 * DTO class for the devices returned by the energy manager.
 *
 * @author Sven Carstens - Initial contribution
 */
public class DeviceDTO {
    private final Logger logger = LoggerFactory.getLogger(DeviceDTO.class);

    private String guid;
    private Collection<DeviceModel> deviceModel;
    private Map<String, TagValueDTO> tagValues;

    public String getGuid() {
        return this.guid;
    }

    public Collection<String> getDeviceModelStrings() {
        return this.deviceModel.stream().map(DeviceModel::getDeviceClass).collect(Collectors.toList());
    }

    public Collection<DeviceModel> getDeviceModel() {
        return this.deviceModel;
    }

    public Map<String, TagValueDTO> getTagValues() {
        return this.tagValues;
    }

    public @Nullable String getStringTag(String tagName) {
        JsonPrimitive jsonPrimitive = this.getJsonPrimitiveFromTag(tagName);
        if (jsonPrimitive != null) {
            return jsonPrimitive.getAsString();
        }

        return null;
    }

    protected @Nullable JsonPrimitive getJsonPrimitiveFromTag(String tagName) {
        @Nullable
        Map<String, TagValueDTO> localTagValues = this.getTagValues();
        if (localTagValues != null) {
            @Nullable
            TagValueDTO localTag = localTagValues.get(tagName);
            if (localTag != null && localTag.getValue().isJsonPrimitive()) {
                return (JsonPrimitive) localTag.getValue();
            }
        }
        return null;
    }

    public @Nullable JsonObject getJsonObjectFromTag(String tagName) {
        @Nullable
        Map<String, TagValueDTO> localTagValues = this.getTagValues();
        if (localTagValues != null) {
            @Nullable
            TagValueDTO localTag = localTagValues.get(tagName);
            if (localTag != null && localTag.getValue().isJsonObject()) {
                return (JsonObject) localTag.getValue();
            }
        }
        return null;
    }

    public JsonPrimitive getJsonPrimitiveFromPath(String tagName, String path) {
        JsonElement jsonElement = this.getJsonFromPath(tagName, path);

        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            return (JsonPrimitive) jsonElement;
        }
        return null;
    }

    public JsonElement getJsonFromPath(String tagName, String path) {
        JsonObject json = this.getJsonObjectFromTag(tagName);
        if (json != null) {
            String[] parts = path.split("\\.|\\[|\\]");
            JsonElement result = json;

            for (String key : parts) {

                key = key.trim();
                if (key.isEmpty())
                    continue;

                if (result == null) {
                    result = JsonNull.INSTANCE;
                    break;
                }

                if (result.isJsonObject()) {
                    result = ((JsonObject) result).get(key);
                } else if (result.isJsonArray()) {
                    int ix = Integer.valueOf(key) - 1;
                    result = ((JsonArray) result).get(ix);
                } else
                    break;
            }

            return result;
        }

        return null;
    }

    public State getState(JsonStateConverter converter, String tagName) {
        return this.getState(converter, tagName, tagName, null);
    }

    public State getState(JsonStateConverter converter, String channelName, String tagName, String jsonPath) {
        State state = UnDefType.UNDEF;
        try {
            JsonPrimitive jsonPrimitive = jsonPath == null ? this.getJsonPrimitiveFromTag(tagName)
                    : this.getJsonPrimitiveFromPath(tagName, jsonPath);
            if (jsonPrimitive != null) {
                state = converter.convert(jsonPrimitive);
            } else {
                state = UnDefType.NULL;
            }
        } catch (Exception ex) {
            this.logger.error("failed getting state for {}", channelName, ex);
        }

        return state;
    }

    public static class DeviceModel {
        private String deviceClass;

        public String getDeviceClass() {
            return this.deviceClass;
        }

        @Override
        public String toString() {
            return this.deviceClass;
        }
    }
}
