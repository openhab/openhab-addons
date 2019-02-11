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
    @SerializedName("OverrideTimeoutUnixTime")
    @Expose
    private Integer overrideTimeoutUnixTime;

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

    public Integer getOverrideTimeoutUnixTime() {
        return overrideTimeoutUnixTime;
    }

}
