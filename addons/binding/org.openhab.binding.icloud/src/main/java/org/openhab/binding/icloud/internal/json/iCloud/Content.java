/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.iCloud;

import java.util.ArrayList;

public class Content {
    private Object msg;

    public Object getMsg() {
        return this.msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    private boolean canWipeAfterLock;

    public boolean getCanWipeAfterLock() {
        return this.canWipeAfterLock;
    }

    public void setCanWipeAfterLock(boolean canWipeAfterLock) {
        this.canWipeAfterLock = canWipeAfterLock;
    }

    private boolean wipeInProgress;

    public boolean getWipeInProgress() {
        return this.wipeInProgress;
    }

    public void setWipeInProgress(boolean wipeInProgress) {
        this.wipeInProgress = wipeInProgress;
    }

    private boolean lostModeEnabled;

    public boolean getLostModeEnabled() {
        return this.lostModeEnabled;
    }

    public void setLostModeEnabled(boolean lostModeEnabled) {
        this.lostModeEnabled = lostModeEnabled;
    }

    private boolean activationLocked;

    public boolean getActivationLocked() {
        return this.activationLocked;
    }

    public void setActivationLocked(boolean activationLocked) {
        this.activationLocked = activationLocked;
    }

    private int passcodeLength;

    public int getPasscodeLength() {
        return this.passcodeLength;
    }

    public void setPasscodeLength(int passcodeLength) {
        this.passcodeLength = passcodeLength;
    }

    private String deviceStatus;

    public String getDeviceStatus() {
        return this.deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    private String deviceColor;

    public String getDeviceColor() {
        return this.deviceColor;
    }

    public void setDeviceColor(String deviceColor) {
        this.deviceColor = deviceColor;
    }

    private Features features;

    public Features getFeatures() {
        return this.features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    private boolean lowPowerMode;

    public boolean getLowPowerMode() {
        return this.lowPowerMode;
    }

    public void setLowPowerMode(boolean lowPowerMode) {
        this.lowPowerMode = lowPowerMode;
    }

    private String rawDeviceModel;

    public String getRawDeviceModel() {
        return this.rawDeviceModel;
    }

    public void setRawDeviceModel(String rawDeviceModel) {
        this.rawDeviceModel = rawDeviceModel;
    }

    private String id;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private Object remoteLock;

    public Object getRemoteLock() {
        return this.remoteLock;
    }

    public void setRemoteLock(Object remoteLock) {
        this.remoteLock = remoteLock;
    }

    private boolean isLocating;

    public boolean getIsLocating() {
        return this.isLocating;
    }

    public void setIsLocating(boolean isLocating) {
        this.isLocating = isLocating;
    }

    private String modelDisplayName;

    public String getModelDisplayName() {
        return this.modelDisplayName;
    }

    public void setModelDisplayName(String modelDisplayName) {
        this.modelDisplayName = modelDisplayName;
    }

    private String lostTimestamp;

    public String getLostTimestamp() {
        return this.lostTimestamp;
    }

    public void setLostTimestamp(String lostTimestamp) {
        this.lostTimestamp = lostTimestamp;
    }

    private double batteryLevel;

    public double getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    private Object mesg;

    public Object getMesg() {
        return this.mesg;
    }

    public void setMesg(Object mesg) {
        this.mesg = mesg;
    }

    private boolean locationEnabled;

    public boolean getLocationEnabled() {
        return this.locationEnabled;
    }

    public void setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
    }

    private Object lockedTimestamp;

    public Object getLockedTimestamp() {
        return this.lockedTimestamp;
    }

    public void setLockedTimestamp(Object lockedTimestamp) {
        this.lockedTimestamp = lockedTimestamp;
    }

    private boolean locFoundEnabled;

    public boolean getLocFoundEnabled() {
        return this.locFoundEnabled;
    }

    public void setLocFoundEnabled(boolean locFoundEnabled) {
        this.locFoundEnabled = locFoundEnabled;
    }

    private Object snd;

    public Object getSnd() {
        return this.snd;
    }

    public void setSnd(Object snd) {
        this.snd = snd;
    }

    private boolean fmlyShare;

    public boolean getFmlyShare() {
        return this.fmlyShare;
    }

    public void setFmlyShare(boolean fmlyShare) {
        this.fmlyShare = fmlyShare;
    }

    private Object lostDevice;

    public Object getLostDevice() {
        return this.lostDevice;
    }

    public void setLostDevice(Object lostDevice) {
        this.lostDevice = lostDevice;
    }

    private boolean lostModeCapable;

    public boolean getLostModeCapable() {
        return this.lostModeCapable;
    }

    public void setLostModeCapable(boolean lostModeCapable) {
        this.lostModeCapable = lostModeCapable;
    }

    private Object wipedTimestamp;

    public Object getWipedTimestamp() {
        return this.wipedTimestamp;
    }

    public void setWipedTimestamp(Object wipedTimestamp) {
        this.wipedTimestamp = wipedTimestamp;
    }

    private String deviceDisplayName;

    public String getDeviceDisplayName() {
        return this.deviceDisplayName;
    }

    public void setDeviceDisplayName(String deviceDisplayName) {
        this.deviceDisplayName = deviceDisplayName;
    }

    private String prsId;

    public String getPrsId() {
        return this.prsId;
    }

    public void setPrsId(String prsId) {
        this.prsId = prsId;
    }

    private ArrayList<Object> audioChannels;

    public ArrayList<Object> getAudioChannels() {
        return this.audioChannels;
    }

    public void setAudioChannels(ArrayList<Object> audioChannels) {
        this.audioChannels = audioChannels;
    }

    private boolean locationCapable;

    public boolean getLocationCapable() {
        return this.locationCapable;
    }

    public void setLocationCapable(boolean locationCapable) {
        this.locationCapable = locationCapable;
    }

    private String batteryStatus;

    public String getBatteryStatus() {
        return this.batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    private Object trackingInfo;

    public Object getTrackingInfo() {
        return this.trackingInfo;
    }

    public void setTrackingInfo(Object trackingInfo) {
        this.trackingInfo = trackingInfo;
    }

    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean isMac;

    public boolean getIsMac() {
        return this.isMac;
    }

    public void setIsMac(boolean isMac) {
        this.isMac = isMac;
    }

    private boolean thisDevice;

    public boolean getThisDevice() {
        return this.thisDevice;
    }

    public void setThisDevice(boolean thisDevice) {
        this.thisDevice = thisDevice;
    }

    private String deviceClass;

    public String getDeviceClass() {
        return this.deviceClass;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    private Location location;

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private String deviceModel;

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    private int maxMsgChar;

    public int getMaxMsgChar() {
        return this.maxMsgChar;
    }

    public void setMaxMsgChar(int maxMsgChar) {
        this.maxMsgChar = maxMsgChar;
    }

    private boolean darkWake;

    public boolean getDarkWake() {
        return this.darkWake;
    }

    public void setDarkWake(boolean darkWake) {
        this.darkWake = darkWake;
    }

    private Object remoteWipe;

    public Object getRemoteWipe() {
        return this.remoteWipe;
    }

    public void setRemoteWipe(Object remoteWipe) {
        this.remoteWipe = remoteWipe;
    }
}
