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
package org.openhab.binding.unifiprotect.internal.dto;

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
    public Long openStatusChangedAt; // nullable
    public Boolean isMotionDetected;
    public Long motionDetectedAt; // nullable
    public MotionSettings motionSettings;

    public Long alarmTriggeredAt; // nullable
    public AlarmSettings alarmSettings;
    public Long leakDetectedAt; // nullable
    public Long externalLeakDetectedAt; // nullable
    public LeakSettings leakSettings;
    public Long tamperingDetectedAt; // nullable
}
