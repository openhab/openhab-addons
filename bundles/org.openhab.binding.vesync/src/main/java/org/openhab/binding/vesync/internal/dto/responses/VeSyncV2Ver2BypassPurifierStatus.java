/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link VeSyncV2Ver2BypassPurifierStatus} is a Java class used as a DTO to hold the Vesync's API's common
 * response data, in regards to an Air Purifier based device, using the latest encoding protocol scheme.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV2Ver2BypassPurifierStatus extends VeSyncResponse {

    @SerializedName("result")
    public PurifierStatus result;

    public class PurifierStatus extends VeSyncResponse {

        @SerializedName("result")
        public AirPurifierStatus result;

        public class AirPurifierStatus {
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
            public VeSyncV2Ver2BypassPurifierStatus.PurifierStatus.AirPurifierStatus.AirPurifierConfigAutoPref autoPreference;

            public class AirPurifierConfigAutoPref {
                @SerializedName("autoPreferenceType")
                public String autoType;

                @SerializedName("roomSize")
                public int roomSize;
            }

            @SerializedName("sleepPreference")
            public VeSyncV2Ver2BypassPurifierStatus.PurifierStatus.AirPurifierStatus.AirPurifierSleepPref sleepPreference;

            public class AirPurifierSleepPref {
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
        }
    }
}
