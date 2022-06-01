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
package org.openhab.binding.lgthinq.lgservices.model.dryer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;

/**
 * The {@link DryerCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DryerCapability extends AbstractCapability {
    public enum MonitoringCap {
        STATE_V2("state"),
        PROCESS_STATE_V2("processState"),
        DRY_LEVEL_V2("dryLevel"),
        ERROR_V2("error"),
        STATE_V1("State"),
        PROCESS_STATE_V1("PreState"),
        ERROR_V1("Error");

        final String value;

        MonitoringCap(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static class MonitoringValue {
        private final Map<String, String> state = new LinkedHashMap<String, String>();
        private final Map<String, String> dryLevel = new LinkedHashMap<String, String>();
        private final Map<String, String> error = new LinkedHashMap<String, String>();
        private final Map<String, String> processState = new LinkedHashMap<String, String>();
        private boolean hasChildLock;
        private boolean hasRemoteStart;
    }

    private final MonitoringValue monitoringValue = new MonitoringValue();
    private final Map<String, String> courses = new LinkedHashMap<String, String>();

    private final Map<String, String> smartCourses = new LinkedHashMap<String, String>();

    public Map<String, String> getCourses() {
        return courses;
    }

    public Map<String, String> getSmartCourses() {
        return smartCourses;
    }

    public void addCourse(String courseLabel, String courseName) {
        courses.put(courseLabel, courseName);
    }

    public void addSmartCourse(String courseLabel, String courseName) {
        smartCourses.put(courseLabel, courseName);
    }

    public Map<String, String> getState() {
        return monitoringValue.state;
    }

    public Map<String, String> getDryLevels() {
        return monitoringValue.dryLevel;
    }

    public Map<String, String> getErrors() {
        return monitoringValue.error;
    }

    public Map<String, String> getProcessStates() {
        return monitoringValue.processState;
    }

    public boolean hasRemoteStart() {
        return monitoringValue.hasRemoteStart;
    }

    public boolean hasChildLock() {
        return monitoringValue.hasChildLock;
    }

    public void setChildLock(boolean hasChildLock) {
        monitoringValue.hasChildLock = hasChildLock;
    }

    public void setRemoteStart(boolean hasRemoteStart) {
        monitoringValue.hasRemoteStart = hasRemoteStart;
    }

    public void addMonitoringValue(MonitoringCap monCap, String key, String value) {
        switch (monCap) {
            case STATE_V2:
            case STATE_V1:
                monitoringValue.state.put(key, value);
                break;
            case PROCESS_STATE_V2:
            case PROCESS_STATE_V1:
                monitoringValue.processState.put(key, value);
                break;
            case DRY_LEVEL_V2:
                monitoringValue.dryLevel.put(key, value);
                break;
            case ERROR_V1:
            case ERROR_V2:
                monitoringValue.error.put(key, value);
                break;
        }
    }
}
