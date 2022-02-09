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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.Capability;

/**
 * The {@link WMCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WMCapability extends Capability {
    public enum MonitoringCap {
        STATE("state"),
        SOIL_WASH("soilWash"),
        SPIN("spin"),
        TEMPERATURE("temp"),
        RINSE("rinse");

        final String value;

        MonitoringCap(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static class MonitoringValue {
        private Map<String, String> state = new LinkedHashMap<String, String>();
        private Map<String, String> soilWash = new LinkedHashMap<String, String>();
        private Map<String, String> spin = new LinkedHashMap<String, String>();
        private Map<String, String> temperature = new LinkedHashMap<String, String>();
        private Map<String, String> rinse = new LinkedHashMap<String, String>();
        private boolean hasDoorLook;
        private boolean hasTurboWash;
    }

    private MonitoringValue monitoringValue = new MonitoringValue();
    private Map<String, String> courses = new LinkedHashMap<String, String>();

    private Map<String, String> smartCourses = new LinkedHashMap<String, String>();

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

    public Map<String, String> getSoilWash() {
        return monitoringValue.soilWash;
    }

    public Map<String, String> getSpin() {
        return monitoringValue.spin;
    }

    public Map<String, String> getTemperature() {
        return monitoringValue.temperature;
    }

    public Map<String, String> getRinse() {
        return monitoringValue.rinse;
    }

    public boolean hasDoorLook() {
        return monitoringValue.hasDoorLook;
    }

    public void setHasDoorLook(boolean hasDoorLook) {
        monitoringValue.hasDoorLook = hasDoorLook;
    }

    public boolean hasTurboWash() {
        return monitoringValue.hasTurboWash;
    }

    public void setHasTurboWash(boolean hasTurboWash) {
        monitoringValue.hasTurboWash = hasTurboWash;
    }

    public void addMonitoringValue(MonitoringCap monCap, String key, String value) {
        switch (monCap) {
            case STATE:
                monitoringValue.state.put(key, value);
                break;
            case SOIL_WASH:
                monitoringValue.soilWash.put(key, value);
                break;
            case SPIN:
                monitoringValue.spin.put(key, value);
                break;
            case TEMPERATURE:
                monitoringValue.temperature.put(key, value);
                break;
            case RINSE:
                monitoringValue.rinse.put(key, value);
                break;
        }
    }
}
