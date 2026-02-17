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
package org.openhab.binding.linkplay.internal.client.http.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Static IP information.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class StaticIpInfo {
    @SerializedName("wlanStaticIp")
    public String wlanStaticIp;
    @SerializedName("wlanGateWay")
    public String wlanGateWay;
    @SerializedName("wlanDnsServer")
    public String wlanDnsServer;
    @SerializedName("wlanStaticIpEnable")
    public Integer wlanStaticIpEnable;
    @SerializedName("ethStaticIp")
    public String ethStaticIp;
    @SerializedName("ethGateWay")
    public String ethGateWay;
    @SerializedName("ethDnsServer")
    public String ethDnsServer;
    @SerializedName("ethStaticIpEnable")
    public Integer ethStaticIpEnable;

    @Override
    public String toString() {
        return "StaticIpInfo{" + "wlanStaticIp='" + wlanStaticIp + '\'' + ", wlanGateWay='" + wlanGateWay + '\''
                + ", wlanDnsServer='" + wlanDnsServer + '\'' + ", wlanStaticIpEnable=" + wlanStaticIpEnable
                + ", ethStaticIp='" + ethStaticIp + '\'' + ", ethGateWay='" + ethGateWay + '\'' + ", ethDnsServer='"
                + ethDnsServer + '\'' + ", ethStaticIpEnable=" + ethStaticIpEnable + '}';
    }
}
