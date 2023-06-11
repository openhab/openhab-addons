/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal.json.response;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Serializable class to parse json response received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudServerContext {
    @Nullable
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

    private ICloudServerContextTimezone timezone;

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

    public ICloudServerContextTimezone getTimezone() {
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
}
