/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.radiothermostat.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioThermostatJsonTime} is responsible for storing
 * the "time" node from the thermostat JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatJsonTime {
    @SerializedName("day")
    private Integer dayOfWeek = -1;

    @SerializedName("hour")
    private Integer hour = 0;

    @SerializedName("minute")
    private Integer minute = 0;

    public RadioThermostatJsonTime() {
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public Integer getHour() {
        return hour;
    }

    public Integer getMinute() {
        return minute;
    }

    /**
     * Convenience method to return the total number of runtime minutes
     * 
     * @return {runtime hours + minutes as minutes Integer}
     */
    public Integer getRuntime() {
        return (hour * 60) + minute;
    }

    /**
     * Get formatted thermostat date stamp
     *
     * @return {Day of week/Time string}
     */
    public String getThemostatDateTime() {
        String day;

        switch (dayOfWeek.toString()) {
            case "0":
                day = "Monday ";
                break;
            case "1":
                day = "Tuesday ";
                break;
            case "2":
                day = "Wedensday ";
                break;
            case "3":
                day = "Thursday ";
                break;
            case "4":
                day = "Friday ";
                break;
            case "5":
                day = "Saturday ";
                break;
            case "6":
                day = "Sunday ";
                break;
            default:
                day = "";
        }
        return day + hour + ":" + String.format("%02d", minute);
    }
}
