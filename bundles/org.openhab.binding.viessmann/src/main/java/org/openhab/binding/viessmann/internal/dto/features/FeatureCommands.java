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
package org.openhab.binding.viessmann.internal.dto.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link FeatureCommands} provides command of features
 *
 * @author Ronny Grun - Initial contribution
 */
public class FeatureCommands {
    public FeatureSetName setName;
    public FeatureSetCurve setCurve;
    public FeatureSetSchedule setSchedule;
    public FeatureSetMode setMode;
    public FeatureSetTemperature setTemperature;
    public FeatureDefaultCommands activate;
    public FeatureDefaultCommands deactivate;
    public FeatureChangeEndDate changeEndDate;
    public FeatureSchedule schedule;
    public FeatureDefaultCommands unschedule;
    public FeatureSetTargetTemperature setTargetTemperature;
    public FeatureSetTargetTemperature setMin;
    public FeatureSetTargetTemperature setMax;
    public FeatureSetLevels setLevels;
    public FeatureSetHysteresis setHysteresis;
    public FeatureSetHysteresis setHysteresisSwitchOnValue;
    public FeatureSetHysteresis setHysteresisSwitchOffValue;

    public ArrayList<String> getUsedCommands() {
        ArrayList<String> list = new ArrayList<>();
        if (setName != null) {
            list.add("setName");
        }
        if (setCurve != null) {
            list.add("setCurve");
        }
        if (setSchedule != null) {
            list.add("setSchedule");
        }
        if (setMode != null) {
            list.add("setMode");
        }
        if (setTemperature != null) {
            list.add("setTemperature");
        }
        if (activate != null) {
            list.add("activate");
        }
        if (deactivate != null) {
            list.add("deactivate");
        }
        if (changeEndDate != null) {
            list.add("changeEndDate");
        }
        if (schedule != null) {
            list.add("schedule");
        }
        if (unschedule != null) {
            list.add("unschedule");
        }
        if (setTargetTemperature != null) {
            list.add("setTargetTemperature");
        }
        if (setHysteresis != null) {
            list.add("setHysteresis");
        }
        if (setHysteresisSwitchOnValue != null) {
            list.add("setHysteresisSwitchOnValue");
        }
        if (setHysteresisSwitchOffValue != null) {
            list.add("setHysteresisSwitchOffValue");
        }
        return list;
    }

    public Map<String, String> getUris() {
        Map<String, String> uris = new HashMap<>();
        if (setName != null) {
            uris.put("setNameUri", setName.uri);
        }
        if (setCurve != null) {
            uris.put("setCurveUri", setCurve.uri);
        }
        if (setSchedule != null) {
            uris.put("setScheduleUri", setSchedule.uri);
        }
        if (setMode != null) {
            uris.put("setModeUri", setMode.uri);
        }
        if (setTemperature != null) {
            uris.put("setTemperatureUri", setTemperature.uri);
        }
        if (activate != null) {
            uris.put("activateUri", activate.uri);
        }
        if (deactivate != null) {
            uris.put("deactivateUri", deactivate.uri);
        }
        if (changeEndDate != null) {
            uris.put("changeEndDateUri", changeEndDate.uri);
        }
        if (schedule != null) {
            uris.put("scheduleUri", schedule.uri);
        }
        if (unschedule != null) {
            uris.put("unscheduleUri", unschedule.uri);
        }
        if (setTargetTemperature != null) {
            uris.put("setTargetTemperatureUri", setTargetTemperature.uri);
        }
        if (setHysteresis != null) {
            uris.put("setHysteresisUri", setHysteresis.uri);
        }
        if (setHysteresisSwitchOnValue != null) {
            uris.put("setHysteresisSwitchOnValueUri", setHysteresisSwitchOnValue.uri);
        }
        if (setHysteresisSwitchOffValue != null) {
            uris.put("setHysteresisSwitchOffValueUri", setHysteresisSwitchOffValue.uri);
        }

        return uris;
    }
}
