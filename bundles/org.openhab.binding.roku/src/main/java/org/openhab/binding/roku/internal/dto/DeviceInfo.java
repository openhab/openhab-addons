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
package org.openhab.binding.roku.internal.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps the XML response from the Roku HTTP endpoint '/query/device-info' (Device information)
 *
 * @author Michael Lobstein - Initial contribution
 */

@NonNullByDefault
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "device-info")
public class DeviceInfo {
    @XmlElement(name = "udn")
    private String udn = "";
    @XmlElement(name = "serial-number")
    private String serialNumber = "";
    @XmlElement(name = "device-id")
    private String deviceId = "";
    @XmlElement(name = "advertising-id")
    private String advertisingId = "";
    @XmlElement(name = "vendor-name")
    private String vendorName = "";
    @XmlElement(name = "model-name")
    private String modelName = "";
    @XmlElement(name = "model-number")
    private String modelNumber = "";
    @XmlElement(name = "model-region")
    private String modelRegion = "";
    @XmlElement(name = "is-tv")
    private boolean isTv = false;
    @XmlElement(name = "is-stick")
    private boolean isStick = false;
    @XmlElement(name = "ui-resolution")
    private String uiResolution = "";
    @XmlElement(name = "supports-ethernet")
    private boolean supportsEthernet = false;
    @XmlElement(name = "wifi-mac")
    private String wifiMac = "";
    @XmlElement(name = "wifi-driver")
    private String wifiDriver = "";
    @XmlElement(name = "has-wifi-extender")
    private boolean hasWifiExtender = false;
    @XmlElement(name = "has-wifi-5G-support")
    private boolean hasWifi5GSupport = false;
    @XmlElement(name = "can-use-wifi-extender")
    private boolean canUseWifiExtender = false;
    @XmlElement(name = "ethernet-mac")
    private String ethernetMac = "";
    @XmlElement(name = "network-type")
    private String networkType = "";
    @XmlElement(name = "friendly-device-name")
    private String friendlyDeviceName = "";
    @XmlElement(name = "friendly-model-name")
    private String friendlyModelName = "";
    @XmlElement(name = "default-device-name")
    private String defaultDeviceName = "";
    @XmlElement(name = "user-device-name")
    private String userDeviceName = "";
    @XmlElement(name = "user-device-location")
    private String userDeviceLocation = "";
    @XmlElement(name = "build-number")
    private String buildNumber = "";
    @XmlElement(name = "software-version")
    private String softwareVersion = "";
    @XmlElement(name = "software-build")
    private String softwareBuild = "";
    @XmlElement(name = "secure-device")
    private boolean secureDevice = false;
    @XmlElement(name = "language")
    private String language = "";
    @XmlElement(name = "country")
    private String country = "";
    @XmlElement(name = "locale")
    private String locale = "";
    @XmlElement(name = "time-zone-auto")
    private boolean timeZoneAuto = false;
    @XmlElement(name = "time-zone")
    private String timeZone = "";
    @XmlElement(name = "time-zone-name")
    private String timeZoneName = "";
    @XmlElement(name = "time-zone-tz")
    private String timeZoneTz = "";
    @XmlElement(name = "time-zone-offset")
    private int timeZoneOffset = 0;
    @XmlElement(name = "clock-format")
    private String clockFormat = "";
    @XmlElement(name = "uptime")
    private int uptime = 0;
    @XmlElement(name = "power-mode")
    private String powerMode = "";
    @XmlElement(name = "supports-suspend")
    private boolean supportsSuspend = false;
    @XmlElement(name = "supports-find-remote")
    private boolean supportsFindRemote = false;
    @XmlElement(name = "find-remote-is-possible")
    private boolean findRemoteIsPossible = false;
    @XmlElement(name = "supports-audio-guide")
    private boolean supportsAudioGuide = false;
    @XmlElement(name = "supports-rva")
    private boolean supportsRva = false;
    @XmlElement(name = "developer-enabled")
    private boolean developerEnabled = false;
    @XmlElement(name = "keyed-developer-id")
    private String keyedDeveloperId = "";
    @XmlElement(name = "search-enabled")
    private boolean searchEnabled = false;
    @XmlElement(name = "search-channels-enabled")
    private boolean searchChannelsEnabled = false;
    @XmlElement(name = "voice-search-enabled")
    private boolean voiceSearchEnabled = false;
    @XmlElement(name = "notifications-enabled")
    private boolean notificationsEnabled = false;
    @XmlElement(name = "notifications-first-use")
    private boolean notificationsFirstUse = false;
    @XmlElement(name = "supports-private-listening")
    private boolean supportsPrivateListening = false;
    @XmlElement(name = "headphones-connected")
    private boolean headphonesConnected = false;
    @XmlElement(name = "supports-ecs-textedit")
    private boolean supportsEcsTextedit = false;
    @XmlElement(name = "supports-ecs-microphone")
    private boolean supportsEcsMicrophone = false;
    @XmlElement(name = "supports-wake-on-wlan")
    private boolean supportsWakeOnWlan = false;
    @XmlElement(name = "has-play-on-roku")
    private boolean hasPlayOnRoku = false;
    @XmlElement(name = "has-mobile-screensaver")
    private boolean hasMobileScreensaver = false;
    @XmlElement(name = "support-url")
    private String supportUrl = "";
    @XmlElement(name = "grandcentral-version")
    private String grandcentralVersion = "";
    @XmlElement(name = "trc-version")
    private String trcVersion = "";
    @XmlElement(name = "trc-channel-version")
    private String trcChannelVersion = "";
    @XmlElement(name = "davinci-version")
    private String davinciVersion = "";

