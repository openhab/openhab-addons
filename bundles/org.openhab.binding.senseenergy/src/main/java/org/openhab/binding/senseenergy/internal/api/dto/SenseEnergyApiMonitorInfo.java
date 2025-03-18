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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyMonitorInfo } is the dto for Monitor info inside the MonitorStatus dto class
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiMonitorInfo {
    public String emac;
    @SerializedName("wifi_strength")
    public int wifiStrength;
    @SerializedName("ip_address")
    public String ipAddress;
    public String connection;
    public String version;
    public String ssid;
    public String mac;
    public boolean ethernet;
    @SerializedName("test_result")
    public String testResult;
    public String serial;
    @SerializedName("ndt_enabled")
    public boolean ndtEnabled;
    public boolean online;
}
