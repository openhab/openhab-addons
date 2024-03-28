/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link WasherDryerCapabilityFactoryV1}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryerCapabilityFactoryV1 extends AbstractWasherDryerCapabilityFactory {
    private static final Logger logger = LoggerFactory.getLogger(WasherDryerCapabilityFactoryV1.class);

    @Override
    protected String getStateFeatureNodeName() {
        return "State";
    }

    @Override
    protected String getProcessStateNodeName() {
        return "ProcessState";
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
        // there is no dook lock node in V1.
        return "DUMMY_DOOR_LOCK";
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) throws LGThinqApiException {
        // V1 Commands are resolved direct in the ThinqService
        return Collections.EMPTY_MAP;
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
    protected FeatureDefinition getFeatureDefinition(String featureName, JsonNode featuresNode) {
        JsonNode featureNode = featuresNode.path(featureName);
        if (featureNode.isMissingNode()) {
            return FeatureDefinition.NULL_DEFINITION;
        }
        FeatureDefinition fd = new FeatureDefinition();
        fd.setName(featureName);
        fd.setLabel(featureName);
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
            AtomicReference<String> courseNodeName = new AtomicReference<>("");
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

    @Override
    protected Map<String, CourseDefinition> getCourseDefinitions(JsonNode courseNode) {
        // TODO
        return Collections.EMPTY_MAP;
    }

    @Override
    protected Map<String, CourseDefinition> getSmartCourseDefinitions(JsonNode smartCourseNode) {
        // TODO
        return Collections.EMPTY_MAP;
    }
}
