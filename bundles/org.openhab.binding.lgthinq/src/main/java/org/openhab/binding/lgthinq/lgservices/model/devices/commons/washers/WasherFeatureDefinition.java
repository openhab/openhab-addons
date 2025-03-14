/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The WasherFeatureDefinition
 *
 * @author nemer (nemer.daud@gmail.com) - Initial contribution
 */
@NonNullByDefault
public class WasherFeatureDefinition {
    public static FeatureDefinition getBasicFeatureDefinition(String featureName, JsonNode featureNode,
            @Nullable String targetChannelId, @Nullable String refChannelId) {
        if (featureNode.isMissingNode()) {
            return FeatureDefinition.NULL_DEFINITION;
        }
        FeatureDefinition fd = new FeatureDefinition();
        fd.setName(featureName);
        fd.setChannelId(Objects.requireNonNullElse(targetChannelId, ""));
        fd.setRefChannelId(Objects.requireNonNullElse(refChannelId, ""));
        fd.setLabel(featureName);
        return fd;
    }

    public static FeatureDefinition setAllValuesMapping(FeatureDefinition fd, JsonNode featureNode) {
        fd.setDataType(FeatureDataType.ENUM);
        JsonNode valuesMappingNode = featureNode.path("option");
        if (!valuesMappingNode.isMissingNode()) {
            Map<String, String> valuesMapping = new HashMap<>();
            valuesMappingNode.fields().forEachRemaining(e -> {
                // collect values as:
                //
                // "option":{
                // "0":"@WM_STATE_POWER_OFF_W",
                // to "0" -> "@WM_STATE_POWER_OFF_W"
                valuesMapping.put(e.getKey(), e.getValue().asText());
            });
            fd.setValuesMapping(valuesMapping);
        }

        return fd;
    }
}
