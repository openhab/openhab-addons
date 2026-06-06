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

import java.time.Instant;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectAdoptableDevice;

import com.google.gson.annotations.SerializedName;

/**
 * Doorlock device model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Doorlock extends UniFiProtectAdoptableDevice {

    public BatteryStatus batteryStatus;

    @SerializedName("camera")
    public String cameraId;

    public String lockStatus;
    public LEDSettings ledSettings;
    public Instant lastLockStatusChangeTime;
    public Integer autoCloseTime;

    public static class BatteryStatus {
        public Integer percentage;
        public Boolean isLow;
    }

    public static class LEDSettings {
        public Boolean isEnabled;
        public Integer blinkRate;
    }
}
