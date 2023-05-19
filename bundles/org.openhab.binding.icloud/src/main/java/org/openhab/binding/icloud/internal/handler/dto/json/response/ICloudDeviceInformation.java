/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal.handler.dto.json.response;

import java.util.ArrayList;

/**
 * Serializable class to parse json response received from the Apple server.
 * Contains device specific status information.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudDeviceInformation {
    private boolean activationLocked;

    private ArrayList<Object> audioChannels;

    private double batteryLevel;

    private String batteryStatus;

    private boolean canWipeAfterLock;

    private boolean darkWake;

    private String deviceClass;

    private String deviceColor;

    private String deviceDisplayName;

    private String deviceModel;

    private int deviceStatus;

    private ICloudDeviceFeatures features;

    private boolean fmlyShare;

    private String id;

    private String deviceDiscoveryId;

    private boolean isLocating;

    private boolean isMac;

    private ICloudDeviceLocation location;

    private boolean locationCapable;

    private boolean locationEnabled;

    private boolean locFoundEnabled;

    private Object lockedTimestamp;

    private Object lostDevice;

    private boolean lostModeCapable;

    private boolean lostModeEnabled;

    private String lostTimestamp;

    private boolean lowPowerMode;

    private int maxMsgChar;

    private Object mesg;

    private String modelDisplayName;

    private Object msg;

    private String name;

    private int passcodeLength;

    private String prsId;

    private String rawDeviceModel;

    private Object remoteLock;

    private Object remoteWipe;

    private Object snd;

    private boolean thisDevice;

    private Object trackingInfo;

    private Object wipedTimestamp;

    private boolean wipeInProgress;

    public boolean getActivationLocked() {
        return this.activationLocked;
    }

    public ArrayList<Object> getAudioChannels() {
        return this.audioChannels;
    }

    public double getBatteryLevel() {
        return this.batteryLevel;
    }

    public String getBatteryStatus() {
        return this.batteryStatus;
    }

    public boolean getCanWipeAfterLock() {
        return this.canWipeAfterLock;
    }

    public boolean getDarkWake() {
        return this.darkWake;
    }

    public String getDeviceClass() {
        return this.deviceClass;
    }

    public String getDeviceColor() {
        return this.deviceColor;
    }

    public String getDeviceDisplayName() {
        return this.deviceDisplayName;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public int getDeviceStatus() {
        return this.deviceStatus;
    }

    public ICloudDeviceFeatures getFeatures() {
        return this.features;
    }

    public boolean getFmlyShare() {
        return this.fmlyShare;
    }

    public String getId() {
        return this.id;
    }

    public String getDeviceDiscoveryId() {
        return this.deviceDiscoveryId;
    }

    public boolean getIsLocating() {
        return this.isLocating;
    }

    public boolean getIsMac() {
        return this.isMac;
    }

    public ICloudDeviceLocation getLocation() {
        return this.location;
    }

    public boolean getLocationCapable() {
        return this.locationCapable;
    }

    public boolean getLocationEnabled() {
        return this.locationEnabled;
    }

    public boolean getLocFoundEnabled() {
        return this.locFoundEnabled;
    }

    public Object getLockedTimestamp() {
        return this.lockedTimestamp;
    }

    public Object getLostDevice() {
        return this.lostDevice;
    }

    public boolean getLostModeCapable() {
        return this.lostModeCapable;
    }

    public boolean getLostModeEnabled() {
        return this.lostModeEnabled;
    }

    public String getLostTimestamp() {
        return this.lostTimestamp;
    }

    public boolean getLowPowerMode() {
        return this.lowPowerMode;
    }

    public int getMaxMsgChar() {
        return this.maxMsgChar;
    }

    public Object getMesg() {
        return this.mesg;
    }

    public String getModelDisplayName() {
        return this.modelDisplayName;
    }

    public Object getMsg() {
        return this.msg;
    }

    public String getName() {
        return this.name;
    }

    public int getPasscodeLength() {
        return this.passcodeLength;
    }

    public String getPrsId() {
        return this.prsId;
    }

    public String getRawDeviceModel() {
        return this.rawDeviceModel;
    }

    public Object getRemoteLock() {
        return this.remoteLock;
    }

    public Object getRemoteWipe() {
        return this.remoteWipe;
    }

    public Object getSnd() {
        return this.snd;
    }

    public boolean getThisDevice() {
        return this.thisDevice;
    }

    public Object getTrackingInfo() {
        return this.trackingInfo;
    }

    public Object getWipedTimestamp() {
        return this.wipedTimestamp;
    }

    public boolean getWipeInProgress() {
        return this.wipeInProgress;
    }
}
