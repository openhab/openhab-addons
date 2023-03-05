/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * DTO class for the devices returned by the energy manager.
 *
 * Properties without setters are only filled by gson JSON parsing.
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

    /**
     * Helper to get a string value from the list of tag values.
     *
     * @param tagName name of tag to read
     * @return tag value
     */
    public @Nullable String getStringTag(String tagName) {
        JsonPrimitive jsonPrimitive = this.getJsonPrimitiveFromTag(tagName);
        if (jsonPrimitive != null) {
            return jsonPrimitive.getAsString();
        }

        return null;
    }

    /**
     * Helper to get a {@link JsonPrimitive} from the list of tag values.
     *
     * The primitves are later converted to the desired target type.
     * 
     * @param tagName name of tag to read
     * @return raw json from tag value
     */
    protected @Nullable JsonPrimitive getJsonPrimitiveFromTag(String tagName) {
        Map<String, TagValueDTO> localTagValues = this.getTagValues();
        if (localTagValues != null) {
            TagValueDTO localTag = localTagValues.get(tagName);
            if (localTag != null && localTag.getValue().isJsonPrimitive()) {
                return (JsonPrimitive) localTag.getValue();
            }
        }
        return null;
    }

    /**
     * Helper to get a {@link JsonObject} from the list of tag values.
     *
     * The objects are used by the concrete devices to read interesting
     * but deeply nested values.
     *
     * @param tagName name of tag to read
     * @return raw json from tag value
     */
    public @Nullable JsonObject getJsonObjectFromTag(String tagName) {
        Map<String, TagValueDTO> localTagValues = this.getTagValues();
        if (localTagValues != null) {
            TagValueDTO localTag = localTagValues.get(tagName);
            if (localTag != null && localTag.getValue().isJsonObject()) {
                return (JsonObject) localTag.getValue();
            }
        }
        return null;
    }

    /**
     * Helper to get a {@link JsonObject} from a tag value which contains JSON.
     *
     * The objects are used by the concrete devices to read interesting
     * but deeply nested values.
     *
     * @param tagName name of tag to read
     * @return raw json from tag value
     */
    public JsonPrimitive getJsonPrimitiveFromPath(String tagName, String path) {
        JsonElement jsonElement = this.getJsonFromPath(tagName, path);

        if (jsonElement != null && jsonElement.isJsonPrimitive()) {
            return (JsonPrimitive) jsonElement;
        }
        return null;
    }

    /**
     * Helper to get a {@link JsonObject} from a tag value which contains JSON.
     *
     * The json path is traversed according to the supplied path. Only simple pathes
     * are supported.
     *
     * @param tagName name of tag to read
     * @return raw json from tag value
     */
    public JsonElement getJsonFromPath(String tagName, String path) {
        JsonObject json = this.getJsonObjectFromTag(tagName);
        if (json != null) {
            String[] parts = path.split("\\.|\\[|\\]");
            JsonElement result = json;

            for (String key : parts) {

                key = key.trim();
                if (key.isEmpty()) {
                    continue;
                }

                if (result == null) {
                    result = JsonNull.INSTANCE;
                    break;
                }

                if (result.isJsonObject()) {
                    result = ((JsonObject) result).get(key);
                } else if (result.isJsonArray()) {
                    int ix = Integer.valueOf(key) - 1;
                    result = ((JsonArray) result).get(ix);
                } else {
                    break;
                }
            }

            return result;
        }

        return null;
    }

    /**
     * Transform a value from a tag to a state.
     *
     * @param converter applied the the {@link JsonPrimitive}
     * @param tagName to find value
     * @return state for channel
     */
    public State getState(JsonStateConverter converter, String tagName) {
        return this.getState(converter, tagName, tagName, null);
    }

    /**
     * Transform a value specified via JSON path from a tag to a state.
     *
     * @param converter applied the the {@link JsonPrimitive}
     * @param channelName for the state
     * @param tagName to find value
     * @param jsonPath to find value
     * @return state for channel
     */
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
            this.logger.warn("failed getting state for {}", channelName, ex);
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
