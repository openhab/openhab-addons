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
 * Model representing the payload returned by the /getStatusEx endpoint.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceStatus {

    private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

    public String language;
    public String ssid;
    @SerializedName("hideSSID")
    public String hideSsid;
    @SerializedName("SSIDStrategy")
    public String ssidStrategy;
    public String branch;
    public String firmware;
    public String build;
    public String project;
    public String privPrj;
    public String projectBuildName;
    @SerializedName("Release")
    public String release;
    @SerializedName("FW_Release_version")
    public String fwReleaseVersion;
    @SerializedName("PCB_version")
    public String pcbVersion;
    public Integer castEnable;
    public Integer castUsageReport;
    public String group;
    public String masterUuid;
    public String slaveMask;
    public String wmrmVersion;
    public String wmrmSubVer;
    public String expired;
    public String internet;
    public String uuid;
    @SerializedName("MAC")
    public String mac;
    @SerializedName("STA_MAC")
    public String staMac;
    @SerializedName("BTMAC")
    public String btMac;
    @SerializedName("AP_MAC")
    public String apMac;
    @SerializedName("ETH_MAC")
    public String ethMac;
    @SerializedName("InitialConfiguration")
    public String initialConfiguration;
    public String temperaturePowerControl;
    public String temperatureCpu;
    public String temperatureTmp102;
    @SerializedName("CountryCode")
    public String countryCode;
    @SerializedName("CountryRegion")
    public String countryRegion;
    public String date;
    public String time;
    public String tz;
    public String dstEnable;
    public String netstat;
    public String essid;
    public String apcli0;
    public String eth0;
    public String eth2;
    public String ethDhcp;
    public String ethStaticIp;
    public String ethStaticMask;
    public String ethStaticGateway;
    public String ethStaticDns1;
    public String ethStaticDns2;
    public String hardware;
    @SerializedName("VersionUpdate")
    public String versionUpdate;
    @SerializedName("NewVer")
    public String newVer;
    public String mcuVer;
    public String mcuVerNew;
    public String hdmiVer;
    public String hdmiVerNew;
    public String updateCheckCount;
    @SerializedName("BleRemote_update_checked_counter")
    public String bleRemoteUpdateCheckedCounter;
    public String ra0;
    public String tempUuid;
    public String cap1;
    public String capability;
    public String languages;
    public String promptStatus;
    public String iotVer;
    public String alexaVer;
    public String alexaBetaEnable;
    public String alexaForceBetaCfg;
    public String dspVer;
    public String dspVerNew;
    @SerializedName("ModuleColorNumber")
    public String moduleColorNumber;
    @SerializedName("ModuleColorString")
    public String moduleColorString;
    public String ubootVerinfo;
    public String streamsAll;
    public String streams;
    public String region;
    public String volumeControl;
    public String external;
    public String presetKey;
    public String spotifyActive;
    public String plmSupport;
    public String mqttSupport;
    public String lbcSupport;
    @SerializedName("WifiChannel")
    public String wifiChannel;
    @SerializedName("RSSI")
    public String rssi;
    @SerializedName("BSSID")
    public String bssid;
    @SerializedName("wlanSnr")
    public String wlanSnr;
    @SerializedName("wlanNoise")
    public String wlanNoise;
    @SerializedName("wlanFreq")
    public String wlanFreq;
    @SerializedName("wlanDataRate")
    public String wlanDataRate;
    public String battery;
    public String batteryPercent;
    @SerializedName("securemode")
    public String secureMode;
    public String auth;
    public String encry;
    public String otaInterfaceVer;
    public String otaApiVer;
    public String upnpVersion;
    public String upnpUuid;
    public String uartPassPort;
    public String communicationPort;
    public String webFirmwareUpdateHide;
    public String ignoreTalkstart;
    public String silenceOtaFlag;
    @SerializedName("silenceOTATime")
    public String silenceOtaTime;
    @SerializedName("ignore_silenceOTATime")
    public String ignoreSilenceOtaTime;
    public String newTuneinPresetAndAlarm;
    @SerializedName("iheartradio_new")
    public String iHeartRadioNew;
    public String newIheartPodcast;
    public String tidalVersion;
    public String serviceVersion;
    @SerializedName("EQ_support")
    public String eqSupport;
    @SerializedName("EQVersion")
    public String eqVersion;
    @SerializedName("HiFiSRC_version")
    public String hiFiSrcVersion;
    public String audioChannelConfig;
    public String appTimezoneId;
    public String avsTimezoneId;
    public String tzInfoVer;
    public String maxVolume;
    public String powerMode;
    public String security;
    public String securityVersion;
    public SecurityCapabilities securityCapabilities;
    public String publicHttpsVersion;
    @SerializedName("BleRemoteControl")
    public String bleRemoteControl;
    @SerializedName("BleRemoteConnected")
    public String bleRemoteConnected;
    @SerializedName("BleRemoteException")
    public String bleRemoteException;
    public String udisk;
    public String umount;
    @SerializedName("autoSenseVersion")
    public String autoSenseVersion;
    public String setPlayModeEnable;
    public String setPlayModeGain;
    @SerializedName("audioOutputModeVer")
    public String audioOutputModeVer;
    public String privacyMode;
    @SerializedName("DeviceName")
    public String deviceName;
    @SerializedName("GroupName")
    public String groupName;

    public static class SecurityCapabilities {
        public String ver;
        public String aesVer;

        @Override
        public String toString() {
            return "SecurityCapabilities" + GSON.toJson(this);
        }
    }

    @Override
    public String toString() {
        return "DeviceStatus" + GSON.toJson(this);
    }
}
