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
 * The {@link VeSyncV2Ver2BypassAirPurifierSleepPref} is a Java class used as a DTO to hold the Vesync's API's
 * response data from the bypass API about the sleep preferences regarding an Air Purifier unit.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV2Ver2BypassAirPurifierSleepPref {
    @SerializedName("sleepPreferenceType")
    public String sleepPreferenceType;

    @SerializedName("cleaningBeforeBedSwitch")
    public int cleaningBeforeBedSwitch;

    @SerializedName("cleaningBeforeBedSpeedLevel")
    public int cleaningBeforeBedSpeedLevel;

    @SerializedName("cleaningBeforeBedMinutes")
    public int cleaningBeforeBedMinutes;

    @SerializedName("whiteNoiseSleepAidSwitch")
    public int whiteNoiseSleepAidSwitch;

    @SerializedName("whiteNoiseSleepAidSpeedLevel")
    public int whiteNoiseSleepAidSpeedLevel;

    @SerializedName("whiteNoiseSleepAidMinutes")
    public int whiteNoiseSleepAidMinutes;

    @SerializedName("duringSleepSpeedLevel")
    public int duringSleepSpeedLevel;

    @SerializedName("duringSleepMinutes")
    public int duringSleepMinutes;

    @SerializedName("afterWakeUpPowerSwitch")
    public int afterWakeUpPowerSwitch;

    @SerializedName("afterWakeUpWorkMode")
    public String afterWakeUpWorkMode;

    @SerializedName("afterWakeUpFanSpeedLevel")
    public String afterWakeUpFanSpeedLevel;
}
