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
public class HotWater {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("OverrideType")
    @Expose
    private String overrideType;
    @SerializedName("ScheduleId")
    @Expose
    private Integer scheduleId;
    @SerializedName("Mode")
    @Expose
    private String mode;
    @SerializedName("WaterHeatingState")
    @Expose
    private String waterHeatingState;
    @SerializedName("HotWaterRelayState")
    @Expose
    private String hotWaterRelayState;

    public Integer getId() {
        return id;
    }

    public String getOverrideType() {
        return overrideType;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public String getMode() {
        return mode;
    }

    public String getWaterHeatingState() {
        return waterHeatingState;
    }

    public String getHotWaterRelayState() {
        return hotWaterRelayState;
    }

}
