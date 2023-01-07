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
package org.openhab.binding.vesync.internal.dto.requests;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncRequestManagedDeviceBypassV2} is a Java class used as a DTO to hold the Vesync's API's common
 * request data for V2 ByPass payloads.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncRequestManagedDeviceBypassV2 extends VeSyncAuthenticatedRequest {

    @SerializedName("deviceRegion")
    public String deviceRegion = "";

    @SerializedName("debugMode")
    public boolean debugMode = false;

    @SerializedName("cid")
    public String cid = "";

    @SerializedName("configModule")
    public String configModule = "";

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
    }

    public static class EmptyPayload {
    }

    public static class SetSwitchPayload extends EmptyPayload {

        public SetSwitchPayload(final boolean enabled, final int id) {
            this.enabled = enabled;
            this.id = id;
        }

        @SerializedName("enabled")
        public boolean enabled = true;

        @SerializedName("id")
        public int id = -1;
    }

    public static class EnabledPayload extends EmptyPayload {

        public EnabledPayload(final boolean enabled) {
            this.enabled = enabled;
        }

        @SerializedName("enabled")
        public boolean enabled = true;
    }

    public static class SetLevelPayload extends EmptyPayload {

        public SetLevelPayload(final int id, final String type, final int level) {
            this.id = id;
            this.type = type;
            this.level = level;
        }

        @SerializedName("id")
        public int id = -1;

        @SerializedName("level")
        public int level = -1;

        @SerializedName("type")
        public String type = "";
    }

    public static class SetState extends EmptyPayload {

        public SetState(final boolean state) {
            this.state = state;
        }

        @SerializedName("state")
        public boolean state = false;
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

    public VeSyncRequestManagedDeviceBypassV2() {
        super();
        method = "bypassV2";
    }
}
