/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.iCloud;

public class ServerContext {
    private int minCallbackIntervalInMS;

    public int getMinCallbackIntervalInMS() {
        return this.minCallbackIntervalInMS;
    }

    public void setMinCallbackIntervalInMS(int minCallbackIntervalInMS) {
        this.minCallbackIntervalInMS = minCallbackIntervalInMS;
    }

    private boolean enable2FAFamilyActions;

    public boolean getEnable2FAFamilyActions() {
        return this.enable2FAFamilyActions;
    }

    public void setEnable2FAFamilyActions(boolean enable2FAFamilyActions) {
        this.enable2FAFamilyActions = enable2FAFamilyActions;
    }

    private String preferredLanguage;

    public String getPreferredLanguage() {
        return this.preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    private Object lastSessionExtensionTime;

    public Object getLastSessionExtensionTime() {
        return this.lastSessionExtensionTime;
    }

    public void setLastSessionExtensionTime(Object lastSessionExtensionTime) {
        this.lastSessionExtensionTime = lastSessionExtensionTime;
    }

    private boolean enableMapStats;

    public boolean getEnableMapStats() {
        return this.enableMapStats;
    }

    public void setEnableMapStats(boolean enableMapStats) {
        this.enableMapStats = enableMapStats;
    }

    private int callbackIntervalInMS;

    public int getCallbackIntervalInMS() {
        return this.callbackIntervalInMS;
    }

    public void setCallbackIntervalInMS(int callbackIntervalInMS) {
        this.callbackIntervalInMS = callbackIntervalInMS;
    }

    private boolean validRegion;

    public boolean getValidRegion() {
        return this.validRegion;
    }

    public void setValidRegion(boolean validRegion) {
        this.validRegion = validRegion;
    }

    private Timezone timezone;

    public Timezone getTimezone() {
        return this.timezone;
    }

    public void setTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    private String authToken;

    public String getAuthToken() {
        return this.authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private int maxCallbackIntervalInMS;

    public int getMaxCallbackIntervalInMS() {
        return this.maxCallbackIntervalInMS;
    }

    public void setMaxCallbackIntervalInMS(int maxCallbackIntervalInMS) {
        this.maxCallbackIntervalInMS = maxCallbackIntervalInMS;
    }

    private boolean classicUser;

    public boolean getClassicUser() {
        return this.classicUser;
    }

    public void setClassicUser(boolean classicUser) {
        this.classicUser = classicUser;
    }

    private boolean isHSA;

    public boolean getIsHSA() {
        return this.isHSA;
    }

    public void setIsHSA(boolean isHSA) {
        this.isHSA = isHSA;
    }

    private int trackInfoCacheDurationInSecs;

    public int getTrackInfoCacheDurationInSecs() {
        return this.trackInfoCacheDurationInSecs;
    }

    public void setTrackInfoCacheDurationInSecs(int trackInfoCacheDurationInSecs) {
        this.trackInfoCacheDurationInSecs = trackInfoCacheDurationInSecs;
    }

    private String imageBaseUrl;

    public String getImageBaseUrl() {
        return this.imageBaseUrl;
    }

    public void setImageBaseUrl(String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }

    private int minTrackLocThresholdInMts;

    public int getMinTrackLocThresholdInMts() {
        return this.minTrackLocThresholdInMts;
    }

    public void setMinTrackLocThresholdInMts(int minTrackLocThresholdInMts) {
        this.minTrackLocThresholdInMts = minTrackLocThresholdInMts;
    }

    private int maxLocatingTime;

    public int getMaxLocatingTime() {
        return this.maxLocatingTime;
    }

    public void setMaxLocatingTime(int maxLocatingTime) {
        this.maxLocatingTime = maxLocatingTime;
    }

    private int sessionLifespan;

    public int getSessionLifespan() {
        return this.sessionLifespan;
    }

    public void setSessionLifespan(int sessionLifespan) {
        this.sessionLifespan = sessionLifespan;
    }

    private String info;

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    private int prefsUpdateTime;

    public int getPrefsUpdateTime() {
        return this.prefsUpdateTime;
    }

    public void setPrefsUpdateTime(int prefsUpdateTime) {
        this.prefsUpdateTime = prefsUpdateTime;
    }

    private boolean useAuthWidget;

    public boolean getUseAuthWidget() {
        return this.useAuthWidget;
    }

    public void setUseAuthWidget(boolean useAuthWidget) {
        this.useAuthWidget = useAuthWidget;
    }

    private String clientId;

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private boolean enable2FAFamilyRemove;

    public boolean getEnable2FAFamilyRemove() {
        return this.enable2FAFamilyRemove;
    }

    public void setEnable2FAFamilyRemove(boolean enable2FAFamilyRemove) {
        this.enable2FAFamilyRemove = enable2FAFamilyRemove;
    }

    private long serverTimestamp;

    public long getServerTimestamp() {
        return this.serverTimestamp;
    }

    public void setServerTimestamp(long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    private int macCount;

    public int getMacCount() {
        return this.macCount;
    }

    public void setMacCount(int macCount) {
        this.macCount = macCount;
    }

    private String deviceLoadStatus;

    public String getDeviceLoadStatus() {
        return this.deviceLoadStatus;
    }

    public void setDeviceLoadStatus(String deviceLoadStatus) {
        this.deviceLoadStatus = deviceLoadStatus;
    }

    private int maxDeviceLoadTime;

    public int getMaxDeviceLoadTime() {
        return this.maxDeviceLoadTime;
    }

    public void setMaxDeviceLoadTime(int maxDeviceLoadTime) {
        this.maxDeviceLoadTime = maxDeviceLoadTime;
    }

    private String prsId;

    public String getPrsId() {
        return this.prsId;
    }

    public void setPrsId(String prsId) {
        this.prsId = prsId;
    }

    private boolean showSllNow;

    public boolean getShowSllNow() {
        return this.showSllNow;
    }

    public void setShowSllNow(boolean showSllNow) {
        this.showSllNow = showSllNow;
    }

    private boolean cloudUser;

    public boolean getCloudUser() {
        return this.cloudUser;
    }

    public void setCloudUser(boolean cloudUser) {
        this.cloudUser = cloudUser;
    }

    private boolean enable2FAErase;

    public boolean getEnable2FAErase() {
        return this.enable2FAErase;
    }

    public void setEnable2FAErase(boolean enable2FAErase) {
        this.enable2FAErase = enable2FAErase;
    }
}
