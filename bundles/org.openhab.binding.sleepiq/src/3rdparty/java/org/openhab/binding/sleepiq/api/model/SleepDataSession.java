/*
 * Copyright 2022 Mark Hilbush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.model;

import com.google.gson.annotations.SerializedName;

public class SleepDataSession {

    @SerializedName("avgSleepIQ")
    private Integer sessionAverageSleepIQ;

    @SerializedName("avgHeartRate")
    private Integer sessionAverageHeartRate;

    @SerializedName("avgRespirationRate")
    private Integer sessionAverageRespirationRate;

    @SerializedName("isFinalized")
    private boolean sessionIsFinalized;

    @SerializedName("totalSleepSessionTime")
    private Integer sessionTotalSeconds;

    @SerializedName("inBed")
    private Integer sessionInBedSeconds;

    @SerializedName("outOfBed")
    private Integer sessionOutOfBedSeconds;

    @SerializedName("restful")
    private Integer sessionRestfulSeconds;

    @SerializedName("restless")
    private Integer sessionRestlessSeconds;

    public Integer getSessionAverageSleepIQ() {
        return sessionAverageSleepIQ;
    }

    public Integer getSessionAverageHeartRate() {
        return sessionAverageHeartRate;
    }

    public Integer getSessionAverageRespirationRate() {
        return sessionAverageRespirationRate;
    }

    public Boolean sessionIsFinalized() {
        return sessionIsFinalized;
    }

    public Integer getSessionTotalSeconds() {
        return sessionTotalSeconds;
    }

    public Integer getSessionInBedSeconds() {
        return sessionInBedSeconds;
    }

    public Integer getSessionOutOfBedSeconds() {
        return sessionOutOfBedSeconds;
    }

    public Integer getSessionRestfulSeconds() {
        return sessionRestfulSeconds;
    }

    public Integer getSessionRestlessSeconds() {
        return sessionRestlessSeconds;
    }
}
