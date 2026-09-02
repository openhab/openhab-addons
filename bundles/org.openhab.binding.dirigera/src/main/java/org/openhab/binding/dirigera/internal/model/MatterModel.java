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
package org.openhab.binding.dirigera.internal.model;

import static org.openhab.binding.dirigera.internal.interfaces.Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.ResourceReader;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterModel} holds the configuration for a specific deviceId
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MatterModel {
    public static final String CONFIG_FILE_PATH = "/json/matter/devices.json";

    public static final String TYPE_BASE = "baseDevice";

    public static final String CONFIG_KEY_THING_TYPE = "thingType";
    public static final String CONFIG_KEY_THING_PROPERTIES = "thingProperties";
    public static final String CONFIG_KEY_STATUS_PROPERTIES = "statusProperties";
    public static final String CONFIG_KEY_CONTROL_PROPERTIES = "controlProperties";
    public static final String CONFIG_KEY_LINK_CANDIDATE_TYPES = "linkCandidates";
    public static final String CONFIG_KEY_IDENTIFICATION = "identification";

    public static final String IDENTIFICATION_KEY_TYPE = "type";
    public static final String IDENTIFICATION_KEY_DEVICE_TYPE = "deviceType";

    public static final String CHANNEL_KEY_ATTRIBUTE = "attribute";
    public static final String CHANNEL_KEY_CHANNEL_NAME = "channel";
    public static final String CHANNEL_KEY_CHANNEL_TYPE = "channelType";
    public static final String CHANNEL_KEY_CHANNEL_LABEL = "channelLabel";
    public static final String CHANNEL_KEY_CHANNEL_DESCRIPTION = "channelDescription";
    public static final String CHANNEL_KEY_ITEM_TYPE = "itemType";
    public static final String CHANNEL_KEY_TRANSFORMATION = "transformation";
    public static final String CHANNEL_KEY_REQUEST_JSON = "json";
    public static final String CHANNEL_KEY_MAPPING = "mapping";
    public static final String CHANNEL_KEY_FORMAT = "format";
    public static final String CHANNEL_KEY_CONVERSION = "conversion";
    public static final String CHANNEL_KEY_IN_TYPE = "inType";
    public static final String CHANNEL_KEY_OUT_TYPE = "outType";

    private static final JSONObject MATTER_DEVICE_CONFIG = new JSONObject();

    private final Logger logger = LoggerFactory.getLogger(MatterModel.class);
    private final String deviceId;
    private final String deviceType;

    // holds for each deviceId the mapping of channelName to control property configuration
    private final Map<String, String> identificationMap = new HashMap<>();
    private final Map<String, JSONObject> controlPropertiesMapping = new HashMap<>();
    private final Map<String, JSONObject> statusProperties = new HashMap<>();
    private final List<String> thingProperties = new ArrayList<>();

    static {
        loadDeviceConfig();
    }

    public MatterModel(String deviceId, String deviceType) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        addDeviceType(TYPE_BASE);
        addDeviceType(deviceType);
    }

    /**
     * Get the type of this device model
     *
     * @return type
     */
    public String getType() {
        return identificationMap.getOrDefault(IDENTIFICATION_KEY_TYPE, "");
    }

    /**
     * Get the device type of this model
     *
     * @return device type
     */
    public String getDeviceType() {
        return identificationMap.getOrDefault(IDENTIFICATION_KEY_DEVICE_TYPE, "");
    }

    /**
     * Get all status properties from this model including their configuration as JSONObject
     *
     * @return Map with status property as key and JSONObject configuration as value
     */
    public Map<String, JSONObject> getStatusProperties() {
        return statusProperties;
    }

    /**
     * Get thing properties of this model
     *
     * @param deviceStatus as JSONObject from API device status call
     * @return Map with property key and property value for thing properties
     */
    public Map<String, String> getThingProperties(JSONObject deviceStatus) {
        TreeMap<String, String> handlerProperties = new TreeMap<>();
        thingProperties.forEach(property -> {
            String propertyKey = property;
            if (deviceStatus.has(propertyKey)) {
                handlerProperties.put(propertyKey, deviceStatus.get(propertyKey).toString());
            }
            if (deviceStatus.has(JSON_KEY_CAPABILITIES)) {
                JSONObject capabilities = deviceStatus.getJSONObject(JSON_KEY_CAPABILITIES);
                if (capabilities.has(CAPABILITIES_KEY_CAN_RECEIVE)) {
                    handlerProperties.put(CAPABILITIES_KEY_CAN_RECEIVE,
                            capabilities.get(CAPABILITIES_KEY_CAN_RECEIVE).toString());
                }
                if (capabilities.has(CAPABILITIES_KEY_CAN_SEND)) {
                    handlerProperties.put(CAPABILITIES_KEY_CAN_SEND,
                            capabilities.get(CAPABILITIES_KEY_CAN_SEND).toString());
                }
            }
            if (deviceStatus.has(JSON_KEY_ATTRIBUTES)) {
                JSONObject attributes = deviceStatus.getJSONObject(JSON_KEY_ATTRIBUTES);
                if (attributes.has(propertyKey)) {
                    handlerProperties.put(propertyKey, attributes.get(propertyKey).toString());
                }
            }
        });
        return handlerProperties;
    }

    /**
     * Get link candidates which can be linked to this device model
     *
     * @return List of strings which device types can be connected
     */
    public List<String> getLinkCandidates() {
        List<String> linkCandidates = new ArrayList<>();
        JSONObject deviceConfig = MATTER_DEVICE_CONFIG.optJSONObject(deviceType);
        if (deviceConfig != null) {
            deviceConfig.getJSONArray(CONFIG_KEY_LINK_CANDIDATE_TYPES).forEach(entry -> {
                String candidateType = entry.toString();
                if (!linkCandidates.contains(candidateType)) {
                    linkCandidates.add(candidateType);
                }
            });
        }
        return linkCandidates;
    }

    /**
     * Get the channel updates to be performed based on an update JSONObject
     *
     * @param deviceUpdate as JSONObject
     * @return Map of updates with channel name as key and State as value
     */
    public Map<String, State> getAttributeUpdates(JSONObject deviceUpdate) {
        Map<String, State> channelUpdates = new HashMap<>();
        JSONObject attributeUpdates = deviceUpdate.optJSONObject(JSON_KEY_ATTRIBUTES);
        if (attributeUpdates != null) {
            attributeUpdates.toMap().forEach((attributeKey, value) -> {
                Entry<String, State> attributeUpdate = attributeUpdate(attributeKey, value);
                if (attributeUpdate != null) {
                    channelUpdates.put(attributeUpdate.getKey(), attributeUpdate.getValue());
                }
            });
        }
        return channelUpdates;
    }

    /**
     * Get a Map Entry for a given attribute and corresponding value
     *
     * @param attribute which has an update
     * @param value update of the attribute
     * @return Map Entry with channel name as key and State as value. null if it cannot or shouldn't be transformed
     */
    private @Nullable Entry<String, State> attributeUpdate(String attribute, Object value) {
        JSONObject statusConfig = statusProperties.get(attribute);
        if (statusConfig != null) {
            String channel = statusConfig.optString(CHANNEL_KEY_CHANNEL_NAME);
            State state = getState(value, statusConfig);
            if (!channel.isBlank() && state != null) {
                return Map.entry(channel, state);
            }
        }
        return null;
    }

    /**
     * Get the state for a value based on the transformation defined in the model
     *
     * @param value to be transformed
     * @param channelConfiguration for the value
     * @return state after transformation, null if it cannot or shouldn't be transformed
     */
    private @Nullable State getState(Object value, JSONObject channelConfiguration) {
        // convert value into correct type
        String inType = channelConfiguration.optString(CHANNEL_KEY_IN_TYPE);
        var inValue = value;
        if (!inType.isBlank() && inValue instanceof Number num) {
            inValue = switch (inType) {
                case "Number" -> num.doubleValue();
                case "String" -> value.toString();
                default -> value;
            };
        }

        // apply configured conversion for numeric values
        String conversionType = channelConfiguration.optString(CHANNEL_KEY_CONVERSION);
        var convertedValue = inValue;
        if (!conversionType.isBlank()) {
            if (value instanceof Number num) {
                convertedValue = ConversionModel.convert(conversionType, num);
            } else {
                logger.warn("MATTER MODEL State conversion: value {} is not a number, cannot apply conversion {}",
                        value, conversionType);
            }
        }

        // apply transformation
        String transformation = channelConfiguration.optString(CHANNEL_KEY_TRANSFORMATION);
        var transformed = switch (transformation) {
            case "raw" -> convertedValue.toString();
            case "format" ->
                String.format(Locale.ENGLISH, channelConfiguration.getString(CHANNEL_KEY_FORMAT), convertedValue);
            case "mapping" -> map(convertedValue.toString(), channelConfiguration.getJSONObject(CHANNEL_KEY_MAPPING));
            case "code" -> "";
            default -> {
                logger.warn("MATTER MODEL State transformation '{}' unknown", transformation);
                yield "";
            }
        };

        if (transformed.isBlank()) {
            return null;
        }

        // convert into state type
        String outType = channelConfiguration.optString(CHANNEL_KEY_OUT_TYPE);
        var state = switch (outType) {
            case "DecimalType" -> {
                try {
                    yield new DecimalType(transformed);
                } catch (NumberFormatException nfe) {
                    logger.warn("MATTER MODEL State conversion: cannot convert {} into number", transformed);
                    yield UnDefType.UNDEF;
                }
            }
            case "StringType" -> new StringType(transformed);
            case "QuantityType" -> ConversionModel.stringToQuality(transformed);
            case "OnOffType" -> OnOffType.from(transformed);
            case "OpenClosedType" ->
                (Boolean.TRUE.toString().equalsIgnoreCase(transformed)) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case "DateTimeType" -> ConversionModel.stringToDateTime(transformed);
            default -> {
                logger.warn("MATTER MODEL State conversion: out type '{}' unknown", outType);
                yield UnDefType.NULL;
            }
        };
        return state;
    }

    /**
     * Maps value according to configuration
     *
     * @param value to be mapped
     * @param mapping configuration as JSONObject
     * @return mapped value as String, blank String if no mapping value was found
     */
    private String map(String value, JSONObject mapping) {
        Object mappingValue = mapping.opt(value);
        if (mappingValue == null) {
            logger.warn("MATTER MODEL State conversion: no mapping found for {} in {}", value, mapping);
            return "";
        }
        return mappingValue.toString();
    }

    /**
     * Get JSON commands to send to API for a command performed on a channel
     *
     * @param targetChannel of command
     * @param command to be executed
     * @return Map with device id as key and JSONObject for API call, empty if no requests were found
     */
    public Map<String, JSONObject> getCommandPatch(String targetChannel, Command command) {
        Map<String, JSONObject> requests = new HashMap<>();
        controlPropertiesMapping.forEach((channel, config) -> {
            if (channel.equals(targetChannel)) {
                JSONObject request = getCommandJson(command, config);
                if (!request.isEmpty()) {
                    requests.put(deviceId, request);
                }
            }
        });
        return requests;
    }

    /**
     * Gets a JSON request which can be sent to API
     *
     * @param command from channel
     * @param channelConfiguration as JSONObject
     * @return request as JSONObject, empty if no request is found
     */
    private JSONObject getCommandJson(Command command, JSONObject channelConfiguration) {
        String targetAttribute = channelConfiguration.optString(CHANNEL_KEY_ATTRIBUTE);
        String transformation = channelConfiguration.optString(CHANNEL_KEY_TRANSFORMATION);
        if (!targetAttribute.isBlank() && !transformation.isBlank()) {
            var commandStringValue = switch (transformation) {
                case "raw" -> command.toString();
                case "mapping" -> map(command.toString(), channelConfiguration.getJSONObject(CHANNEL_KEY_MAPPING));
                case "code" -> "";
                case "format" -> String.format(Locale.ENGLISH, channelConfiguration.getString(CHANNEL_KEY_FORMAT),
                        command.toString());
                default -> {
                    logger.warn("MATTER MODEL Request conversion: unknown transformation {} for target attribute '{}'",
                            transformation, targetAttribute);
                    yield "";
                }
            };
            if (commandStringValue.isBlank()) {
                return new JSONObject();
            }
            String outType = channelConfiguration.optString(CHANNEL_KEY_OUT_TYPE);
            var commandValue = switch (outType) {
                case "Boolean" -> Boolean.parseBoolean(commandStringValue);
                case "String" -> commandStringValue;
                case "Number" -> ConversionModel.stringToInteger(commandStringValue);
                default -> {
                    logger.warn("MATTER MODEL Request conversion: unknown outType {} for target attribute '{}'",
                            outType, targetAttribute);
                    yield "";
                }
            };
            String jsonContent = channelConfiguration.optString(CHANNEL_KEY_REQUEST_JSON);
            var json = switch (jsonContent) {
                case CHANNEL_KEY_ATTRIBUTE -> {
                    JSONObject attributes = (new JSONObject()).put(targetAttribute, commandValue);
                    JSONObject patch = (new JSONObject()).put(JSON_KEY_ATTRIBUTES, attributes);
                    yield patch;
                }
                default -> {
                    logger.warn("MATTER MODEL Request conversion: unknown json content {} for target attribute '{}'",
                            jsonContent, targetAttribute);
                    yield new JSONObject();
                }
            };
            return json;
        }
        return new JSONObject();
    }

    /**
     * Adds device type for a device type to this model which collects all status, control and thing properties
     *
     * @param deviceType as String to be added
     */
    private void addDeviceType(String deviceType) {
        JSONObject deviceConfig = MATTER_DEVICE_CONFIG.optJSONObject(deviceType);
        if (deviceConfig == null) {
            logger.debug("No configuration found for device type {}", deviceType);
            MATTER_DEVICE_CONFIG.keySet().forEach(key -> logger.warn("Available device type: {}", key));
            return;
        }

        JSONObject identification = deviceConfig.optJSONObject(CONFIG_KEY_IDENTIFICATION);
        if (identification != null) {
            identificationMap.putAll(identification.toMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))));
        }

        // collect control configuration - item2handler
        deviceConfig.getJSONArray(CONFIG_KEY_CONTROL_PROPERTIES).forEach(entry ->

        {
            JSONObject controlEntry = (JSONObject) entry;
            String channelName = controlEntry.getString(CHANNEL_KEY_CHANNEL_NAME);
            controlPropertiesMapping.put(channelName, controlEntry);
        });

        // collect thing properties
        thingProperties.addAll(deviceConfig.getJSONArray(CONFIG_KEY_THING_PROPERTIES).toList().stream()
                .map(Object::toString).toList());

        // collect status configurations - handler2item
        deviceConfig.getJSONArray(CONFIG_KEY_STATUS_PROPERTIES).forEach(entry -> {
            JSONObject statusEntry = (JSONObject) entry;
            String propertyName = statusEntry.getString(CHANNEL_KEY_ATTRIBUTE);
            statusProperties.put(propertyName, statusEntry);
        });
    }

    /**
     * Read complete device definitions from resources into memory
     */
    public static void loadDeviceConfig() {
        String deviceConfig = ResourceReader.getResource(CONFIG_FILE_PATH);
        JSONArray devices = new JSONArray(deviceConfig);
        devices.forEach(device -> {
            String tt = ((JSONObject) device).getString(CONFIG_KEY_THING_TYPE);
            MATTER_DEVICE_CONFIG.put(tt, device);
        });
    }
}
