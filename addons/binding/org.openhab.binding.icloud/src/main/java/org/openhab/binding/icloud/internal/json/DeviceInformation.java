/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json;

import java.util.ArrayList;

/**
 *
 * Contains device information received via iCloud (json).
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class DeviceInformation {
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

    private Features features;

    private boolean fmlyShare;

    private String id;

    private boolean isLocating;

    private boolean isMac;

    private Location location;

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

    public Features getFeatures() {
        return this.features;
    }

    public boolean getFmlyShare() {
        return this.fmlyShare;
    }

    public String getId() {
        return this.id;
    }

    public boolean getIsLocating() {
        return this.isLocating;
    }

    public boolean getIsMac() {
        return this.isMac;
    }

    public Location getLocation() {
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

    public void setActivationLocked(boolean activationLocked) {
        this.activationLocked = activationLocked;
    }

    public void setAudioChannels(ArrayList<Object> audioChannels) {
        this.audioChannels = audioChannels;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public void setCanWipeAfterLock(boolean canWipeAfterLock) {
        this.canWipeAfterLock = canWipeAfterLock;
    }

    public void setDarkWake(boolean darkWake) {
        this.darkWake = darkWake;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    public void setDeviceColor(String deviceColor) {
        this.deviceColor = deviceColor;
    }

    public void setDeviceDisplayName(String deviceDisplayName) {
        this.deviceDisplayName = deviceDisplayName;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setDeviceStatus(int deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public void setFmlyShare(boolean fmlyShare) {
        this.fmlyShare = fmlyShare;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIsLocating(boolean isLocating) {
        this.isLocating = isLocating;
    }

    public void setIsMac(boolean isMac) {
        this.isMac = isMac;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocationCapable(boolean locationCapable) {
        this.locationCapable = locationCapable;
    }

    public void setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
    }

    public void setLocFoundEnabled(boolean locFoundEnabled) {
        this.locFoundEnabled = locFoundEnabled;
    }

    public void setLockedTimestamp(Object lockedTimestamp) {
        this.lockedTimestamp = lockedTimestamp;
    }

    public void setLostDevice(Object lostDevice) {
        this.lostDevice = lostDevice;
    }

    public void setLostModeCapable(boolean lostModeCapable) {
        this.lostModeCapable = lostModeCapable;
    }

    public void setLostModeEnabled(boolean lostModeEnabled) {
        this.lostModeEnabled = lostModeEnabled;
    }

    public void setLostTimestamp(String lostTimestamp) {
        this.lostTimestamp = lostTimestamp;
    }

    public void setLowPowerMode(boolean lowPowerMode) {
        this.lowPowerMode = lowPowerMode;
    }

    public void setMaxMsgChar(int maxMsgChar) {
        this.maxMsgChar = maxMsgChar;
    }

    public void setMesg(Object mesg) {
        this.mesg = mesg;
    }

    public void setModelDisplayName(String modelDisplayName) {
        this.modelDisplayName = modelDisplayName;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPasscodeLength(int passcodeLength) {
        this.passcodeLength = passcodeLength;
    }

    public void setPrsId(String prsId) {
        this.prsId = prsId;
    }

    public void setRawDeviceModel(String rawDeviceModel) {
        this.rawDeviceModel = rawDeviceModel;
    }

    public void setRemoteLock(Object remoteLock) {
        this.remoteLock = remoteLock;
    }

    public void setRemoteWipe(Object remoteWipe) {
        this.remoteWipe = remoteWipe;
    }

    public void setSnd(Object snd) {
        this.snd = snd;
    }

    public void setThisDevice(boolean thisDevice) {
        this.thisDevice = thisDevice;
    }

    public void setTrackingInfo(Object trackingInfo) {
        this.trackingInfo = trackingInfo;
    }

    public void setWipedTimestamp(Object wipedTimestamp) {
        this.wipedTimestamp = wipedTimestamp;
    }

    public void setWipeInProgress(boolean wipeInProgress) {
        this.wipeInProgress = wipeInProgress;
    }
}
