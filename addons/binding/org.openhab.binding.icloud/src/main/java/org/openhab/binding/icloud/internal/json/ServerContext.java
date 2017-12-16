/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json;

/**
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ServerContext {
    private String authToken;

    private int callbackIntervalInMS;

    private boolean classicUser;

    private String clientId;

    private boolean cloudUser;

    private String deviceLoadStatus;

    private boolean enable2FAErase;

    private boolean enable2FAFamilyActions;

    private boolean enable2FAFamilyRemove;

    private boolean enableMapStats;

    private String imageBaseUrl;

    private String info;

    private boolean isHSA;

    private Object lastSessionExtensionTime;

    private int macCount;

    private int maxCallbackIntervalInMS;

    private int maxDeviceLoadTime;

    private int maxLocatingTime;

    private int minCallbackIntervalInMS;

    private int minTrackLocThresholdInMts;

    private String preferredLanguage;

    private long prefsUpdateTime;

    private String prsId;

    private long serverTimestamp;

    private int sessionLifespan;

    private boolean showSllNow;

    private Timezone timezone;

    private int trackInfoCacheDurationInSecs;

    private boolean useAuthWidget;

    private boolean validRegion;

    public String getAuthToken() {
        return this.authToken;
    }

    public int getCallbackIntervalInMS() {
        return this.callbackIntervalInMS;
    }

    public boolean getClassicUser() {
        return this.classicUser;
    }

    public String getClientId() {
        return this.clientId;
    }

    public boolean getCloudUser() {
        return this.cloudUser;
    }

    public String getDeviceLoadStatus() {
        return this.deviceLoadStatus;
    }

    public boolean getEnable2FAErase() {
        return this.enable2FAErase;
    }

    public boolean getEnable2FAFamilyActions() {
        return this.enable2FAFamilyActions;
    }

    public boolean getEnable2FAFamilyRemove() {
        return this.enable2FAFamilyRemove;
    }

    public boolean getEnableMapStats() {
        return this.enableMapStats;
    }

    public String getImageBaseUrl() {
        return this.imageBaseUrl;
    }

    public String getInfo() {
        return this.info;
    }

    public boolean getIsHSA() {
        return this.isHSA;
    }

    public Object getLastSessionExtensionTime() {
        return this.lastSessionExtensionTime;
    }

    public int getMacCount() {
        return this.macCount;
    }

    public int getMaxCallbackIntervalInMS() {
        return this.maxCallbackIntervalInMS;
    }

    public int getMaxDeviceLoadTime() {
        return this.maxDeviceLoadTime;
    }

    public int getMaxLocatingTime() {
        return this.maxLocatingTime;
    }

    public int getMinCallbackIntervalInMS() {
        return this.minCallbackIntervalInMS;
    }

    public int getMinTrackLocThresholdInMts() {
        return this.minTrackLocThresholdInMts;
    }

    public String getPreferredLanguage() {
        return this.preferredLanguage;
    }

    public long getPrefsUpdateTime() {
        return this.prefsUpdateTime;
    }

    public String getPrsId() {
        return this.prsId;
    }

    public long getServerTimestamp() {
        return this.serverTimestamp;
    }

    public int getSessionLifespan() {
        return this.sessionLifespan;
    }

    public boolean getShowSllNow() {
        return this.showSllNow;
    }

    public Timezone getTimezone() {
        return this.timezone;
    }

    public int getTrackInfoCacheDurationInSecs() {
        return this.trackInfoCacheDurationInSecs;
    }

    public boolean getUseAuthWidget() {
        return this.useAuthWidget;
    }

    public boolean getValidRegion() {
        return this.validRegion;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setCallbackIntervalInMS(int callbackIntervalInMS) {
        this.callbackIntervalInMS = callbackIntervalInMS;
    }

    public void setClassicUser(boolean classicUser) {
        this.classicUser = classicUser;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setCloudUser(boolean cloudUser) {
        this.cloudUser = cloudUser;
    }

    public void setDeviceLoadStatus(String deviceLoadStatus) {
        this.deviceLoadStatus = deviceLoadStatus;
    }

    public void setEnable2FAErase(boolean enable2FAErase) {
        this.enable2FAErase = enable2FAErase;
    }

    public void setEnable2FAFamilyActions(boolean enable2FAFamilyActions) {
        this.enable2FAFamilyActions = enable2FAFamilyActions;
    }

    public void setEnable2FAFamilyRemove(boolean enable2FAFamilyRemove) {
        this.enable2FAFamilyRemove = enable2FAFamilyRemove;
    }

    public void setEnableMapStats(boolean enableMapStats) {
        this.enableMapStats = enableMapStats;
    }

    public void setImageBaseUrl(String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setIsHSA(boolean isHSA) {
        this.isHSA = isHSA;
    }

    public void setLastSessionExtensionTime(Object lastSessionExtensionTime) {
        this.lastSessionExtensionTime = lastSessionExtensionTime;
    }

    public void setMacCount(int macCount) {
        this.macCount = macCount;
    }

    public void setMaxCallbackIntervalInMS(int maxCallbackIntervalInMS) {
        this.maxCallbackIntervalInMS = maxCallbackIntervalInMS;
    }

    public void setMaxDeviceLoadTime(int maxDeviceLoadTime) {
        this.maxDeviceLoadTime = maxDeviceLoadTime;
    }

    public void setMaxLocatingTime(int maxLocatingTime) {
        this.maxLocatingTime = maxLocatingTime;
    }

    public void setMinCallbackIntervalInMS(int minCallbackIntervalInMS) {
        this.minCallbackIntervalInMS = minCallbackIntervalInMS;
    }

    public void setMinTrackLocThresholdInMts(int minTrackLocThresholdInMts) {
        this.minTrackLocThresholdInMts = minTrackLocThresholdInMts;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setPrefsUpdateTime(long prefsUpdateTime) {
        this.prefsUpdateTime = prefsUpdateTime;
    }

    public void setPrsId(String prsId) {
        this.prsId = prsId;
    }

    public void setServerTimestamp(long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public void setSessionLifespan(int sessionLifespan) {
        this.sessionLifespan = sessionLifespan;
    }

    public void setShowSllNow(boolean showSllNow) {
        this.showSllNow = showSllNow;
    }

    public void setTimezone(Timezone timezone) {
        this.timezone = timezone;
    }

    public void setTrackInfoCacheDurationInSecs(int trackInfoCacheDurationInSecs) {
        this.trackInfoCacheDurationInSecs = trackInfoCacheDurationInSecs;
    }

    public void setUseAuthWidget(boolean useAuthWidget) {
        this.useAuthWidget = useAuthWidget;
    }

    public void setValidRegion(boolean validRegion) {
        this.validRegion = validRegion;
    }
}
