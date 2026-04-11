/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.dto.responses.devices.v2_2.airpurifier;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link V2StatusDetails} class is used as a DTO to hold the Vesync's API's
 * response data from the bypass API about the result of a request for the status of an air purifier.
 *
 * @author David Goodyear - Initial contribution
 */
public class V2StatusDetails {
    @SerializedName("AQLevel")
    public int airQuality;

    @SerializedName("powerSwitch")
    public int powerSwitch;

    public boolean getPowerSwitch() {
        return powerSwitch == 1;
    }

    @SerializedName("workMode")
    public String workMode;

    @SerializedName("fanSpeedLevel")
    public int fanSpeedLevel;

    @SerializedName("manualSpeedLevel")
    public int manualSpeedLevel;

    @SerializedName("filterLifePercent")
    public int filterLifePercent;

    @SerializedName("childLockSwitch")
    public int childLockSwitch;

    public boolean getChildLockSwitch() {
        return childLockSwitch == 1;
    }

    @SerializedName("screenState")
    public int screenState;

    public boolean getScreenState() {
        return screenState == 1;
    }

    @SerializedName("lightDetectionSwitch")
    public int lightDetectionSwitch;

    public boolean getLightDetectionSwitch() {
        return lightDetectionSwitch == 1;
    }

    @SerializedName("environmentLightState")
    public int environmentLightState;

    public boolean getEnvironmentLightState() {
        return environmentLightState == 1;
    }

    @SerializedName("screenSwitch")
    public int screenSwitch;

    public boolean getScreenSwitch() {
        return screenSwitch == 1;
    }

    @SerializedName("PM25")
    public int pm25;

    @SerializedName("timerRemain")
    public int timerRemain;

    @SerializedName("scheduleCount")
    public int scheduleCount;

    @SerializedName("efficientModeTimeRemain")
    public int efficientModeTimeRemain;

    @SerializedName("errorCode")
    public int errorCode;

    @SerializedName("autoPreference")
    public V2AutoSetup autoPreference;

    @SerializedName("sleepPreference")
    public V2SleepSetup sleepPreference;
}
