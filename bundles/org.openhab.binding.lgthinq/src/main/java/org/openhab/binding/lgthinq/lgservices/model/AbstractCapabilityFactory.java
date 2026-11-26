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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

/**
 * The {@link AbstractCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractCapabilityFactory<T extends CapabilityDefinition> {
    protected final ObjectMapper mapper = JsonMapper.builder().configureForJackson2().build();
    private final Logger logger = LoggerFactory.getLogger(AbstractCapabilityFactory.class);

    public T create(JsonNode rootNode) throws LGThinqException {
        T cap = getCapabilityInstance();
        cap.setModelName(rootNode.path("Info").path("modelName").stringValue(null));
        cap.setDeviceType(ModelUtils.getDeviceType(rootNode));
        cap.setDeviceVersion(ModelUtils.discoveryAPIVersion(rootNode));
        cap.setRawData(Objects.requireNonNull(mapper.convertValue(rootNode, new TypeReference<Map<String, Object>>() {
        }), "ObjectMapper.convertValue returned null"));
        switch (cap.getDeviceVersion()) {
            case V1_0:
                // V1 has Monitoring node describing the protocol data format
                JsonNode type = rootNode.path(getMonitoringNodeName()).path("type");
                if (!type.isMissingNode() && type.isString()) {
                    cap.setMonitoringDataFormat(MonitoringResultFormat.getFormatOf(type.stringValue(null)));
                }
                break;
            case V2_0:
                // V2 doesn't have node describing the protocol because it's they unified Value (features) and
                // Monitoring nodes in the MonitoringValue node
                JsonNode v2type = rootNode.path(getMonitoringNodeName()).path("type");
                if (!v2type.isMissingNode() && v2type.isString()) {
                    cap.setMonitoringDataFormat(MonitoringResultFormat.getFormatOf(v2type.stringValue(null)));
                } else {
                    cap.setMonitoringDataFormat(MonitoringResultFormat.JSON_FORMAT);
                }
                break;
            default:
                cap.setMonitoringDataFormat(MonitoringResultFormat.UNKNOWN_FORMAT);
        }
        if (MonitoringResultFormat.BINARY_FORMAT.equals(cap.getMonitoringDataFormat())) {
            // get MonitorProtocol
            JsonNode protocol = rootNode.path(getMonitoringNodeName()).path("protocol");
            if (protocol.isArray()) {
                ArrayNode pNode = (ArrayNode) protocol;
                List<MonitoringBinaryProtocol> protocols = mapper.convertValue(pNode, new TypeReference<>() {
                });
                cap.setMonitoringBinaryProtocol(protocols);
            } else {
                if (protocol.isMissingNode()) {
                    logger.warn("protocol node is missing in the capability descriptor for a binary monitoring");
                } else {
                    logger.warn("protocol node is not and array in the capability descriptor for a binary monitoring ");
                }
            }
        }
        return cap;
    }

    /**
     * Return constant pointing to MonitoringNode. This node has information about monitoring response description,
     * <b>only present in V1 devices</b>. If some device has different node name for this descriptor, please override
     * it.
     *
     * @return Monitoring node name
     */
    protected String getMonitoringNodeName() {
        return "Monitoring";
    }

    protected abstract List<DeviceTypes> getSupportedDeviceTypes();

    protected abstract List<LGAPIVerion> getSupportedAPIVersions();

    /**
     * Return the feature definition, i.e, the definition of the device attributes that can be mapped to Channels.
     * The targetChannelId is needed if you intend to get the destination channelId for that feature, typically for
     * dynamic channels.
     *
     * @param featureName Name of the features: feature node name
     * @param featuresNode The jsonNode containing the data definition of the feature
     * @param targetChannelId The destination channelID, normally used when you want to create dynamic channels (outside
     *            xml)
     * @param refChannelId
     * @return the Feature definition.
     */
    protected abstract FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode,
            @Nullable String targetChannelId, @Nullable String refChannelId);

    protected FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode) {
        return newFeatureDefinition(featureName, featuresNode, null, null);
    }

    protected abstract T getCapabilityInstance();

    protected abstract Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode);

    /**
     * General method to parse commands for average of V1 Thinq Devices.
     *
     * @param rootNode ControlWifi root node
     * @return return map with commands definition
     */
    protected Map<String, CommandDefinition> getCommandsDefinitionV1(JsonNode rootNode) {
        boolean isBinaryCommands = MonitoringResultFormat.BINARY_FORMAT.getFormat()
                .equals(rootNode.path("ControlWifi").path("type").stringValue(null));
        JsonNode commandNode = rootNode.path("ControlWifi").path("action");
        if (commandNode.isMissingNode()) {
            logger.warn("No commands found in the devices's definition. This is most likely a bug.");
            return Collections.emptyMap();
        }
        Map<String, CommandDefinition> commands = new HashMap<>();
        for (Map.Entry<String, JsonNode> e : commandNode.properties()) {
            String commandName = e.getKey();
            CommandDefinition cd = new CommandDefinition();
            JsonNode thisCommandNode = e.getValue();
            JsonNode cmdField = thisCommandNode.path("cmd");
            if (cmdField.isMissingNode()) {
                // command not supported
                continue;
            }
            cd.setCommand(cmdField.stringValue(null));
            // cd.setCmdOpt(thisCommandNode.path("cmdOpt").stringValue(null));
            cd.setCmdOptValue(thisCommandNode.path("value").stringValue(null));
            cd.setBinary(isBinaryCommands);
            String strData = Objects.requireNonNullElse(thisCommandNode.path("data").stringValue(null), "");
            cd.setDataTemplate(strData);
            cd.setRawCommand(thisCommandNode.toPrettyString());
            int reservedIndex = 0;
            // keep the order
            if (!strData.isEmpty()) {
                Map<String, Object> data = new LinkedHashMap<>();
                for (String f : strData.split(",")) {
                    if (f.contains("{")) {
                        // it's a featured field
                        // create data entry with the key and blank value
                        data.put(f.replaceAll("[{\\[}\\]]", ""), "");
                    } else {
                        // its a fixed reserved value
                        data.put("Reserved" + reservedIndex, f.replaceAll("[{\\[}\\]]", ""));
                        reservedIndex++;
                    }
                }
                cd.setData(data);
            }
            commands.put(commandName, cd);
        }
        return commands;
    }
}
