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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.WasherFeatureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * The {@link WasherDryerCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryerCapabilityFactoryV2 extends AbstractWasherDryerCapabilityFactory {
    private final Logger logger = LoggerFactory.getLogger(WasherDryerCapabilityFactoryV2.class);

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V2_0);
    }

    @Override
    public WasherDryerCapability create(JsonNode rootNode) throws LGThinqException {
        WasherDryerCapability cap = super.create(rootNode);
        cap.setRemoteStartFeatName("remoteStart");
        cap.setChildLockFeatName("standby");
        cap.setDoorLockFeatName("loadItemWasher");
        return cap;
    }

    @Override
    protected boolean hasFeatInOptions(String featName, JsonNode monitoringValueNode) {
        // there's no option node in V2
        return false;
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
        JsonNode labelNode = featureNode.path("label");
        if (!labelNode.isMissingNode() && !labelNode.isNull()) {
            fd.setLabel(labelNode.asText());
        } else {
            fd.setLabel(featureName);
        }
        // all features from V2 are enums
        fd.setDataType(FeatureDataType.ENUM);
        JsonNode valuesMappingNode = featureNode.path("valueMapping");
        if (!valuesMappingNode.isMissingNode()) {
            Map<String, String> valuesMapping = new HashMap<>();
            valuesMappingNode.fields().forEachRemaining(e -> {
                // collect values as:
                //
                // "POWEROFF": {
                // "index": 0,
                // "label": "@WM_STATE_POWER_OFF_W"
                // },
                // to "POWEROFF" -> "@WM_STATE_POWER_OFF_W"
                valuesMapping.put(e.getKey(), e.getValue().path("label").asText());
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
    protected String getCourseNodeName(JsonNode rootNode) {
        String courseType = getConfigCourseType(rootNode);
        return rootNode.path(getMonitorValueNodeName()).path(courseType).path("ref").textValue();
    }

    @Override
    protected String getSmartCourseNodeName(JsonNode rootNode) {
        return "SmartCourse";
    }

    private String getConfigNodeName() {
        return "Config";
    }

    @Override
    /*
     * Return the default Course Name
     * OBS:In the V2, the default course points to the default course <b>name</b>
     */
    protected String getDefaultCourse(JsonNode rootNode) {
        return rootNode.path(getConfigNodeName()).path("defaultCourse").textValue();
    }

    @Override
    protected String getRemoteFeatName() {
        return "remoteStart";
    }

    @Override
    protected String getStandByFeatName() {
        return "standby";
    }

    @Override
    protected String getConfigCourseType(JsonNode rootNode) {
        return rootNode.path(getConfigNodeName()).path("courseType").textValue();
    }

    protected String getConfigSmartCourseType(JsonNode rootNode) {
        return rootNode.path(getConfigNodeName()).path("smartCourseType").textValue();
    }

    protected String getConfigDownloadCourseType(JsonNode rootNode) {
        return rootNode.path(getConfigNodeName()).path("downloadedCourseType").textValue();
    }

    @Override
    protected String getStateFeatureNodeName() {
        return "state";
    }

    @Override
    protected String getProcessStateNodeName() {
        return "preState";
    }

    @Override
    protected String getPreStateFeatureNodeName() {
        return "preState";
    }

    @Override
    protected String getRinseFeatureNodeName() {
        return "rinse";
    }

    @Override
    protected String getTemperatureFeatureNodeName() {
        return "temp";
    }

    @Override
    protected String getSpinFeatureNodeName() {
        return "spin";
    }

    @Override
    protected String getSoilWashFeatureNodeName() {
        return "soilWash";
    }

    @Override
    protected String getDoorLockFeatureNodeName() {
        return "doorLock";
    }

    @Override
    protected MonitoringResultFormat getMonitorDataFormat(JsonNode rootNode) {
        // All v2 are Json format
        return MonitoringResultFormat.JSON_FORMAT;
    }

    @Override
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) {
        JsonNode commandNode = rootNode.path("ControlWifi");
        List<String> escapeDataValues = Arrays.asList("course", "SmartCourse", "doorLock", "childLock");
        if (commandNode.isMissingNode()) {
            logger.warn("No commands found in the DryerWasher definition. This is most likely a bug.");
            return Collections.emptyMap();
        }
        Map<String, CommandDefinition> commands = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = commandNode.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> e = it.next();
            String commandName = e.getKey();
            if ("vtCtrl".equals(commandName)) {
                // ignore command
                continue;
            }
            CommandDefinition cd = new CommandDefinition();
            JsonNode thisCommandNode = e.getValue();
            cd.setCommand(thisCommandNode.path("command").textValue());
            JsonNode dataValues = thisCommandNode.path("data").path("washerDryer");
            if (!dataValues.isMissingNode()) {
                Map<String, Object> data = new HashMap<>();
                dataValues.fields().forEachRemaining(f -> {
                    // only load features outside escape.
                    if (!escapeDataValues.contains(f.getKey())) {
                        if (f.getValue().isValueNode()) {
                            ValueNode vn = (ValueNode) f.getValue();
                            if (f.getValue().isTextual()) {
                                data.put(f.getKey(), vn.asText());
                            } else if (f.getValue().isNumber()) {
                                data.put(f.getKey(), vn.asInt());
                            }
                        }
                    }
                });
                // add extra data features
                data.put(getConfigCourseType(rootNode), "");
                data.put(getConfigSmartCourseType(rootNode), "");
                data.put("courseType", "");
                cd.setData(data);
                cd.setRawCommand(thisCommandNode.toPrettyString());
            } else {
                logger.warn("Data node not found in the WasherDryer definition. It's most likely a bug");
            }
            commands.put(commandName, cd);
        }
        return commands;
    }

    @Override
    protected String getCommandRemoteStartNodeName() {
        return "WMStart";
    }

    @Override
    protected String getCommandStopNodeName() {
        return "WMStop";
    }

    @Override
    protected String getCommandWakeUpNodeName() {
        return "WMWakeup";
    }

    @Override
    protected String getDefaultCourseIdNodeName() {
        return "defaultCourse";
    }

    @Override
    protected String getNotSelectedCourseKey() {
        return "NOT_SELECTED";
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "MonitoringValue";
    }

    @Override
    protected String getDryLevelNodeName() {
        return "dryLevel";
    }
}
