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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;

/**
 * The {@link WasherDryerCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WasherDryerCapability extends AbstractCapability<WasherDryerCapability> {
    private String defaultCourseFieldName = "";
    private String doorLockFeatName = "";
    private String childLockFeatName = "";
    private String defaultSmartCourseFieldName = "";
    private String commandRemoteStart = "";
    private String remoteStartFeatName = "";
    private String commandWakeUp = "";
    private String commandStop = "";
    private String defaultCourseId = "";
    private FeatureDefinition state = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition soilWash = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition spin = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition temperature = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition rinse = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition error = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition dryLevel = FeatureDefinition.NULL_DEFINITION;
    private FeatureDefinition processState = FeatureDefinition.NULL_DEFINITION;
    private boolean hasDoorLook;
    private boolean hasTurboWash;
    private Map<String, CommandDefinition> commandsDefinition = new HashMap<>();
    private Map<String, CourseDefinition> courses = new LinkedHashMap<>();

    static class RinseFeatureFunction implements Function<WasherDryerCapability, FeatureDefinition> {
        @Override
        public FeatureDefinition apply(WasherDryerCapability c) {
            return c.getRinseFeat();
        }
    }

    static class TemperatureFeatureFunction implements Function<WasherDryerCapability, FeatureDefinition> {
        @Override
        public FeatureDefinition apply(WasherDryerCapability c) {
            return c.getTemperatureFeat();
        }
    }

    static class SpinFeatureFunction implements Function<WasherDryerCapability, FeatureDefinition> {
        @Override
        public FeatureDefinition apply(WasherDryerCapability c) {
            return c.getSpinFeat();
        }
    }

    public String getDefaultCourseId() {
        return defaultCourseId;
    }

    public void setDefaultCourseId(String defaultCourseId) {
        this.defaultCourseId = defaultCourseId;
    }

    public Map<String, CommandDefinition> getCommandsDefinition() {
        return commandsDefinition;
    }

    public FeatureDefinition getDryLevel() {
        return dryLevel;
    }

    public String getCommandStop() {
        return commandStop;
    }

    public void setCommandStop(String commandStop) {
        this.commandStop = commandStop;
    }

    public String getCommandRemoteStart() {
        return commandRemoteStart;
    }

    public void setCommandRemoteStart(String commandRemoteStart) {
        this.commandRemoteStart = commandRemoteStart;
    }

    public String getCommandWakeUp() {
        return commandWakeUp;
    }

    public void setCommandWakeUp(String commandWakeUp) {
        this.commandWakeUp = commandWakeUp;
    }

    public void setDryLevel(FeatureDefinition dryLevel) {
        this.dryLevel = dryLevel;
    }

    public FeatureDefinition getProcessState() {
        return processState;
    }

    public void setProcessState(FeatureDefinition processState) {
        this.processState = processState;
    }

    public void setCommandsDefinition(Map<String, CommandDefinition> commandsDefinition) {
        this.commandsDefinition = commandsDefinition;
    }

    public Map<String, CourseDefinition> getCourses() {
        return courses;
    }

    public void setCourses(Map<String, CourseDefinition> courses) {
        this.courses = courses;
    }

    public FeatureDefinition getStateFeat() {
        return state;
    }

    public boolean hasDoorLook() {
        return this.hasDoorLook;
    }

    public void setHasDoorLook(boolean hasDoorLook) {
        this.hasDoorLook = hasDoorLook;
    }

    public void setState(FeatureDefinition state) {
        this.state = state;
    }

    public FeatureDefinition getSoilWash() {
        return soilWash;
    }

    public void setSoilWash(FeatureDefinition soilWash) {
        this.soilWash = soilWash;
    }

    public FeatureDefinition getSpinFeat() {
        return spin;
    }

    public void setSpinFeat(FeatureDefinition spin) {
        this.spin = spin;
    }

    public FeatureDefinition getTemperatureFeat() {
        return temperature;
    }

    public void setTemperatureFeat(FeatureDefinition temperature) {
        this.temperature = temperature;
    }

    public FeatureDefinition getRinseFeat() {
        return rinse;
    }

    public void setRinseFeat(FeatureDefinition rinse) {
        this.rinse = rinse;
    }

    public FeatureDefinition getError() {
        return error;
    }

    public void setError(FeatureDefinition error) {
        this.error = error;
    }

    public String getDefaultCourseFieldName() {
        return defaultCourseFieldName;
    }

    public void setDefaultCourseFieldName(String defaultCourseFieldName) {
        this.defaultCourseFieldName = defaultCourseFieldName;
    }

    public String getDefaultSmartCourseFeatName() {
        return defaultSmartCourseFieldName;
    }

    public void setDefaultSmartCourseFeatName(String defaultSmartCourseFieldName) {
        this.defaultSmartCourseFieldName = defaultSmartCourseFieldName;
    }

    public String getRemoteStartFeatName() {
        return remoteStartFeatName;
    }

    public void setRemoteStartFeatName(String remoteStartFeatName) {
        this.remoteStartFeatName = remoteStartFeatName;
    }

    public String getChildLockFeatName() {
        return childLockFeatName;
    }

    public void setChildLockFeatName(String childLockFeatName) {
        this.childLockFeatName = childLockFeatName;
    }

    public String getDoorLockFeatName() {
        return doorLockFeatName;
    }

    public void setDoorLockFeatName(String doorLockFeatName) {
        this.doorLockFeatName = doorLockFeatName;
    }
}
