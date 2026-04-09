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
package org.openhab.binding.vesync.internal.dto.requests.v2;

import org.openhab.binding.vesync.internal.dto.requests.login.AuthenticatedReq;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ManagedDeviceBypassReq} class is used as a DTO to hold the Vesync's API's common
 * request data for V2 ByPass payloads.
 *
 * @author David Goodyear - Initial contribution
 */
public class ManagedDeviceBypassReq extends AuthenticatedReq {

    @SerializedName("deviceRegion")
    public String deviceRegion = "";

    @SerializedName("debugMode")
    public boolean debugMode = false;

    @SerializedName("cid")
    public String cid = "";

    @SerializedName("configModule")
    public String configModule = "";

    @SerializedName("configModel")
    public String configModel = "";

    @SerializedName("payload")
    public VesyncManagedDeviceBase payload = new VesyncManagedDeviceBase();

    /**
     * Contains basic information about the device.
     */
    public class VesyncManagedDeviceBase {

        @SerializedName("method")
        public String method;

        @SerializedName("source")
        public String source = "APP";

        @SerializedName("data")
        public EmptyPayload data = new EmptyPayload();

        @SerializedName("subDeviceNo")
        public int subDeviceNo = 0;
    }

    public static class SetManualSpeedLevelPayload extends EmptyPayload {

        public SetManualSpeedLevelPayload(final int manualSpeedLevel) {
            this.manualSpeedLevel = manualSpeedLevel;
        }

        @SerializedName("levelIdx")
        public int levelIdx = 0;

        @SerializedName("levelType")
        public String levelType = "wind";

        @SerializedName("manualSpeedLevel")
        public int manualSpeedLevel = -1;
    }

    public static class SetWorkModePayload extends EmptyPayload {
        public SetWorkModePayload(final String workMode) {
            this.workMode = workMode;
        }

        @SerializedName("workMode")
        public String workMode = "";
    }

    public static class EnabledPayload extends EmptyPayload {

        public EnabledPayload(final boolean enabled) {
            this.enabled = enabled;
        }

        @SerializedName("enabled")
        public boolean enabled = true;
    }

    public static class SetNightLight extends EmptyPayload {

        public SetNightLight(final String state) {
            this.nightLight = state;
        }

        @SerializedName("night_light")
        public String nightLight = "";
    }

    public static class SetNightLightBrightness extends EmptyPayload {

        public SetNightLightBrightness(final int state) {
            this.nightLightLevel = state;
        }

        @SerializedName("night_light_brightness")
        public int nightLightLevel = 0;
    }

    public static class SetTargetHumidity extends EmptyPayload {

        public SetTargetHumidity(final int state) {
            this.targetHumidity = state;
        }

        @SerializedName("target_humidity")
        public int targetHumidity = 0;
    }

    public static class SetChildLock extends EmptyPayload {

        public SetChildLock(final boolean childLock) {
            this.childLock = childLock;
        }

        @SerializedName("child_lock")
        public boolean childLock = false;
    }

    public static class SetMode extends EmptyPayload {

        public SetMode(final String mode) {
            this.mode = mode;
        }

        @SerializedName("mode")
        public String mode = "";
    }

    public static class GetEnergyHistory extends EmptyPayload {

        public GetEnergyHistory(final long start, final long end) {
            this.start = start;
            this.end = end;
        }

        @SerializedName("fromDay")
        public long start = 0;

        @SerializedName("toDay")
        public long end = 0;
    }

    public ManagedDeviceBypassReq() {
        method = "bypassV2";
    }
}
