/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

/* @formatter:off
"monitor_info": {
    "emac":"f2:ed:5e:b1:96:8e",
    "wifi_strength":86,
    "ip_address":"192.168.1.36",
    "connection_state":"ONLINE",
    "version":"1.49.29-4a5ddd65-release",
    "ssid":"jameswireless_EXT",
    "mac":"24:cd:8d:3d:dd:79",
    "ethernet":false,
    "test_result":"Good as of 04/01/2024",
    "serial":"N327004101",
    "ndt_enabled":false,
    "online":true,
    "signal":null
}
@formatter:on
*/
