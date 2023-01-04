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
package org.openhab.binding.livisismarthome.internal.client.api.entity.device;

import org.openhab.binding.livisismarthome.internal.client.api.entity.state.BooleanStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.DateTimeStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.DoubleStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.IntegerStateDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.state.StringStateDTO;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the state of the Device.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class StateDTO {
    /** Standard device states */
    @SerializedName("deviceInclusionState")
    private StringStateDTO deviceInclusionState;
    @SerializedName("deviceConfigurationState")
    private StringStateDTO deviceConfigurationState;
    private BooleanStateDTO isReachable;
    private StringStateDTO updateState;
    private StringStateDTO firmwareVersion;
    @SerializedName("WHRating")
    private DoubleStateDTO wHRating;

    /** SHC device states */
    // Removed updateAvailable because it is different between version 1 and 2 devices and not used anyway
    // Related to openhab-addons #6613
    // private StringState updateAvailable
    private DateTimeStateDTO lastReboot;
    private DoubleStateDTO memoryLoad;
    @SerializedName("CPULoad")
    private DoubleStateDTO cpuLoad;
    @SerializedName("LBDongleAttached")
    private BooleanStateDTO lBDongleAttached;
    @SerializedName("MBusDongleAttached")
    private BooleanStateDTO mBusDongleAttached;
    private IntegerStateDTO configVersion;
    @SerializedName("OSState")
    private StringStateDTO osState;
    private IntegerStateDTO wifiSignalStrength;
    private StringStateDTO ethIpAddress;
    private StringStateDTO wifiIpAddress;
    private StringStateDTO ethMacAddress;
    private StringStateDTO wifiMacAddress;
    private StringStateDTO inUseAdapter;
    private BooleanStateDTO discoveryActive;
    private StringStateDTO operationStatus;
    private DoubleStateDTO currentUtcOffset;
    private DoubleStateDTO cpuUsage;
    private DoubleStateDTO diskUsage;
    private DoubleStateDTO memoryUsage;

    public StateDTO() {
        deviceInclusionState = new StringStateDTO();
        deviceConfigurationState = new StringStateDTO();
        isReachable = new BooleanStateDTO();
        updateState = new StringStateDTO();
        firmwareVersion = new StringStateDTO();
        wHRating = new DoubleStateDTO();
        lastReboot = new DateTimeStateDTO();
        memoryLoad = new DoubleStateDTO();
        cpuLoad = new DoubleStateDTO();
        lBDongleAttached = new BooleanStateDTO();
        mBusDongleAttached = new BooleanStateDTO();
        configVersion = new IntegerStateDTO();
        osState = new StringStateDTO();
        wifiSignalStrength = new IntegerStateDTO();
        ethIpAddress = new StringStateDTO();
        wifiIpAddress = new StringStateDTO();
        ethMacAddress = new StringStateDTO();
        wifiMacAddress = new StringStateDTO();
        inUseAdapter = new StringStateDTO();
        discoveryActive = new BooleanStateDTO();
        operationStatus = new StringStateDTO();
        currentUtcOffset = new DoubleStateDTO();
        cpuUsage = new DoubleStateDTO();
        diskUsage = new DoubleStateDTO();
        memoryUsage = new DoubleStateDTO();
    }

    /**
     * @return the deviceInclusionState
     */
    public StringStateDTO getDeviceInclusionState() {
        return deviceInclusionState;
    }

    /**
     * @param deviceInclusionState the deviceInclusionState to set
     */
    public void setDeviceInclusionState(StringStateDTO deviceInclusionState) {
        this.deviceInclusionState = deviceInclusionState;
    }

    /**
     * @return the deviceConfigurationState
     */
    public StringStateDTO getDeviceConfigurationState() {
        return deviceConfigurationState;
    }

    /**
     * @param deviceConfigurationState the deviceConfigurationState to set
     */
    public void setDeviceConfigurationState(StringStateDTO deviceConfigurationState) {
        this.deviceConfigurationState = deviceConfigurationState;
    }

    /**
     * @return the isReachable
     */
    public BooleanStateDTO getIsReachable() {
        return isReachable;
    }

    /**
     * @param isReachable the isReachable to set
     */
    public void setIsReachable(BooleanStateDTO isReachable) {
        this.isReachable = isReachable;
    }

    /**
     * @return the updateState
     */
    public StringStateDTO getUpdateState() {
        return updateState;
    }

    /**
     * @param updateState the updateState to set
     */
    public void setUpdateState(StringStateDTO updateState) {
        this.updateState = updateState;
    }

    /**
     * @return the firmwareVersion
     */
    public StringStateDTO getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * @param firmwareVersion the firmwareVersion to set
     */
    public void setFirmwareVersion(StringStateDTO firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * @return the wHRating
     */
    public DoubleStateDTO getWHRating() {
        return wHRating;
    }

    /**
     * @param wHRating the wHRating to set
     */
    public void setWHRating(DoubleStateDTO wHRating) {
        this.wHRating = wHRating;
    }

    /**
     * @return the lastReboot
     */
    public DateTimeStateDTO getLastReboot() {
        return lastReboot;
    }

    /**
     * @param lastReboot the lastReboot to set
     */
    public void setLastReboot(DateTimeStateDTO lastReboot) {
        this.lastReboot = lastReboot;
    }

    /**
     * @return the memoryLoad
     */
    public DoubleStateDTO getMemoryLoad() {
        return memoryLoad;
    }

    /**
     * @param memoryLoad the memoryLoad to set
     */
    public void setMemoryLoad(DoubleStateDTO memoryLoad) {
        this.memoryLoad = memoryLoad;
    }

    /**
     * @return the cPULoad
     */
    public DoubleStateDTO getCPULoad() {
        return cpuLoad;
    }

    /**
     * @param cpuLoad the cPULoad to set
     */
    public void setCPULoad(DoubleStateDTO cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    /**
     * @return the lBDongleAttached
     */
    public BooleanStateDTO getLBDongleAttached() {
        return lBDongleAttached;
    }

    /**
     * @param lBDongleAttached the lBDongleAttached to set
     */
    public void setLBDongleAttached(BooleanStateDTO lBDongleAttached) {
        this.lBDongleAttached = lBDongleAttached;
    }

    /**
     * @return the mBusDongleAttached
     */
    public BooleanStateDTO getMBusDongleAttached() {
        return mBusDongleAttached;
    }

    /**
     * @param mBusDongleAttached the mBusDongleAttached to set
     */
    public void setMBusDongleAttached(BooleanStateDTO mBusDongleAttached) {
        this.mBusDongleAttached = mBusDongleAttached;
    }

    /**
     * @return the configVersion
     */
    public IntegerStateDTO getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the configVersion to set
     */
    public void setConfigVersion(IntegerStateDTO configVersion) {
        this.configVersion = configVersion;
    }

    /**
     * @return the oSState
     */
    public StringStateDTO getOSState() {
        return osState;
    }

    /**
     * @param osState the oSState to set
     */
    public void setOSState(StringStateDTO osState) {
        this.osState = osState;
    }

    /**
     * @return the wifiSignalStrength
     */
    public IntegerStateDTO getWifiSignalStrength() {
        return wifiSignalStrength;
    }

    /**
     * @param wifiSignalStrength the wifiSignalStrength to set
     */
    public void setWifiSignalStrength(IntegerStateDTO wifiSignalStrength) {
        this.wifiSignalStrength = wifiSignalStrength;
    }

    /**
     * @return the ethIpAddress
     */
    public StringStateDTO getEthIpAddress() {
        return ethIpAddress;
    }

    /**
     * @param ethIpAddress the ethIpAddress to set
     */
    public void setEthIpAddress(StringStateDTO ethIpAddress) {
        this.ethIpAddress = ethIpAddress;
    }

    /**
     * @return the wifiIpAddress
     */
    public StringStateDTO getWifiIpAddress() {
        return wifiIpAddress;
    }

    /**
     * @param wifiIpAddress the wifiIpAddress to set
     */
    public void setWifiIpAddress(StringStateDTO wifiIpAddress) {
        this.wifiIpAddress = wifiIpAddress;
    }

    /**
     * @return the ethMacAddress
     */
    public StringStateDTO getEthMacAddress() {
        return ethMacAddress;
    }

    /**
     * @param ethMacAddress the ethMacAddress to set
     */
    public void setEthMacAddress(StringStateDTO ethMacAddress) {
        this.ethMacAddress = ethMacAddress;
    }

    /**
     * @return the wifiMacAddress
     */
    public StringStateDTO getWifiMacAddress() {
        return wifiMacAddress;
    }

    /**
     * @param wifiMacAddress the wifiMacAddress to set
     */
    public void setWifiMacAddress(StringStateDTO wifiMacAddress) {
        this.wifiMacAddress = wifiMacAddress;
    }

    /**
     * @return the inUseAdapter
     */
    public StringStateDTO getInUseAdapter() {
        return inUseAdapter;
    }

    /**
     * @param inUseAdapter the inUseAdapter to set
     */
    public void setInUseAdapter(StringStateDTO inUseAdapter) {
        this.inUseAdapter = inUseAdapter;
    }

    /**
     * @return the discoveryActive
     */
    public BooleanStateDTO getDiscoveryActive() {
        return discoveryActive;
    }

    /**
     * @param discoveryActive the discoveryActive to set
     */
    public void setDiscoveryActive(BooleanStateDTO discoveryActive) {
        this.discoveryActive = discoveryActive;
    }

    /**
     * @return the operationStatus
     */
    public StringStateDTO getOperationStatus() {
        return operationStatus;
    }

    /**
     * @param operationStatus the operationStatus to set
     */
    public void setOperationStatus(StringStateDTO operationStatus) {
        this.operationStatus = operationStatus;
    }

    /**
     * @return the operationStatus
     */
    public StringStateDTO getOperationStatus(boolean isSHCClassic) {
        if (isSHCClassic) {
            return getOSState();
        }
        return getOperationStatus();
    }

    /**
     * @return the currentUtcOffset
     */
    public DoubleStateDTO getCurrentUtcOffset() {
        return currentUtcOffset;
    }

    /**
     * @param currentUtcOffset the currentUtcOffset to set
     */
    public void setCurrentUtcOffset(DoubleStateDTO currentUtcOffset) {
        this.currentUtcOffset = currentUtcOffset;
    }

    /**
     * @return the cpuUsage
     */
    public DoubleStateDTO getCpuUsage() {
        return cpuUsage;
    }

    /**
     * @param cpuUsage the cpuUsage to set
     */
    public void setCpuUsage(DoubleStateDTO cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public DoubleStateDTO getCpuUsage(boolean isSHCClassic) {
        if (isSHCClassic) {
            return getCPULoad();
        }
        return getCpuUsage();
    }

    /**
     * @return the diskUsage
     */
    public DoubleStateDTO getDiskUsage() {
        return diskUsage;
    }

    /**
     * @param diskUsage the diskUsage to set
     */
    public void setDiskUsage(DoubleStateDTO diskUsage) {
        this.diskUsage = diskUsage;
    }

    /**
     * @return the memoryUsage
     */
    public DoubleStateDTO getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * @param memoryUsage the memoryUsage to set
     */
    public void setMemoryUsage(DoubleStateDTO memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    /**
     * @return the memoryUsage
     */
    public DoubleStateDTO getMemoryUsage(boolean isSHCClassic) {
        if (isSHCClassic) {
            return getMemoryLoad();
        }
        return getMemoryUsage();
    }
}
