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
package org.openhab.binding.innogysmarthome.internal.client.entity.device;

import org.openhab.binding.innogysmarthome.internal.client.entity.state.BooleanState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DateTimeState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DoubleState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.IntegerState;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.StringState;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the state of the Device.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class State {
    /** Standard device states */
    @SerializedName("deviceInclusionState")
    private StringState deviceInclusionState;

    @SerializedName("deviceConfigurationState")
    private StringState deviceConfigurationState;

    private BooleanState isReachable;

    private StringState updateState;

    private StringState firmwareVersion;

    @SerializedName("WHRating")
    private DoubleState wHRating;

    /** SHC device states */
    // Removed updateAvailable because it is different between version 1 and 2 devices and not used anyway
    // Related to openhab-addons #6613
    // private StringState updateAvailable

    private DateTimeState lastReboot;

    private IntegerState memoryLoad;

    @SerializedName("CPULoad")
    private IntegerState cpuLoad;

    @SerializedName("LBDongleAttached")
    private BooleanState lBDongleAttached;

    @SerializedName("MBusDongleAttached")
    private BooleanState mBusDongleAttached;

    private IntegerState configVersion;

    @SerializedName("OSState")
    private StringState oSState;

    private IntegerState wifiSignalStrength;

    private StringState ethIpAddress;

    private StringState wifiIpAddress;

    private StringState ethMacAddress;

    private StringState wifiMacAddress;

    private StringState inUseAdapter;

    private BooleanState innogyLayerAttached;

    private BooleanState discoveryActive;

    private StringState operationStatus;

    private DoubleState currentUtcOffset;

    private DoubleState cpuUsage;

    private DoubleState diskUsage;

    private DoubleState memoryUsage;

    /**
     * @return the deviceInclusionState
     */
    public StringState getDeviceInclusionState() {
        return deviceInclusionState;
    }

    /**
     * @param deviceInclusionState the deviceInclusionState to set
     */
    public void setDeviceInclusionState(StringState deviceInclusionState) {
        this.deviceInclusionState = deviceInclusionState;
    }

    /**
     * @return the deviceConfigurationState
     */
    public StringState getDeviceConfigurationState() {
        return deviceConfigurationState;
    }

    /**
     * @param deviceConfigurationState the deviceConfigurationState to set
     */
    public void setDeviceConfigurationState(StringState deviceConfigurationState) {
        this.deviceConfigurationState = deviceConfigurationState;
    }

    /**
     * @return the isReachable
     */
    public BooleanState getIsReachable() {
        return isReachable;
    }

    /**
     * @param isReachable the isReachable to set
     */
    public void setIsReachable(BooleanState isReachable) {
        this.isReachable = isReachable;
    }

    /**
     * @return the updateState
     */
    public StringState getUpdateState() {
        return updateState;
    }

    /**
     * @param updateState the updateState to set
     */
    public void setUpdateState(StringState updateState) {
        this.updateState = updateState;
    }

    /**
     * @return the firmwareVersion
     */
    public StringState getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * @param firmwareVersion the firmwareVersion to set
     */
    public void setFirmwareVersion(StringState firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * @return the wHRating
     */
    public DoubleState getWHRating() {
        return wHRating;
    }

    /**
     * @param wHRating the wHRating to set
     */
    public void setWHRating(DoubleState wHRating) {
        this.wHRating = wHRating;
    }

    /**
     * @return the lastReboot
     */
    public DateTimeState getLastReboot() {
        return lastReboot;
    }

    /**
     * @param lastReboot the lastReboot to set
     */
    public void setLastReboot(DateTimeState lastReboot) {
        this.lastReboot = lastReboot;
    }

    /**
     * @return the memoryLoad
     */
    public IntegerState getMemoryLoad() {
        return memoryLoad;
    }

    /**
     * @param memoryLoad the memoryLoad to set
     */
    public void setMemoryLoad(IntegerState memoryLoad) {
        this.memoryLoad = memoryLoad;
    }

    /**
     * @return the cPULoad
     */
    public IntegerState getCPULoad() {
        return cpuLoad;
    }

    /**
     * @param cpuLoad the cPULoad to set
     */
    public void setCPULoad(IntegerState cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    /**
     * @return the lBDongleAttached
     */
    public BooleanState getLBDongleAttached() {
        return lBDongleAttached;
    }

    /**
     * @param lBDongleAttached the lBDongleAttached to set
     */
    public void setLBDongleAttached(BooleanState lBDongleAttached) {
        this.lBDongleAttached = lBDongleAttached;
    }

    /**
     * @return the mBusDongleAttached
     */
    public BooleanState getMBusDongleAttached() {
        return mBusDongleAttached;
    }

    /**
     * @param mBusDongleAttached the mBusDongleAttached to set
     */
    public void setMBusDongleAttached(BooleanState mBusDongleAttached) {
        this.mBusDongleAttached = mBusDongleAttached;
    }

    /**
     * @return the configVersion
     */
    public IntegerState getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the configVersion to set
     */
    public void setConfigVersion(IntegerState configVersion) {
        this.configVersion = configVersion;
    }

    /**
     * @return the oSState
     */
    public StringState getOSState() {
        return oSState;
    }

    /**
     * @param oSState the oSState to set
     */
    public void setOSState(StringState oSState) {
        this.oSState = oSState;
    }

    /**
     * @return the wifiSignalStrength
     */
    public IntegerState getWifiSignalStrength() {
        return wifiSignalStrength;
    }

    /**
     * @param wifiSignalStrength the wifiSignalStrength to set
     */
    public void setWifiSignalStrength(IntegerState wifiSignalStrength) {
        this.wifiSignalStrength = wifiSignalStrength;
    }

    /**
     * @return the ethIpAddress
     */
    public StringState getEthIpAddress() {
        return ethIpAddress;
    }

    /**
     * @param ethIpAddress the ethIpAddress to set
     */
    public void setEthIpAddress(StringState ethIpAddress) {
        this.ethIpAddress = ethIpAddress;
    }

    /**
     * @return the wifiIpAddress
     */
    public StringState getWifiIpAddress() {
        return wifiIpAddress;
    }

    /**
     * @param wifiIpAddress the wifiIpAddress to set
     */
    public void setWifiIpAddress(StringState wifiIpAddress) {
        this.wifiIpAddress = wifiIpAddress;
    }

    /**
     * @return the ethMacAddress
     */
    public StringState getEthMacAddress() {
        return ethMacAddress;
    }

    /**
     * @param ethMacAddress the ethMacAddress to set
     */
    public void setEthMacAddress(StringState ethMacAddress) {
        this.ethMacAddress = ethMacAddress;
    }

    /**
     * @return the wifiMacAddress
     */
    public StringState getWifiMacAddress() {
        return wifiMacAddress;
    }

    /**
     * @param wifiMacAddress the wifiMacAddress to set
     */
    public void setWifiMacAddress(StringState wifiMacAddress) {
        this.wifiMacAddress = wifiMacAddress;
    }

    /**
     * @return the inUseAdapter
     */
    public StringState getInUseAdapter() {
        return inUseAdapter;
    }

    /**
     * @param inUseAdapter the inUseAdapter to set
     */
    public void setInUseAdapter(StringState inUseAdapter) {
        this.inUseAdapter = inUseAdapter;
    }

    /**
     * @return the innogyLayerAttached
     */
    public BooleanState getInnogyLayerAttached() {
        return innogyLayerAttached;
    }

    /**
     * @param innogyLayerAttached the innogyLayerAttached to set
     */
    public void setInnogyLayerAttached(BooleanState innogyLayerAttached) {
        this.innogyLayerAttached = innogyLayerAttached;
    }

    /**
     * @return the discoveryActive
     */
    public BooleanState getDiscoveryActive() {
        return discoveryActive;
    }

    /**
     * @param discoveryActive the discoveryActive to set
     */
    public void setDiscoveryActive(BooleanState discoveryActive) {
        this.discoveryActive = discoveryActive;
    }

    /**
     * @return the operationStatus
     */
    public StringState getOperationStatus() {
        return operationStatus;
    }

    /**
     * @param operationStatus the operationStatus to set
     */
    public void setOperationStatus(StringState operationStatus) {
        this.operationStatus = operationStatus;
    }

    /**
     * @return the currentUtcOffset
     */
    public DoubleState getCurrentUtcOffset() {
        return currentUtcOffset;
    }

    /**
     * @param currentUtcOffset the currentUtcOffset to set
     */
    public void setCurrentUtcOffset(DoubleState currentUtcOffset) {
        this.currentUtcOffset = currentUtcOffset;
    }

    /**
     * @return the cpuUsage
     */
    public DoubleState getCpuUsage() {
        return cpuUsage;
    }

    /**
     * @param cpuUsage the cpuUsage to set
     */
    public void setCpuUsage(DoubleState cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * @return the diskUsage
     */
    public DoubleState getDiskUsage() {
        return diskUsage;
    }

    /**
     * @param diskUsage the diskUsage to set
     */
    public void setDiskUsage(DoubleState diskUsage) {
        this.diskUsage = diskUsage;
    }

    /**
     * @return the memoryUsage
     */
    public DoubleState getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * @param memoryUsage the memoryUsage to set
     */
    public void setMemoryUsage(DoubleState memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
}
