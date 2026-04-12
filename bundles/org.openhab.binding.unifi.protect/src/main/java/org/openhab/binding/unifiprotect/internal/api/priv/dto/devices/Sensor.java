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
 * Sensor device model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Sensor extends UniFiProtectAdoptableDevice {

    public BatteryStatus batteryStatus;
    public Instant leakDetectedAt;
    public Instant motionDetectedAt;
    public Instant openStatusChangedAt;
    public Instant alarmTriggeredAt;
    public Instant extremeValueDetectedAt;
    public Instant tamperingDetectedAt;
    public Boolean isOpened;
    public Boolean isMotionDetected;
    public String mountType;

    @SerializedName("tamperingSensitivityLevel")
    public String tamperingSensitivityLevel;

    public AlarmSettings alarmSettings;
    public HumiditySettings humiditySettings;
    public TemperatureSettings temperatureSettings;
    public LightSettings lightSettings;
    public LEDSettings ledSettings;
    public SensorStats stats;

    public static class BatteryStatus {
        public Integer percentage;
        public Boolean isLow;
    }

    public static class AlarmSettings {
        public Boolean isEnabled;
    }

    public static class HumiditySettings {
        public Integer margin;
        public Integer highThreshold;
        public Integer lowThreshold;
    }

    public static class TemperatureSettings {
        public Double margin;
        public Double highThreshold;
        public Double lowThreshold;
    }

    public static class LightSettings {
        public Double margin;
        public Double highThreshold;
        public Double lowThreshold;
    }

    public static class LEDSettings {
        public Boolean isEnabled;
        public Integer blinkRate;
    }

    public static class SensorStats {
        public SensorValue light;
        public SensorValue humidity;
        public SensorValue temperature;
    }

    public static class SensorValue {
        public Double value;
        public String status;
    }
}
