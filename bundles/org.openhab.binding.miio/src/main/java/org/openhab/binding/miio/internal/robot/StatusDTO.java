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
package org.openhab.binding.miio.internal.robot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the status message json structure
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class StatusDTO {

    @SerializedName("msg_ver")
    @Expose
    private Integer msgVer;
    @SerializedName("msg_seq")
    @Expose
    private Integer msgSeq;
    @SerializedName("state")
    @Expose
    private Integer state;
    @SerializedName("battery")
    @Expose
    private Integer battery;
    @SerializedName("clean_time")
    @Expose
    private Long cleanTime;
    @SerializedName("clean_area")
    @Expose
    private Integer cleanArea;
    @SerializedName("error_code")
    @Expose
    private Integer errorCode;
    @SerializedName("map_present")
    @Expose
    private Integer mapPresent;
    @SerializedName("in_cleaning")
    @Expose
    private Integer inCleaning;
    @SerializedName("fan_power")
    @Expose
    private Integer fanPower;
    @SerializedName("dnd_enabled")
    @Expose
    private Integer dndEnabled;
    @SerializedName("in_returning")
    @Expose
    private Integer inReturning;
    @SerializedName("in_fresh_state")
    @Expose
    private Integer inFreshState;
    @SerializedName("lab_status")
    @Expose
    private Integer labStatus;
    @SerializedName("water_box_status")
    @Expose
    private Integer waterBoxStatus;
    @SerializedName("map_status")
    @Expose
    private Integer mapStatus;
    @SerializedName("is_locating")
    @Expose
    private Integer isLocating;
    @SerializedName("lock_status")
    @Expose
    private Integer lockStatus;
    @SerializedName("water_box_mode")
    @Expose
    private Integer waterBoxMode;
    @SerializedName("water_box_carriage_status")
    @Expose
    private Integer waterBoxCarriageStatus;
    @SerializedName("mop_forbidden_enable")
    @Expose
    private Integer mopForbiddenEnable;

    public final Integer getMsgVer() {
        return msgVer;
    }

    public final Integer getMsgSeq() {
        return msgSeq;
    }

    public final Integer getState() {
        return state;
    }

    public final Integer getBattery() {
        return battery;
    }

    public final Long getCleanTime() {
        return cleanTime;
    }

    public final Integer getCleanArea() {
        return cleanArea;
    }

    public final Integer getErrorCode() {
        return errorCode;
    }

    public final Integer getMapPresent() {
        return mapPresent;
    }

    public final Integer getInCleaning() {
        return inCleaning;
    }

    public final Integer getFanPower() {
        return fanPower;
    }

    public final Integer getDndEnabled() {
        return dndEnabled;
    }

    public final Integer getInReturning() {
        return inReturning;
    }

    public final Integer getInFreshState() {
        return inFreshState;
    }

    public final Integer getLabStatus() {
        return labStatus;
    }

    public final Integer getWaterBoxStatus() {
        return waterBoxStatus;
    }

    public final Integer getMapStatus() {
        return mapStatus;
    }

    public final Integer getIsLocating() {
        return isLocating;
    }

    public final Integer getLockStatus() {
        return lockStatus;
    }

    public final Integer getWaterBoxMode() {
        return waterBoxMode;
    }

    public final Integer getWaterBoxCarriageStatus() {
        return waterBoxCarriageStatus;
    }

    public final Integer getMopForbiddenEnable() {
        return mopForbiddenEnable;
    }
}
