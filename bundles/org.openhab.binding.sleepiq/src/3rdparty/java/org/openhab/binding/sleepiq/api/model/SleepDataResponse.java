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

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SleepDataResponse {
    @SerializedName("sleeperId")
    private String sleeperId;

    @SerializedName("avgSleepIQ")
    private Integer averageSleepIQ;

    @SerializedName("avgHeartRate")
    private Integer averageHeartRate;

    @SerializedName("avgRespirationRate")
    private Integer averageResperationRate;

    @SerializedName("totalSleepSessionTime")
    private Integer totalSleepSessionTime;

    @SerializedName("inBed")
    private Integer totalInBedSeconds;

    @SerializedName("outOfBed")
    private Integer totalOutOfBedSeconds;

    @SerializedName("restful")
    private Integer totalRestfulSeconds;

    @SerializedName("restless")
    private Integer totalRestlessSeconds;

    @SerializedName("sleepData")
    private List<SleepDataDay> sleepDataDays;

    public String getSleeperId() {
        return sleeperId;
    }

    public Integer getAverageSleepIQ() {
        return averageSleepIQ;
    }

    public Integer getAverageHeartRate() {
        return averageHeartRate;
    }

    public Integer getAverageRespirationRate() {
        return averageResperationRate;
    }

    public Integer getTotalSleepSessionTime() {
        return totalInBedSeconds + totalOutOfBedSeconds;
    }

    public Integer getTotalInBedSeconds() {
        return totalInBedSeconds;
    }

    public Integer getTotalOutOfBedSeconds() {
        return totalOutOfBedSeconds;
    }

    public Integer getTotalRestfulSeconds() {
        return totalRestfulSeconds;
    }

    public Integer getTotalRestlessSeconds() {
        return totalRestlessSeconds;
    }

    public List<SleepDataDay> getSleepDataDays() {
        return sleepDataDays;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SleepDataResponse [sleepData=");
        builder.append("averageSleepIQ=");
        builder.append(averageSleepIQ);
        builder.append(", averageHeartRate=");
        builder.append(averageHeartRate);
        builder.append(", averageResperationRate=");
        builder.append(averageResperationRate);
        builder.append(", totalInBedSeconds=");
        builder.append(totalInBedSeconds);
        builder.append(", totalOutOfBedSeconds=");
        builder.append(totalOutOfBedSeconds);
        builder.append(", totalRestfulSeconds=");
        builder.append(totalRestfulSeconds);
        builder.append(", totalRestlessSeconds=");
        builder.append(totalRestlessSeconds);
        builder.append(", totalSleepSessionTime=");
        builder.append(getTotalSleepSessionTime());
        builder.append("]");
        return builder.toString();
    }
}
