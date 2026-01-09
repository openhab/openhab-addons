/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.roborock.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used by the get_status response
 *
 * @author Paul Smedley - Initial Contribution
 *
 */

public class GetStatus {
    public int id;

    public Result[] result;

    public class Result {
        @SerializedName("msg_ver")
        public int msgVer;

        @SerializedName("msg_seq")
        public int msgSeq;

        public int state;

        public int battery;

        @SerializedName("clean_time")
        public int cleanTime;

        @SerializedName("clean_area")
        public int cleanArea;

        @SerializedName("error_code")
        public int errorCode;

        @SerializedName("map_present")
        public int mapPresent;

        @SerializedName("in_cleaning")
        public int inCleaning;

        @SerializedName("in_returning")
        public int inReturning;

        @SerializedName("in_fresh_state")
        public int inFreshState;

        @SerializedName("lab_status")
        public int labStatus;

        @SerializedName("water_box_status")
        public int waterBoxStatus;

        @SerializedName("back_type")
        public int backType;

        @SerializedName("wash_phase")
        public int washPhase;

        @SerializedName("wash_ready")
        public int washReady;

        @SerializedName("wash_status")
        public int washStatus;

        @SerializedName("fan_power")
        public int fanPower;

        @SerializedName("dnd_enabled")
        public int dndEnabled;

        @SerializedName("map_status")
        public int mapStatus;

        @SerializedName("is_locating")
        public int isLocating;

        @SerializedName("lock_status")
        public int lockStatus;

        @SerializedName("water_box_mode")
        public int waterBoxMode;

        @SerializedName("distance_off")
        public int distanceOff;

        @SerializedName("water_box_carriage_status")
        public int waterBoxCarriageStatus;

        @SerializedName("mop_forbidden_enable")
        public int mopForbiddenEnable;

        @SerializedName("camera_status")
        public int cameraStatus;

        @SerializedName("is_exploring")
        public int isExploring;

        @SerializedName("adbumper_status")
        public int[] adbumperStatus;

        @SerializedName("water_shortage_status")
        public int waterShortageStatus;

        @SerializedName("dock_type")
        public int dockType;

        @SerializedName("dust_collection_status")
        public int dustCollectionStatus;

        @SerializedName("auto_dust_collection")
        public int autoDustCollection;

        @SerializedName("avoid_count")
        public int avoidCount;

        @SerializedName("mop_mode")
        public int mopMode;

        @SerializedName("debug_mode")
        public int debugMode;

        @SerializedName("in_warmup")
        public int inWarmup;

        @SerializedName("collision_avoid_status")
        public int collisionAvoidStatus;

        @SerializedName("switch_map_mode")
        public int switchMapMode;

        @SerializedName("dock_error_status")
        public int dockErrorStatus;

        @SerializedName("charge_status")
        public int chargeStatus;

        @SerializedName("unsave_map_reason")
        public int unsaveMapReason;

        @SerializedName("unsave_map_flag")
        public int unsaveMapFlag;

        @SerializedName("dry_status")
        public int dryStatus;

        public int rdt;

        @SerializedName("clean_percent")
        public int cleanPercent;

        public int rss;

        public int dss;

        @SerializedName("common_status")
        public int commonStatus;

        @SerializedName("corner_clean_mode")
        public int cornerCleanMode;

        @SerializedName("replenish_mode")
        public int replenishMode;

        public int repeat;

        public int kct;

        @SerializedName("subdivision_sets")
        public int subdivisionSets;
    }

    public GetStatus() {
    }
}
