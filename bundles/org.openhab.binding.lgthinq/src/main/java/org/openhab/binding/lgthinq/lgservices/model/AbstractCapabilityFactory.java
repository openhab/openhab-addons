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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The {@link AbstractCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractCapabilityFactory<T extends CapabilityDefinition> {
    protected ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(AbstractCapabilityFactory.class);

    public T create(JsonNode rootNode) throws LGThinqException {
        T cap = getCapabilityInstance();
        cap.setModelName(rootNode.path("Info").path("modelName").textValue());
        cap.setDeviceType(ModelUtils.getDeviceType(rootNode));
        cap.setDeviceVersion(ModelUtils.discoveryAPIVersion(rootNode));
        cap.setRawData(mapper.convertValue(rootNode, Map.class));
        switch (cap.getDeviceVersion()) {
            case V1_0:
                // V1 has Monitoring node describing the protocol data format
                JsonNode type = rootNode.path(getMonitoringNodeName()).path("type");
                if (!type.isMissingNode() && type.isTextual()) {
                    cap.setMonitoringDataFormat(MonitoringResultFormat.getFormatOf(type.textValue()));
                }
                break;
            case V2_0:
                // V2 doesn't have node describing the protocol because it's they unified Value (features) and
                // Monitoring nodes in the MonitoringValue node
                cap.setMonitoringDataFormat(MonitoringResultFormat.JSON_FORMAT);
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
                    logger.error("protocol node is missing in the capability descriptor for a binary monitoring");
                } else {
                    logger.error(
                            "protocol node is not and array in the capability descriptor for a binary monitoring ");
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
     * Return the feature definition, i.e, the defition of the device attributes that can be mapped to Channels.
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

    protected void validateMandatoryNote(JsonNode node) throws LGThinqException {
        if (node.isMissingNode()) {
            throw new LGThinqApiException(
                    String.format("Error extracting mandatory %s node for this device cap file", node));
        }
    }

    protected abstract Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode)
            throws LGThinqApiException;
}
