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
package org.openhab.binding.homewizard.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardSystemPayload {

    @SerializedName("wifi_ssid")
    private String wifiSsid = "";

    @SerializedName("wifi_rssi_db")
    private int wifiRssi = 0;

    @SerializedName("cloud_enabled")
    private Boolean cloudEnabled = false;

    @SerializedName("uptime_s")
    private int uptime = 0;

    @SerializedName("status_led_brightness_pct")
    private int ledBrightness = 0;

    /**
     * Getter for the wifi ssid
     *
     * @return wifiSsid
     */
    public String getWifiSsid() {
        return wifiSsid;
    }

    /**
     * Getter for the wifi rssi
     *
     * @return wifiRssi
     */
    public int getWifiRssi() {
        return wifiRssi;
    }

    /**
     * Getter for cloud enabled
     *
     * @return cloudEnabled
     */
    public Boolean isCloudEnabled() {
        return cloudEnabled;
    }

    /**
     * Getter for the uptime
     *
     * @return uptime
     */
    public int getUptime() {
        return uptime;
    }

    /**
     * Getter for the status led brightness
     *
     * @return ledBrightness
     */
    public int getStatusLedBrightness() {
        return ledBrightness;
    }

    @Override
    public String toString() {
        return String.format("""
                Data [wifiSsid: %s wifiRssi: %d cloudEnable: %B uptime: %d ledBrightness: %d]

                """, wifiSsid, wifiRssi, cloudEnabled, uptime, ledBrightness);
    }
}
