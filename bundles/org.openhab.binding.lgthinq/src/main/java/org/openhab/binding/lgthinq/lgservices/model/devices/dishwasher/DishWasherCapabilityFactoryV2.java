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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link DishWasherCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DishWasherCapabilityFactoryV2 extends AbstractDishWasherCapabilityFactory {
    private static final Logger logger = LoggerFactory.getLogger(DishWasherCapabilityFactoryV2.class);

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V2_0);
    }

    protected String getDoorLockFeatureNodeName() {
        return "Door";
    }

    @Override
    protected String getConvertingRulesNodeName() {
        return "ConvertingRule";
    }

    @Override
    protected String getControlConvertingRulesNodeName() {
        return "ControlConvertingRule";
    }

    @Override
    protected FeatureDefinition newFeatureDefinition(String featureName, JsonNode featuresNode,
            @Nullable String targetChannelId, @Nullable String refChannelId) {
        JsonNode featureNode = featuresNode.path(featureName);
        if (featureNode.isMissingNode()) {
            return FeatureDefinition.NULL_DEFINITION;
        }
        FeatureDefinition fd = new FeatureDefinition();
        fd.setName(featureName);
        fd.setChannelId(Objects.requireNonNullElse(targetChannelId, ""));
        fd.setRefChannelId(Objects.requireNonNullElse(refChannelId, ""));
        fd.setLabel(featureName);

        // all features from V2 are enums
        fd.setDataType(FeatureDataType.ENUM);
        // surprisingly the DW V2 has the same json struct as V1 of other devices.
        JsonNode optionsNode = featureNode.path("option");
        if (!optionsNode.isMissingNode()) {

            Map<String, String> options = new HashMap<>();
            optionsNode.fields().forEachRemaining(e -> {
                // collect values as:
                //
                // "State": {
                // "type": "Enum",
                // "default": "POWEROFF",
                // "option": {
                // "POWEROFF": "@DW_STATE_POWER_OFF_W",
                // "INITIAL": "@DW_STATE_INITIAL_W",
                // "RUNNING": "@DW_STATE_RUNNING_W",
                // "PAUSE": "@DW_STATE_PAUSE_W",
                // "STANDBY": "@DW_STATE_POWER_OFF_W",
                // "END": "@DW_STATE_COMPLETE_W",
                // "POWERFAIL": "@DW_STATE_POWER_FAIL_W"
                // }
                // },
                options.put(e.getKey(), e.getValue().asText());
            });
            fd.setValuesMapping(options);
        }

        return fd;
    }

    @Override
    public DishWasherCapability getCapabilityInstance() {
        return new DishWasherCapability();
    }

    @Override
    protected String getCourseNodeName() {
        return "Course";
    }

    @Override
    protected String getSmartCourseNodeName() {
        return "SmartCourse";
    }

    private String getConfigNodeName() {
        return "Config";
    }

    @Override
    protected String getStateFeatureNodeName() {
        return "State";
    }

    @Override
    protected String getProcessStateNodeName() {
        return "Process";
    }

    @Override
    protected MonitoringResultFormat getMonitorDataFormat(JsonNode rootNode) {
        // All v2 are Json format
        return MonitoringResultFormat.JSON_FORMAT;
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) {
        return Collections.emptyMap();
    }

    @Override
    protected String getNotSelectedCourseKey() {
        return "NOT_SELECTED";
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "Value";
    }
}
