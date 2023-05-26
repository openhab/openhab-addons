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

import static org.openhab.binding.lgthinq.lgservices.model.DeviceTypes.fromDeviceTypeAcron;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ModelUtils}
 *
 * @author Nemer Daud - Initial contribution
 */
public class ModelUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static DeviceTypes getDeviceType(Map<String, Object> rootMap) {
        Map<String, String> infoMap = (Map<String, String>) rootMap.get("Info");
        Objects.requireNonNull(infoMap, "Unexpected error. Info node not present in capability schema");
        String productType = infoMap.get("productType");
        String modelType = infoMap.get("modelType");
        Objects.requireNonNull(infoMap, "Unexpected error. ProductType attribute not present in capability schema");
        return fromDeviceTypeAcron(productType, modelType);
    }

    public static DeviceTypes getDeviceType(JsonNode rootNode) {
        Map<String, Object> mapper = objectMapper.convertValue(rootNode, new TypeReference<>() {
        });
        return getDeviceType(mapper);
    }

    public static LGAPIVerion discoveryAPIVersion(JsonNode rootNode) {
        Map<String, Object> mapper = objectMapper.convertValue(rootNode, new TypeReference<>() {
        });
        return discoveryAPIVersion(mapper);
    }

    public static LGAPIVerion discoveryAPIVersion(Map<String, Object> rootMap) {
        DeviceTypes type = getDeviceType(rootMap);
        switch (type) {
            case AIR_CONDITIONER:
            case HEAT_PUMP:
                Map<String, Object> valueNode = (Map<String, Object>) rootMap.get("Value");
                if (valueNode.containsKey("support.airState.opMode")) {
                    return LGAPIVerion.V2_0;
                } else if (valueNode.containsKey("SupportOpMode")) {
                    return LGAPIVerion.V1_0;
                } else {
                    throw new IllegalStateException(
                            "Unexpected error. Can't find key node attributes to determine ACCapability API version.");
                }

            case WASHERDRYER_MACHINE:
            case DRYER:
            case REFRIGERATOR:
                if (rootMap.containsKey("Value")) {
                    return LGAPIVerion.V1_0;
                } else if (rootMap.containsKey("MonitoringValue")) {
                    return LGAPIVerion.V2_0;
                } else {
                    throw new IllegalStateException(
                            "Unexpected error. Can't find key node attributes to determine ACCapability API version.");
                }
            default:
                throw new IllegalStateException("Unexpected capability. The type " + type + " was not implemented yet");
        }
    }
}
