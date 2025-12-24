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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json;

import org.openhab.binding.ecovacs.internal.api.model.CleanMode;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class WorkStateReport {
    @SerializedName("paused")
    public int paused;
    @SerializedName("robotState")
    public RobotState robotState;
    @SerializedName("stationState")
    public DeviceState stationState;

    public static class DeviceState {
        @SerializedName("trigger")
        public String trigger; // app, workComplete, voice, ...?
        @SerializedName("state")
        public String state;
    }

    public static class RobotState extends DeviceState {
        @SerializedName("cleanState")
        public CleanStateReport cleanState;
    }

    public static class CleanStateReport {
        @SerializedName("cid")
        public String id;
        @SerializedName("type")
        public String type;
    }

    public CleanMode determineCleanMode(Gson gson) {
        if (paused != 0) {
            return CleanMode.PAUSE;
        }
        final String modeValue;
        if ("cleaning".equals(robotState.state) && robotState.cleanState != null) {
            modeValue = robotState.cleanState.type;
        } else if ("idle".equals(robotState.state)) {
            modeValue = stationState.state;
        } else {
            modeValue = robotState.state;
        }
        return gson.fromJson(modeValue, CleanMode.class);
    }
}
