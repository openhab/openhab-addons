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

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * The {@link WasherDryerCapabilityFactoryV2}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryerCapabilityFactoryV2 extends AbstractWasherDryerCapabilityFactory {
    private static final Logger logger = LoggerFactory.getLogger(WasherDryerCapabilityFactoryV2.class);

    @Override
    protected List<LGAPIVerion> getSupportedAPIVersions() {
        return List.of(LGAPIVerion.V2_0);
    }

    @Override
    protected FeatureDefinition getFeatureDefinition(String featureName, JsonNode featuresNode) {
        JsonNode featureNode = featuresNode.path(featureName);
        if (featureNode.isMissingNode()) {
            return FeatureDefinition.NULL_DEFINITION;
        }
        FeatureDefinition fd = new FeatureDefinition();
        fd.setName(featureName);
        JsonNode labelNode = featureNode.path("label");
        if (!labelNode.isMissingNode() && !labelNode.isNull()) {
            fd.setLabel(labelNode.asText());
        } else {
            fd.setLabel(featureName);
        }
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
        return "ProcessState";
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
    protected Map<String, CommandDefinition> getCommandsDefinition(JsonNode rootNode) {
        JsonNode commandNode = rootNode.path("ControlWifi");
        List<String> escapeDataValues = Arrays.asList("course", "SmartCourse", "doorLock", "childLock");
        if (commandNode.isMissingNode()) {
            logger.warn("No commands found in the DryerWasher definition. This is most likely a bug.");
            return Collections.EMPTY_MAP;
        }
        Map<String, CommandDefinition> commands = new HashMap<>();
        commandNode.fields().forEachRemaining(e -> {
            String commandName = e.getKey();
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
            } else {
                logger.warn("Data node not found in the WasherDryer definition. It's most likely a bug");
            }
            commands.put(commandName, cd);
        });
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
    protected String getNotSelectedCourseKey() {
        return "NOT_SELECTED";
    }

    @Override
    protected String getMonitorValueNodeName() {
        return "MonitoringValue";
    }

    private Map<String, CourseDefinition> getGenericCourseDefinitions(JsonNode courseNode, CourseType type) {
        Map<String, CourseDefinition> coursesDef = new HashMap<>();
        courseNode.fields().forEachRemaining(e -> {
            CourseDefinition cd = new CourseDefinition();
            JsonNode thisCourseNode = e.getValue();
            cd.setCourseName(thisCourseNode.path("_comment").textValue());
            if (CourseType.SMART_COURSE.equals(type)) {
                cd.setBaseCourseName(thisCourseNode.path("Course").textValue());
            }
            cd.setCourseType(type);
            if (thisCourseNode.path("function").isArray()) {
                // just to be safe here
                ArrayNode functions = (ArrayNode) thisCourseNode.path("function");
                List<CourseFunction> functionList = cd.getFunctions();
                for (JsonNode fNode : functions) {
                    // map all course functions here
                    CourseFunction f = new CourseFunction();
                    f.setValue(fNode.path("value").textValue());
                    f.setDefaultValue(fNode.path("default").textValue());
                    JsonNode selectableNode = fNode.path("selectable");
                    // only Courses (not SmartCourses or DownloadedCourses) can have selectable functions
                    f.setSelectable(
                            !selectableNode.isMissingNode() && selectableNode.isArray() && (type == CourseType.COURSE));
                    if (f.isSelectable()) {
                        List<String> selectableValues = f.getSelectableValues();
                        // map values acceptable for this function
                        for (JsonNode v : (ArrayNode) selectableNode) {
                            if (v.isValueNode()) {
                                selectableValues.add(v.textValue());
                            }
                        }
                        f.setSelectableValues(selectableValues);
                    }
                    functionList.add(f);
                }
                cd.setFunctions(functionList);
            }
            coursesDef.put(e.getKey(), cd);
        });
        CourseDefinition cdNotSelected = new CourseDefinition();
        cdNotSelected.setCourseType(type);
        cdNotSelected.setCourseName("Not Selected");
        coursesDef.put(getNotSelectedCourseKey(), cdNotSelected);
        return coursesDef;
    }

    @Override
    protected Map<String, CourseDefinition> getCourseDefinitions(JsonNode courseNode) {
        return getGenericCourseDefinitions(courseNode, CourseType.COURSE);
    }

    @Override
    protected Map<String, CourseDefinition> getSmartCourseDefinitions(JsonNode smartCourseNode) {
        return getGenericCourseDefinitions(smartCourseNode, CourseType.SMART_COURSE);
    }

    @Override
    protected String getDryLevelNodeName() {
        return "dryLevel";
    }
}
