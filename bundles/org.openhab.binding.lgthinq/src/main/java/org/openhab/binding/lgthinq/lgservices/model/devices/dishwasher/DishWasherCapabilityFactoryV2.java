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
package org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.WasherFeatureDefinition;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link DishWasherCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DishWasherCapabilityFactoryV2 extends AbstractDishWasherCapabilityFactory {

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

        FeatureDefinition fd;
        if ((fd = WasherFeatureDefinition.getBasicFeatureDefinition(featureName, featureNode, targetChannelId,
                refChannelId)) == FeatureDefinition.NULL_DEFINITION) {
            return fd;
        }
        // all features from V2 are enums
        return WasherFeatureDefinition.setAllValuesMapping(fd, featureNode);
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
