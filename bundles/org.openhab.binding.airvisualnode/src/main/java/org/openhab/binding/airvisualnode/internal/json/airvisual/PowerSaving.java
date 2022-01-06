/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.airvisualnode.internal.json.airvisual;

import java.util.List;

import org.openhab.binding.airvisualnode.internal.json.PowerSavingTime;
import org.openhab.binding.airvisualnode.internal.json.PowerSavingTimeSlot;

import com.google.gson.annotations.SerializedName;

/**
 * Power saving data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class PowerSaving {

    @SerializedName("2slots")
    private List<PowerSavingTimeSlot> timeSlots = null;
    private String mode;
    @SerializedName("yes")
    private List<PowerSavingTime> times = null;

    public PowerSaving(List<PowerSavingTimeSlot> timeSlots, String mode, List<PowerSavingTime> times) {
        this.mode = mode;
        this.times = times;
        this.timeSlots = timeSlots;
    }

    public List<PowerSavingTimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<PowerSavingTimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public List<PowerSavingTime> getTimes() {
        return times;
    }

    public void setTimes(List<PowerSavingTime> times) {
        this.times = times;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