    public String getUdn() {
        return udn;
    }

    public void setUdn(String value) {
        this.udn = value;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String value) {
        this.serialNumber = value;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String value) {
        this.deviceId = value;
    }

    public String getAdvertisingId() {
        return advertisingId;
    }

    public void setAdvertisingId(String value) {
        this.advertisingId = value;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String value) {
        this.vendorName = value;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String value) {
        this.modelName = value;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String value) {
        this.modelNumber = value;
    }

    public String getModelRegion() {
        return modelRegion;
    }

    public void setModelRegion(String value) {
        this.modelRegion = value;
    }

    public boolean isTv() {
        return isTv;
    }

    public void setIsTv(boolean value) {
        this.isTv = value;
    }

    public boolean isStick() {
        return isStick;
    }

    public void setIsStick(boolean value) {
        this.isStick = value;
    }

    public String getUiResolution() {
        return uiResolution;
    }

    public void setUiResolution(String value) {
        this.uiResolution = value;
    }

    public boolean isSupportsEthernet() {
        return supportsEthernet;
    }

    public void setSupportsEthernet(boolean value) {
        this.supportsEthernet = value;
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public void setWifiMac(String value) {
        this.wifiMac = value;
    }

    public String getWifiDriver() {
        return wifiDriver;
    }

    public void setWifiDriver(String value) {
        this.wifiDriver = value;
    }

    public boolean isHasWifiExtender() {
        return hasWifiExtender;
    }

    public void setHasWifiExtender(boolean value) {
        this.hasWifiExtender = value;
    }

    public boolean isHasWifi5GSupport() {
        return hasWifi5GSupport;
    }

    public void setHasWifi5GSupport(boolean value) {
        this.hasWifi5GSupport = value;
    }

    public boolean isCanUseWifiExtender() {
        return canUseWifiExtender;
    }

    public void setCanUseWifiExtender(boolean value) {
        this.canUseWifiExtender = value;
    }

    public String getEthernetMac() {
        return ethernetMac;
    }

    public void setEthernetMac(String value) {
        this.ethernetMac = value;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String value) {
        this.networkType = value;
    }

    public String getFriendlyDeviceName() {
        return friendlyDeviceName;
    }

    public void setFriendlyDeviceName(String value) {
        this.friendlyDeviceName = value;
    }

    public String getFriendlyModelName() {
        return friendlyModelName;
    }

    public void setFriendlyModelName(String value) {
        this.friendlyModelName = value;
    }

    public String getDefaultDeviceName() {
        return defaultDeviceName;
    }

    public void setDefaultDeviceName(String value) {
        this.defaultDeviceName = value;
    }

    public String getUserDeviceName() {
        return userDeviceName;
    }

    public void setUserDeviceName(String value) {
        this.userDeviceName = value;
    }

    public String getUserDeviceLocation() {
        return userDeviceLocation;
    }

    public void setUserDeviceLocation(String value) {
        this.userDeviceLocation = value;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String value) {
        this.buildNumber = value;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String value) {
        this.softwareVersion = value;
    }

    public String getSoftwareBuild() {
        return softwareBuild;
    }

    public void setSoftwareBuild(String value) {
        this.softwareBuild = value;
    }

    public boolean isSecureDevice() {
        return secureDevice;
    }

    public void setSecureDevice(boolean value) {
        this.secureDevice = value;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String value) {
        this.language = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        this.country = value;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String value) {
        this.locale = value;
    }

    public boolean isTimeZoneAuto() {
        return timeZoneAuto;
    }

    public void setTimeZoneAuto(boolean value) {
        this.timeZoneAuto = value;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String value) {
        this.timeZone = value;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String value) {
        this.timeZoneName = value;
    }

    public String getTimeZoneTz() {
        return timeZoneTz;
    }

    public void setTimeZoneTz(String value) {
        this.timeZoneTz = value;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(int value) {
        this.timeZoneOffset = value;
    }

    public String getClockFormat() {
        return clockFormat;
    }

    public void setClockFormat(String value) {
        this.clockFormat = value;
    }

    public int getUptime() {
        return uptime;
    }

    public void setUptime(int value) {
        this.uptime = value;
    }

    public String getPowerMode() {
        return powerMode;
    }

    public void setPowerMode(String value) {
        this.powerMode = value;
    }

    public boolean isSupportsSuspend() {
        return supportsSuspend;
    }

    public void setSupportsSuspend(boolean value) {
        this.supportsSuspend = value;
    }

    public boolean isSupportsFindRemote() {
        return supportsFindRemote;
    }

    public void setSupportsFindRemote(boolean value) {
        this.supportsFindRemote = value;
    }

    public boolean isFindRemoteIsPossible() {
        return findRemoteIsPossible;
    }

    public void setFindRemoteIsPossible(boolean value) {
        this.findRemoteIsPossible = value;
    }

    public boolean isSupportsAudioGuide() {
        return supportsAudioGuide;
    }

    public void setSupportsAudioGuide(boolean value) {
        this.supportsAudioGuide = value;
    }

    public boolean isSupportsRva() {
        return supportsRva;
    }

    public void setSupportsRva(boolean value) {
        this.supportsRva = value;
    }

    public boolean isDeveloperEnabled() {
        return developerEnabled;
    }

    public void setDeveloperEnabled(boolean value) {
        this.developerEnabled = value;
    }

    public String getKeyedDeveloperId() {
        return keyedDeveloperId;
    }

    public void setKeyedDeveloperId(String value) {
        this.keyedDeveloperId = value;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean value) {
        this.searchEnabled = value;
    }

    public boolean isSearchChannelsEnabled() {
        return searchChannelsEnabled;
    }

    public void setSearchChannelsEnabled(boolean value) {
        this.searchChannelsEnabled = value;
    }

    public boolean isVoiceSearchEnabled() {
        return voiceSearchEnabled;
    }

    public void setVoiceSearchEnabled(boolean value) {
        this.voiceSearchEnabled = value;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean value) {
        this.notificationsEnabled = value;
    }

    public boolean isNotificationsFirstUse() {
        return notificationsFirstUse;
    }

    public void setNotificationsFirstUse(boolean value) {
        this.notificationsFirstUse = value;
    }

    public boolean isSupportsPrivateListening() {
        return supportsPrivateListening;
    }

    public void setSupportsPrivateListening(boolean value) {
        this.supportsPrivateListening = value;
    }

    public boolean isHeadphonesConnected() {
        return headphonesConnected;
    }

    public void setHeadphonesConnected(boolean value) {
        this.headphonesConnected = value;
    }

    public boolean isSupportsEcsTextedit() {
        return supportsEcsTextedit;
    }

    public void setSupportsEcsTextedit(boolean value) {
        this.supportsEcsTextedit = value;
    }

    public boolean isSupportsEcsMicrophone() {
        return supportsEcsMicrophone;
    }

    public void setSupportsEcsMicrophone(boolean value) {
        this.supportsEcsMicrophone = value;
    }

    public boolean isSupportsWakeOnWlan() {
        return supportsWakeOnWlan;
    }

    public void setSupportsWakeOnWlan(boolean value) {
        this.supportsWakeOnWlan = value;
    }

    public boolean isHasPlayOnRoku() {
        return hasPlayOnRoku;
    }

    public void setHasPlayOnRoku(boolean value) {
        this.hasPlayOnRoku = value;
    }

    public boolean isHasMobileScreensaver() {
        return hasMobileScreensaver;
    }

    public void setHasMobileScreensaver(boolean value) {
        this.hasMobileScreensaver = value;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public void setSupportUrl(String value) {
        this.supportUrl = value;
    }

    public String getGrandcentralVersion() {
        return grandcentralVersion;
    }

    public void setGrandcentralVersion(String value) {
        this.grandcentralVersion = value;
    }

    public String getTrcVersion() {
        return trcVersion;
    }

    public void setTrcVersion(String value) {
        this.trcVersion = value;
    }

    public String getTrcChannelVersion() {
        return trcChannelVersion;
    }

    public void setTrcChannelVersion(String value) {
        this.trcChannelVersion = value;
    }

    public String getDavinciVersion() {
        return davinciVersion;
    }

    public void setDavinciVersion(String value) {
        this.davinciVersion = value;
    }
}
