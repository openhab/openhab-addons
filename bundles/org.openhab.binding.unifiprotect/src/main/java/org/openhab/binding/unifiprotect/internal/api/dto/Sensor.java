/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Sensor device DTO.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Sensor extends Device {
    public SensorMountType mountType;
    public BatteryStatus batteryStatus;
    public SensorStats stats;
    public LightSettings lightSettings;
    public HumiditySettings humiditySettings;
    public TemperatureSettings temperatureSettings;

    public Boolean isOpened;
    public @Nullable Long openStatusChangedAt;
    public Boolean isMotionDetected;
    public @Nullable Long motionDetectedAt;
    public MotionSettings motionSettings;

    public @Nullable Long alarmTriggeredAt;
    public AlarmSettings alarmSettings;
    public @Nullable Long leakDetectedAt;
    public @Nullable Long externalLeakDetectedAt;
    public LeakSettings leakSettings;
    public @Nullable Long tamperingDetectedAt;
}
