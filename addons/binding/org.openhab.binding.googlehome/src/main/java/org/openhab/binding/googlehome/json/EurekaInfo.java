/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.googlehome.json;

import com.google.gson.annotations.SerializedName;

/**
 * Contains all kinds of device information,
 * like build number, noise level or uptime.
 * API Endpoint is /setup/eureka_info
 *
 * @author Kuba Wolanin - Initial contribution
 */
public class EurekaInfo {
    private String bssid;
    @SerializedName("build_version")
    private String buildVersion;
    @SerializedName("cast_build_revision")
    private String castBuildRevision;
    private boolean connected;
    @SerializedName("ethernet_connected")
    private boolean ethernetConnected;
    private boolean hasUpdate;
    private String hotspotBssid;
    private String ipAddress;
    private String locale;
    private String macAddress;
    private String name;
    @SerializedName("noise_level")
    private int noiseLevel;
    private String publicKey;
    private String releaseTrack;
    private int setupState;
    @SerializedName("signal_level")
    private int signalLevel;
    private String ssdpUdn;
    private String ssid;
    private int timeFormat;
    private String timezone;
    private boolean tosAccepted;
    private double uptime;
    private int version;
    private boolean wpaConfigured;
    private int wpaID;
    private int wpaState;

    public EurekaInfo() {
    }

    public String getBssid() {
        return bssid;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getCastBuildRevision() {
        return castBuildRevision;
    }

    public boolean getConnected() {
        return connected;
    }

    public boolean getEthernetConnected() {
        return ethernetConnected;
    }

    public boolean getHasUpdate() {
        return hasUpdate;
    }

    public String getHotspotBssid() {
        return hotspotBssid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLocale() {
        return locale;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getName() {
        return name;
    }

    public int getNoiseLevel() {
        return noiseLevel;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getReleaseTrack() {
        return releaseTrack;
    }

    public int getSetupState() {
        return setupState;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public String getSsdpUdn() {
        return ssdpUdn;
    }

    public String getSsid() {
        return ssid;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public String getTimezone() {
        return timezone;
    }

    public boolean getTosAccepted() {
        return tosAccepted;
    }

    public double getUptime() {
        return uptime;
    }

    public int getVersion() {
        return version;
    }

    public boolean getWpaConfigured() {
        return wpaConfigured;
    }

    public int getWpaID() {
        return wpaID;
    }

    public int getWpaState() {
        return wpaState;
    }
}
