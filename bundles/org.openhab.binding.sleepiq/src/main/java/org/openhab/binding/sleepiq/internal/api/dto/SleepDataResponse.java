/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.internal.api.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SleepDataResponse} holds the response to a request for sleep data.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SleepDataResponse {
    @SerializedName("sleeperId")
    private String sleeperId;

    @SerializedName("avgSleepIQ")
    private Integer averageSleepIQ;

    @SerializedName("avgHeartRate")
    private Integer averageHeartRate;

    @SerializedName("avgRespirationRate")
    private Integer averageResperationRate;

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
