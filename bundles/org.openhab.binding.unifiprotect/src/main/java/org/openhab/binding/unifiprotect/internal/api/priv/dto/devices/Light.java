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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.devices;

import java.time.Duration;
import java.time.Instant;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectAdoptableDevice;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.types.LightModeType;

import com.google.gson.annotations.SerializedName;

/**
 * Light device model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Light extends UniFiProtectAdoptableDevice {

    public Boolean isPirMotionDetected;
    public Boolean isLightOn;
    public Boolean isLocating;
    public LightDeviceSettings lightDeviceSettings;
    public LightOnSettings lightOnSettings;
    public LightModeSettings lightModeSettings;

    @SerializedName("camera")
    public String cameraId;

    public Boolean isCameraPaired;
    public Instant lastMotion;
    public Boolean isDark;

    public static class LightDeviceSettings {
        public Boolean isIndicatorEnabled;
        public Integer ledLevel;
        public Long pirDuration; // milliseconds
        public Integer pirSensitivity;
        public String luxSensitivity;

        public Duration getPirDurationAsDuration() {
            return pirDuration != null ? Duration.ofMillis(pirDuration) : null;
        }

        public void setPirDurationFromDuration(Duration duration) {
            this.pirDuration = duration != null ? duration.toMillis() : null;
        }
    }

    public static class LightOnSettings {
        public Boolean isLedForceOn;
    }

    public static class LightModeSettings {
        public LightModeType mode;
        public String enableAt;
    }
}
