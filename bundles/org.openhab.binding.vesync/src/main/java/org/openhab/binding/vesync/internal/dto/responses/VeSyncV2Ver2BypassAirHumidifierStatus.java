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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncV2Ver2BypassAirHumidifierStatus} class is used as a DTO to hold the Vesync's API's
 * response data from the bypass API with regard's to the status of an air humidifier.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV2Ver2BypassAirHumidifierStatus {

    @SerializedName("powerSwitch")
    public int powerSwitch;

    public boolean getPowerSwitch() {
        return powerSwitch == 1;
    }

    @SerializedName("virtualLevel")
    public int virtualLevel;

    @SerializedName("mistLevel")
    public int mistLevel;

    @SerializedName("workMode")
    public String workMode;

    @SerializedName("waterLacksState")
    public int waterLacksState;

    public boolean getWaterLacksState() {
        return waterLacksState == 1;
    }

    @SerializedName("targetHumidity")
    public int targetHumidity;

    @SerializedName("autoStopState")
    public int autoStopState;

    public boolean getAutoStopState() {
        return autoStopState == 1;
    }

    @SerializedName("screenState")
    public int screenState;

    public boolean getScreenState() {
        return screenState == 1;
    }

    @SerializedName("screenSwitch")
    public int screenSwitch;

    public boolean getScreenSwitch() {
        return screenSwitch == 1;
    }

    @SerializedName("humidity")
    public int humidity;

    @SerializedName("waterTankLifted")
    public int waterTankLifted;

    public boolean getWaterTankLifted() {
        return waterTankLifted == 1;
    }

    @SerializedName("autoStopSwitch")
    public int autoStopSwitch;

    public boolean getAutoStopSwitch() {
        return autoStopSwitch == 1;
    }

    @SerializedName("scheduleCount")
    public int scheduleCount;

    @SerializedName("timerRemain")
    public int timerRemain;

    @SerializedName("errorCode")
    public int errorCode;
}
