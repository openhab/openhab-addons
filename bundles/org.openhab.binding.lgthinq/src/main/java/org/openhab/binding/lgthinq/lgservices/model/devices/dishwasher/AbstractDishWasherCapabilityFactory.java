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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseType;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.Utils;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link AbstractDishWasherCapabilityFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDishWasherCapabilityFactory extends AbstractCapabilityFactory<DishWasherCapability> {

    protected abstract String getStateFeatureNodeName();

    protected abstract String getProcessStateNodeName();

    protected abstract String getDoorLockFeatureNodeName();

    protected abstract String getConvertingRulesNodeName();

    protected abstract String getControlConvertingRulesNodeName();

    @SuppressWarnings("unused")
    protected abstract MonitoringResultFormat getMonitorDataFormat(JsonNode rootNode);

    @Override
    public DishWasherCapability create(JsonNode rootNode) throws LGThinqException {
        DishWasherCapability dwCap = super.create(rootNode);
        JsonNode coursesNode = rootNode.path(getCourseNodeName());
        JsonNode smartCoursesNode = rootNode.path(getSmartCourseNodeName());
        if (coursesNode.isMissingNode()) {
            throw new LGThinqException("Course node not present in Capability Json Descriptor");
        }

        Map<String, CourseDefinition> courses = new HashMap<>(getCourseDefinitions(coursesNode));
        Map<String, CourseDefinition> smartCourses = new HashMap<>(getSmartCourseDefinitions(smartCoursesNode));
        Map<String, CourseDefinition> convertedAllCourses = new HashMap<>();
        // change the Key to the reverse MapCourses coming from LG API
        BiConsumer<Map<String, CourseDefinition>, JsonNode> convertCoursesRules = (courseMap, node) -> {
            node.fields().forEachRemaining(e -> {
                CourseDefinition df = courseMap.get(e.getKey());
                if (df != null) {
                    convertedAllCourses.put(e.getValue().asText(), df);
                }
            });
        };
        JsonNode controlConvertingRules = rootNode.path(getConvertingRulesNodeName()).path(getCourseNodeName())
                .path(getControlConvertingRulesNodeName());
        if (!controlConvertingRules.isMissingNode()) {
            convertCoursesRules.accept(courses, controlConvertingRules);
        }
        controlConvertingRules = rootNode.path(getConvertingRulesNodeName()).path(getSmartCourseNodeName())
                .path(getControlConvertingRulesNodeName());
        if (!controlConvertingRules.isMissingNode()) {
            convertCoursesRules.accept(smartCourses, controlConvertingRules);
        }

        dwCap.setCourses(convertedAllCourses);

        JsonNode monitorValueNode = rootNode.path(getMonitorValueNodeName());
        if (monitorValueNode.isMissingNode()) {
            throw new LGThinqException("MonitoringValue node not found in the V2 WashingDryer cap definition.");
        }
        // mapping possible states
        dwCap.setState(newFeatureDefinition(getStateFeatureNodeName(), monitorValueNode));
        dwCap.setProcessState(newFeatureDefinition(getProcessStateNodeName(), monitorValueNode));
        dwCap.setDoorStateFeat(newFeatureDefinition(getDoorLockFeatureNodeName(), monitorValueNode));
        dwCap.setMonitoringDataFormat(getMonitorDataFormat(rootNode));
        return dwCap;
    }

    protected Map<String, CourseDefinition> getCourseDefinitions(JsonNode courseNode) {
        return Utils.getGenericCourseDefinitions(courseNode, CourseType.COURSE, getNotSelectedCourseKey());
    }

    protected Map<String, CourseDefinition> getSmartCourseDefinitions(JsonNode smartCourseNode) {
        return Utils.getGenericCourseDefinitions(smartCourseNode, CourseType.SMART_COURSE, getNotSelectedCourseKey());
    }

    protected abstract String getNotSelectedCourseKey();

    @Override
    public final List<DeviceTypes> getSupportedDeviceTypes() {
        return List.of(DeviceTypes.DISH_WASHER);
    }

    protected abstract String getCourseNodeName();

    protected abstract String getSmartCourseNodeName();

    protected abstract String getMonitorValueNodeName();
}
