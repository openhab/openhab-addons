/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class SmartPlug {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("ScheduleId")
    @Expose
    private Integer scheduleId;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("ManualState")
    @Expose
    private String manualState;
    @SerializedName("Mode")
    @Expose
    private String mode;
    @SerializedName("AwayAction")
    @Expose
    private String awayAction;
    @SerializedName("OutputState")
    @Expose
    private String outputState;
    @SerializedName("ControlSource")
    @Expose
    private String controlSource;
    @SerializedName("ScheduledState")
    @Expose
    private String scheduledState;
    @SerializedName("TargetState")
    @Expose
    private String targetState;
    @SerializedName("DebounceCount")
    @Expose
    private Integer debounceCount;
    @SerializedName("OverrideState")
    @Expose
    private String overrideState;

    public Integer getId() {
        return id;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public String getName() {
        return name;
    }

    public String getManualState() {
        return manualState;
    }

    public String getAwayAction() {
        return awayAction;
    }

    public String getOutputState() {
        return outputState;
    }

    public String getControlSource() {
        return controlSource;
    }

    public String getScheduledState() {
        return scheduledState;
    }

    public String getTargetState() {
        return targetState;
    }

    public Integer getDebounceCount() {
        return debounceCount;
    }

    public void setDebounceCount(Integer count) {
        this.debounceCount = count;
    }

    public String getOverrideState() {
        return overrideState;
    }

    public String getMode() {
        return mode;
    }
}
