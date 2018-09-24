/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airvisualnode.internal.json;

import java.util.List;
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
