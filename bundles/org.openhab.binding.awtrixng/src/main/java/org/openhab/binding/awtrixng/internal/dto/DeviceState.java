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
package org.openhab.binding.awtrixng.internal.dto;

import java.util.List;

/**
 * The device state JSON published retained on {@code <prefix>/state/device}. Optional members stay null when the
 * clock has no such hardware.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class DeviceState {
    public String version;
    public String uid;
    public String boardType;
    public String ipAddress;
    public String macAddress;
    public String hostname;
    public Integer wifiRssi;
    public Long uptimeSeconds;
    public Long freeHeapBytes;
    public Integer fps;
    public Integer brightness;
    public Boolean matrixPower;
    public List<Indicator> indicators;
    public Double lightLevel;
    public Integer batteryPercent;
    public Double batteryVoltage;
    public Boolean lowBattery;
    public Double temperature;
    public Double humidity;
    public Double pressureHpa;
}
