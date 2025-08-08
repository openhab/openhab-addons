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
package org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.WasherFeatureDefinition;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link WasherDryerCapabilityFactoryV1}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryerCapabilityFactoryV1 extends AbstractWasherDryerCapabilityFactory {

    @Override
    public WasherDryerCapability create(JsonNode rootNode) throws LGThinqException {
        WasherDryerCapability cap = super.create(rootNode);
        cap.setRemoteStartFeatName("RemoteStart");
        cap.setChildLockFeatName("ChildLock");
        cap.setDoorLockFeatName("DoorLock");
        return cap;
    }

    @Override
    protected boolean hasFeatInOptions(String featName, JsonNode monitoringValueNode) {
        for (String optionNode : new String[] { "Option1", "Option2" }) {
            JsonNode arrNode = monitoringValueNode.path(optionNode).path("option");
            if (arrNode.isArray()) {
                for (JsonNode v : arrNode) {
                    if (v.asText().equals(featName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected String getStateFeatureNodeName() {
        return "State";
    }

    @Override
    protected String getProcessStateNodeName() {
        return "PreState";
    }

    @Override
    protected String getPreStateFeatureNodeName() {
        return "PreState";
    }

    @Override
    protected String getRinseFeatureNodeName() {
        return "RinseOption";
    }

    @Override
    protected String getTemperatureFeatureNodeName() {
        return "WaterTemp";
    }

    @Override
    protected String getSpinFeatureNodeName() {
        return "SpinSpeed";
    }

    @Override
    protected String getSoilWashFeatureNodeName() {
        return "Wash";
    }

    @Override
    protected String getDoorLockFeatureNodeName() {
        return "DoorLock";
    }

    @Override
    protected MonitoringResultFormat getMonitorDataFormat(JsonNode rootNode) {
        String type = rootNode.path("Monitoring").path("type").textValue();
        return MonitoringResultFormat.getFormatOf(Objects.requireNonNullElse(type, ""));
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) {
        return getCommandsDefinitionV1(rootNode);
    }

    @Override
    protected String getCommandRemoteStartNodeName() {
        return "OperationStart";
    }

    @Override
    protected String getCommandStopNodeName() {
        return "OperationStop";
    }

    @Override
    protected String getCommandWakeUpNodeName() {
        return "OperationWakeUp";
    }

    @Override
    protected String getDefaultCourseIdNodeName() {
        return "defaultCourseId";
    }

    @Override
    protected String getDryLevelNodeName() {
        return "DryLevel";
    }

    @Override
    protected String getNotSelectedCourseKey() {
        return "0";
    }

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V1_0);
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
        fd.setLabel(featureName);
        fd.setChannelId(Objects.requireNonNullElse(targetChannelId, ""));
        fd.setRefChannelId(Objects.requireNonNullElse(refChannelId, ""));
        // All features from V1 are ENUMs
        return WasherFeatureDefinition.setAllValuesMapping(fd, featureNode);
    }

    @Override
    public WasherDryerCapability getCapabilityInstance() {
        return new WasherDryerCapability();
    }

    @Override
    /*
     * Return the default Course ID.
     * OBS:In the V1, the default course points to the ID of the course list that is the default.
     */
    protected String getDefaultCourse(JsonNode rootNode) {
        return rootNode.path("Config").path("defaultCourseId").textValue();
    }

    @Override
    protected String getRemoteFeatName() {
        return "RemoteStart";
    }

    @Override
    protected String getStandByFeatName() {
        return "Standby";
    }

    @Override
    protected String getConfigCourseType(JsonNode rootNode) {
        if (rootNode.path(getMonitorValueNodeName()).path("APCourse").isMissingNode()) {
            return "Course";
        } else {
            return "APCourse";
        }
    }

    @Override
    protected String getCourseNodeName(JsonNode rootNode) {
        JsonNode refOptions = rootNode.path(getMonitorValueNodeName()).path(getConfigCourseType(rootNode))
                .path("option");
        if (refOptions.isArray()) {
            for (JsonNode node : refOptions) {
                return node.asText();
            }
        }
        return "";
    }

    @Override
    protected String getSmartCourseNodeName(JsonNode rootNode) {
        return "SmartCourse";
    }

    @Override
    protected String getConfigSmartCourseType(JsonNode rootNote) {
        return "SmartCourse";
    }

    @Override
    protected String getConfigDownloadCourseType(JsonNode rootNode) {
        // just to ignore because there is no DownloadCourseType in V1
        return "XXXXXXXXXXX";
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "Value";
    }
}
