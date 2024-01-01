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
package org.openhab.binding.miio.internal;

import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping network properties from json for miio info response
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoInfoApDTO {
    @SerializedName("ssid")
    @Expose
    private String ssid;
    @SerializedName("bssid")
    @Expose
    private String bssid;
    @SerializedName("rssi")
    @Expose
    private JsonPrimitive rssi;
    @SerializedName("wifi_rssi")
    @Expose
    private Long wifiRssi;
    @SerializedName("freq")
    @Expose
    private Long freq;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public Long getRssi() {
        if (rssi != null && rssi.isNumber()) {
            return rssi.getAsLong();
        }
        return null;
    }

    public void setRssi(Long rssi) {
        this.rssi = new JsonPrimitive(rssi);
    }

    public Long getWifiRssi() {
        return wifiRssi;
    }

    public void setWifiRssi(Long wifiRssi) {
        this.wifiRssi = wifiRssi;
    }

    public Long getFreq() {
        return freq;
    }

    public void setFreq(Long freq) {
        this.freq = freq;
    }
}
