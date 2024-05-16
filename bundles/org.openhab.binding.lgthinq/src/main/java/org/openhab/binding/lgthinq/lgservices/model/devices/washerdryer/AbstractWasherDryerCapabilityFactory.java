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
package org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapabilityFactory;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.MonitoringResultFormat;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseType;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link AbstractWasherDryerCapabilityFactory}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractWasherDryerCapabilityFactory extends AbstractCapabilityFactory<WasherDryerCapability> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractWasherDryerCapabilityFactory.class);

    protected abstract String getStateFeatureNodeName();

    protected abstract String getProcessStateNodeName();

    protected abstract String getPreStateFeatureNodeName();

    // --- Selectable features -----
    protected abstract String getRinseFeatureNodeName();

    protected abstract String getTemperatureFeatureNodeName();

    protected abstract String getSpinFeatureNodeName();

    // ------------------------------
    protected abstract String getSoilWashFeatureNodeName();

    protected abstract String getDoorLockFeatureNodeName();

    protected abstract MonitoringResultFormat getMonitorDataFormat(JsonNode rootNode);

    protected abstract String getCommandRemoteStartNodeName();

    protected abstract String getCommandStopNodeName();

    protected abstract String getCommandWakeUpNodeName();

    protected abstract String getDefaultCourseIdNodeName();

    @Override
    public WasherDryerCapability create(JsonNode rootNode) throws LGThinqException {
        WasherDryerCapability wdCap = super.create(rootNode);
        JsonNode coursesNode = rootNode.path(getCourseNodeName(rootNode));
        JsonNode smartCoursesNode = rootNode.path(getSmartCourseNodeName(rootNode));
        if (coursesNode.isMissingNode()) {
            throw new LGThinqException("Course node not present in Capability Json Descriptor");
        }

        Map<String, CourseDefinition> allCourses = new HashMap<>(getCourseDefinitions(coursesNode));
        allCourses.putAll(getSmartCourseDefinitions(smartCoursesNode));
        // TODO - Put Downloaded Course
        wdCap.setCourses(allCourses);

        JsonNode monitorValueNode = rootNode.path(getMonitorValueNodeName());
        if (monitorValueNode.isMissingNode()) {
            throw new LGThinqException("MonitoringValue node not found in the V2 WashingDryer cap definition.");
        }
        // mapping possible states
        wdCap.setState(newFeatureDefinition(getStateFeatureNodeName(), monitorValueNode));
        wdCap.setProcessState(newFeatureDefinition(getProcessStateNodeName(), monitorValueNode));
        // --- Selectable features -----
        wdCap.setRinseFeat(newFeatureDefinition(getRinseFeatureNodeName(), monitorValueNode,
                WM_CHANNEL_REMOTE_START_RINSE, WM_CHANNEL_RINSE_ID));
        wdCap.setTemperatureFeat(newFeatureDefinition(getTemperatureFeatureNodeName(), monitorValueNode,
                WM_CHANNEL_REMOTE_START_TEMP, WM_CHANNEL_TEMP_LEVEL_ID));
        wdCap.setSpinFeat(newFeatureDefinition(getSpinFeatureNodeName(), monitorValueNode, WM_CHANNEL_REMOTE_START_SPIN,
                WM_CHANNEL_SPIN_ID));
        // ----------------------------
        wdCap.setDryLevel(newFeatureDefinition(getDryLevelNodeName(), monitorValueNode));
        wdCap.setSoilWash(newFeatureDefinition(getSoilWashFeatureNodeName(), monitorValueNode));
        wdCap.setCommandsDefinition(getCommandsDefinition(rootNode));
        // DoorLock feat can be in alone (v2) or inside Options node (v1)
        if (monitorValueNode.get(getDoorLockFeatureNodeName()) != null
                || hasFeatInOptions(getDoorLockFeatureNodeName(), monitorValueNode)) {
            wdCap.setHasDoorLook(true);
        }
        wdCap.setDefaultCourseFieldName(getConfigCourseType(rootNode));
        wdCap.setDefaultSmartCourseFeatName(getConfigSmartCourseType(rootNode));
        wdCap.setCommandStop(getCommandStopNodeName());
        wdCap.setCommandRemoteStart(getCommandRemoteStartNodeName());
        wdCap.setCommandWakeUp(getCommandWakeUpNodeName());
        // custom feature values map.
        wdCap.setFeatureDefinitionMap(
                Map.of(getTemperatureFeatureNodeName(), new WasherDryerCapability.TemperatureFeatureFunction(),
                        getRinseFeatureNodeName(), new WasherDryerCapability.RinseFeatureFunction(),
                        getSpinFeatureNodeName(), new WasherDryerCapability.SpinFeatureFunction()));
        wdCap.setMonitoringDataFormat(getMonitorDataFormat(rootNode));
        wdCap.setDefaultCourseId(rootNode.path("Config").path(getDefaultCourseIdNodeName()).asText());
        return wdCap;
    }

    protected abstract boolean hasFeatInOptions(String featName, JsonNode monitoringValueNode);

    protected Map<String, CourseDefinition> getCourseDefinitions(JsonNode courseNode) {
        return Utils.getGenericCourseDefinitions(courseNode, CourseType.COURSE, getNotSelectedCourseKey());
    }

    protected Map<String, CourseDefinition> getSmartCourseDefinitions(JsonNode smartCourseNode) {
        return Utils.getGenericCourseDefinitions(smartCourseNode, CourseType.SMART_COURSE, getNotSelectedCourseKey());
    }

    protected abstract String getDryLevelNodeName();

    protected abstract String getNotSelectedCourseKey();

    @Override
    public final List<DeviceTypes> getSupportedDeviceTypes() {
        return List.of(DeviceTypes.WASHERDRYER_MACHINE, DeviceTypes.DRYER);
    }

    protected abstract String getCourseNodeName(JsonNode rootNode);

    protected abstract String getSmartCourseNodeName(JsonNode rootNode);

    protected abstract String getDefaultCourse(JsonNode rootNode);

    protected abstract String getRemoteFeatName();

    protected abstract String getStandByFeatName();

    protected abstract String getConfigCourseType(JsonNode rootNode);

    protected abstract String getConfigSmartCourseType(JsonNode rootNote);

    protected abstract String getConfigDownloadCourseType(JsonNode rootNode);

    protected abstract String getMonitorValueNodeName();
}
