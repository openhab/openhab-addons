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
package org.openhab.binding.dirigera.internal.handler.matter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.ResourceReader;
import org.openhab.binding.dirigera.internal.interfaces.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseMatterConfiguration} holds the configuration for one BasMatterHandler
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BaseMatterConfiguration {
    public static final String TYPE_BASE = "baseDevice";
    public static final String PROPERTY_THING_PROPERTIES = "thingProperties";
    public static final String PROPERTY_STATUS_PROPERTIES = "statusProperties";
    public static final String PROPERTY_CONTROL_PROPERTIES = "controlProperties";
    public static final String PROPERTY_LINK_CANDIDATE_TYPES = "linkCandidates";

    public static final String KEY_TRANSFORMATION = "transformation";
    public static final String KEY_IDENTIFICATION = "identification";
    public static final String KEY_ATTRIBUTE = "attribute";
    public static final String KEY_CHANNEL = "channel";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DEVICE_TYPE = "deviceType";

    private static JSONObject matterDeviceConfig = new JSONObject();

    private final Logger logger = LoggerFactory.getLogger(BaseMatterConfiguration.class);
    private final String deviceType;;

    // holds for each deviceId the mapping of channelName to control property configuration
    private Map<String, Map<String, JSONObject>> controlPropertiesMapping = new HashMap<>();
    private Map<String, JSONObject> statusProperties = new HashMap<>();
    private List<String> thingProperties = new ArrayList<>();
    private List<String> types = new ArrayList<>();

    private final BaseMatterHandler handler;

    public BaseMatterConfiguration(BaseMatterHandler handler, String deviceId, String deviceType) {
        this.handler = handler;
        this.deviceType = deviceType;
        if (matterDeviceConfig.isEmpty()) {
            Instant startTime = Instant.now();
            loadDeviceConfig();
            logger.trace("BaseMatterConfiguration took {} ms", Duration.between(startTime, Instant.now()).toMillis());
        }
        addDeviceType(deviceId, TYPE_BASE);
        addDeviceType(deviceId, deviceType);
    }

    private void addDeviceType(String deviceId, String deviceType) {
        JSONObject deviceConfig = matterDeviceConfig.optJSONObject(deviceType);
        if (deviceConfig == null) {
            logger.warn("No configuration found for device type {}", deviceType);
            return;
        }

        // collect control configuration - item2handler
        Map<String, JSONObject> controlProperties = new HashMap<>();
        deviceConfig.getJSONArray(PROPERTY_CONTROL_PROPERTIES).forEach(entry -> {
            JSONObject controlEntry = (JSONObject) entry;
            String channelName = controlEntry.getString(KEY_CHANNEL);
            controlProperties.put(channelName, controlEntry);
            controlPropertiesMapping.put(deviceId, controlProperties);
        });

        // collect identification properties
        JSONObject identificatonPRoperties = deviceConfig.optJSONObject(KEY_IDENTIFICATION);
        if (identificatonPRoperties != null) {
            String typeString = identificatonPRoperties.optString(KEY_TYPE);
            if (!types.contains(typeString)) {
                types.add(typeString);
            }
        }

        // collect thing properties
        thingProperties.addAll(
                deviceConfig.getJSONArray(PROPERTY_THING_PROPERTIES).toList().stream().map(Object::toString).toList());

        // collect status configurations - handler2item
        deviceConfig.getJSONArray(PROPERTY_STATUS_PROPERTIES).forEach(entry -> {
            JSONObject statusEntry = (JSONObject) entry;
            String propertyName = statusEntry.getString(KEY_ATTRIBUTE);
            statusProperties.put(propertyName, statusEntry);
        });
    }

    // renove
    public List<String> collectDevices(String id) {
        // adjust thing properties if needed, then apply them
        // add all configs from attached devices
        List<String> connections = new ArrayList<>();
        String relationId = handler.gateway().model().getRelationId(id);
        if (id.equals(relationId)) {
            String deviceType = handler.gateway().model().getDeviceType(id);
            connections.add(id);
            addDeviceType(id, deviceType);
        } else {
            Map<String, String> connectedDevices = handler.gateway().model().getRelations(relationId);
            connectedDevices.forEach((key, value) -> {
                connections.add(key);
                addDeviceType(key, value);
            });
        }
        return connections;
    }

    // remove
    public Map<String, String> updateProperties(String id) {
        final JSONObject cumulatedUpadte = new JSONObject();
        collectDevices(id).forEach(deviceId -> {
            JSONObject values = handler.gateway().api().readDevice(deviceId);
            merge(values, cumulatedUpadte);
        });

        TreeMap<String, String> handlerProperties = new TreeMap<>();
        thingProperties.forEach(property -> {
            String propertyKey = property;
            if (cumulatedUpadte.has(propertyKey)) {
                handlerProperties.put(propertyKey, cumulatedUpadte.get(propertyKey).toString());
            }
            if (cumulatedUpadte.has(Model.CAPABILITIES)) {
                JSONObject capabilities = cumulatedUpadte.getJSONObject(Model.CAPABILITIES);
                if (capabilities.has(Model.PROPERTY_CAN_RECEIVE)) {
                    handlerProperties.put(Model.PROPERTY_CAN_RECEIVE,
                            capabilities.get(Model.PROPERTY_CAN_RECEIVE).toString());
                }
                if (capabilities.has(Model.PROPERTY_CAN_SEND)) {
                    handlerProperties.put(Model.PROPERTY_CAN_SEND,
                            capabilities.get(Model.PROPERTY_CAN_SEND).toString());
                }
            }
            if (cumulatedUpadte.has(Model.ATTRIBUTES)) {
                JSONObject attributes = cumulatedUpadte.getJSONObject(Model.ATTRIBUTES);
                if (attributes.has(propertyKey)) {
                    handlerProperties.put(propertyKey, attributes.get(propertyKey).toString());
                }
            }
        });
        return handlerProperties;
    }

    public List<String> getTypes() {
        return types;
    }

    public Map<String, JSONObject> getStatusProperties() {
        return statusProperties;
    }

    public Map<String, String> getThingProperties(String id) {
        return updateProperties(id);
    }

    public List<String> getLinkCandidates() {
        List<String> linkCandidates = new ArrayList<>();
        JSONObject deviceConfig = matterDeviceConfig.optJSONObject(deviceType);
        if (deviceConfig != null) {
            deviceConfig.getJSONArray(PROPERTY_LINK_CANDIDATE_TYPES).forEach(entry -> {
                String candidateType = entry.toString();
                if (!linkCandidates.contains(candidateType)) {
                    linkCandidates.add(candidateType);
                }
            });
        }
        return linkCandidates;
    }

    /**
     * Merge DIRIGERA updates to receive all attributes and capabilities
     *
     * @param one
     * @param two
     * @return
     */
    public static void merge(JSONObject source, JSONObject target) {
        source.keySet().forEach(key -> {
            switch (key) {
                case Model.ATTRIBUTES:
                    if (!target.has(Model.ATTRIBUTES)) {
                        target.put(Model.ATTRIBUTES, new JSONObject());
                    }
                    JSONObject targetAttributes = target.getJSONObject(Model.ATTRIBUTES);
                    merge(source.getJSONObject(Model.ATTRIBUTES), targetAttributes);
                    target.put(Model.ATTRIBUTES, targetAttributes);
                    break;
                case Model.CAPABILITIES:
                    JSONObject capabilities = new JSONObject();
                    if (target.has(Model.CAPABILITIES)) {
                        capabilities.put("canSend",
                                mergeLists(target.getJSONObject(Model.CAPABILITIES).getJSONArray("canSend"),
                                        source.getJSONObject(Model.CAPABILITIES).getJSONArray("canSend")));
                        capabilities.put("canReceive",
                                mergeLists(target.getJSONObject(Model.CAPABILITIES).getJSONArray("canReceive"),
                                        source.getJSONObject(Model.CAPABILITIES).getJSONArray("canSend")));
                    } else {
                        capabilities = source.getJSONObject(Model.CAPABILITIES);
                    }
                    target.put(Model.CAPABILITIES, capabilities);
                    break;
                case "remoteLinks":
                    if (!target.has("remoteLinks")) {
                        target.put("remoteLinks", new JSONArray());
                    }
                    target.put(key, target.getJSONArray(key).putAll(source.getJSONArray(key)));
                    break;
                default:
                    target.put(key, source.get(key));
                    break;
            }
        });
    }

    private static JSONArray mergeLists(JSONArray source, JSONArray target) {
        List<Object> list = target.toList();
        for (Object obj : source.toList()) {
            if (!list.contains(obj)) {
                list.add(obj);
            }
        }
        return new JSONArray(list);
    }

    /**
     * Process status update from DIRIGERA
     *
     * @param key
     * @param values
     */
    public Map<String, State> getAttributeUpdates(JSONObject values) {
        Map<String, State> channelUpdates = new HashMap<>();
        JSONObject attributeUpdates = values.optJSONObject(Model.ATTRIBUTES);
        if (attributeUpdates != null) {
            attributeUpdates.toMap().forEach((attributeKey, value) -> {
                Entry<String, State> update = attributeUpdate(attributeKey, value);
                if (update != null) {
                    channelUpdates.put(update.getKey(), update.getValue());
                }
            });
        }
        return channelUpdates;
    }

    private @Nullable Entry<String, State> attributeUpdate(String key, Object value) {
        JSONObject statusConfig = statusProperties.get(key);
        if (statusConfig != null) {
            String channel = statusConfig.optString(KEY_CHANNEL);
            State state = getState(value, statusConfig);
            if (channel != null && state != null) {
                System.out.println("Status update for key " + key + " with value " + value + " updates " + channel
                        + " to state " + state);
                return Map.entry(channel, state);
            }
        } else {
            // System.out.println("No status configuration found for key " + key);
        }
        return null;
    }

    public String getDeviceType() {
        return deviceType;
    }

    private @Nullable State getState(Object value, JSONObject config) {
        System.out.println(value.getClass().getSimpleName() + " value: " + value);
        String transformation = config.getString(KEY_TRANSFORMATION);

        var correctedValue = value;
        // correction for numeric values
        String correctionType = config.optString("correction");
        System.out.println(value.getClass().getSimpleName() + " value: " + value + " correction " + correctionType);
        if (!correctionType.isBlank() && value instanceof Number num) {
            double correctionValue = config.getDouble(correctionType);
            System.out.println(value.getClass().getSimpleName() + " value: " + value + " correction " + correctionType
                    + " " + correctionValue);
            switch (correctionType) {
                case "factor" -> correctedValue = num.doubleValue() * correctionValue;
                case "offset" -> correctedValue = num.doubleValue() + correctionValue;
            }
            System.out.println(value.getClass().getSimpleName() + " correctedValue: " + correctedValue);
        }

        // convert numbers in order to have either Int od Double values
        if (config.has("inValue") && correctedValue instanceof Number num) {
            System.out.println("CONVERSION Apply inValue conversion for " + correctedValue + " to "
                    + config.getString("inValue") + " " + value.getClass().getSimpleName());
            String inValue = config.getString("inValue");
            switch (inValue) {
                case "Integer" -> correctedValue = num.intValue();
                case "Float" -> correctedValue = num.doubleValue();
                case "Doublce" -> correctedValue = num.doubleValue();
            }
            System.out.println(
                    "CONVERSION Converted value: " + correctedValue + " " + correctedValue.getClass().getSimpleName());
        }

        // apply transformation
        var transformed = switch (transformation) {
            case "code" -> null; // will be handled´by java code
            case "raw" -> correctedValue.toString();
            case "format" -> String.format(Locale.US, config.getString("format"), correctedValue);
            case "mapping" -> map(correctedValue.toString(), config.getJSONObject("mapping"));
            default -> null;
        };
        if (transformed == null) {
            return null;
        }

        // convert into state type
        String outValue = config.getString("outValue");
        var state = switch (outValue) {
            case "DecimalType" -> new DecimalType(transformed);
            case "StringType" -> new StringType(transformed);
            case "QuantityType" -> QuantityType.valueOf(transformed);
            case "OnOffType" -> OnOffType.from(transformed);
            case "OpenClosedType" ->
                ("true".equalsIgnoreCase(transformed)) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            default -> UnDefType.NULL;
        };
        return state;
    }

    /**
     * Get JSON command to send to DIRIGERA for given channel and command
     *
     * @param channel
     * @param command
     */
    public Map<String, JSONObject> getRequestJson(String targetChannel, Command command) {
        // evaluate which target device to use
        Map<String, JSONObject> requests = new HashMap<>();
        System.out.println("Check " + controlPropertiesMapping.size() + " control properties for channel "
                + targetChannel + " and command " + command);
        controlPropertiesMapping.forEach((deviceId, controlProperties) -> {
            System.out.println("Check control properties for device " + deviceId + " " + controlProperties);
            JSONObject channelConfig = controlProperties.get(targetChannel);
            if (channelConfig != null) {
                JSONObject request = getRequest(command, channelConfig);
                if (!request.isEmpty()) {
                    requests.put(deviceId, request);
                }
            }
        });
        System.out.println("Requests: " + requests);
        return requests;
    }

    private JSONObject getRequest(Command command, JSONObject config) {
        String targetAttribute = config.getString(KEY_ATTRIBUTE);
        String transformation = config.getString(KEY_TRANSFORMATION);
        var commandValue = switch (transformation) {
            case "raw" -> command.toString();
            default -> null;
        };
        if (commandValue == null) {
            return new JSONObject();
        }
        String outValue = config.getString("json");
        var json = switch (outValue) {
            case KEY_ATTRIBUTE -> {
                JSONObject attributes = (new JSONObject()).put(targetAttribute, commandValue);
                JSONObject patch = (new JSONObject()).put(Model.ATTRIBUTES, attributes);
                yield patch;
            }
            default -> new JSONObject();
        };
        return json;
    }

    private String map(String value, JSONObject mapping) {
        Object mappingValue = mapping.opt(value);
        if (mappingValue == null) {
            return "";
        }
        return mappingValue.toString();
    }

    /**
     * Read complete device configuration for all Matter devices from resource file
     */
    public static void loadDeviceConfig() {
        String deviceConfig = ResourceReader.getResource("/json/matter/devices.json");
        JSONArray devices = new JSONArray(deviceConfig);
        devices.forEach(device -> {
            String tt = ((JSONObject) device).getString("thingType");
            matterDeviceConfig.put(tt, device);
        });
    }
}
