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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SleepDataSession} holds the data for a sleep session.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SleepDataSession {

    @SerializedName("sleepQuotient")
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
