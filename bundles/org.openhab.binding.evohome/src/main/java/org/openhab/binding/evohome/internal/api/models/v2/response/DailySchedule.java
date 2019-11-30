/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the daily schedule
 *
 * @author James Kinsman - Initial Contribution
 *
 */
public class DailySchedule {

    @SerializedName("dayOfWeek")
    private String dayOfWeek;

    @SerializedName("switchpoints")
    private List<Switchpoint> switchpoints;

    public DailySchedule() {
        dayOfWeek = "Monday";
        switchpoints = new ArrayList<>();
    }

    public List<Switchpoint> getSwitchpoints() {
        return switchpoints;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getWeekday() {
        switch (dayOfWeek.toLowerCase()) {
            case "monday":
                return 0;
            case "tuesday":
                return 1;
            case "wednesday":
                return 2;
            case "thursday":
                return 3;
            case "friday":
                return 4;
            case "saturday":
                return 5;
            case "sunday":
                return 6;
            default:
                return -1;
        }
    }

}
