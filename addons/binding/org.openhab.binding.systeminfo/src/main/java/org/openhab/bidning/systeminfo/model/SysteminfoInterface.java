/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.bidning.systeminfo.model;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * {@link SysteminfoInterface} defines the methods needed to provide this binding with the required system information.
 *
 * @author Svilen Valkanov
 *
 */
public interface SysteminfoInterface {

    // Operating system info
    /**
     * Get the Family of the operating system /e.g. Windows,Unix,.../
     */
    public StringType getOsFamily();

    /**
     * Get the manufacturer of the operating system
     */
    public StringType getOsManufacturer();

    /**
     * Get the version of the operating system
     *
     * @return
     */
    public StringType getOsVersion();

    // CPU info
    /**
     * Get the name of the CPU
     */
    public StringType getCpuName();

    /**
     * Get description about the CPU e.g(model,family, vendor, serial number, identifier, architecture(32bit or
     * 64bit)";)
     */
    public StringType getCpuDescription();

    /**
     * Get the number of logical CPUs/cores available for processing.
     */
    public DecimalType getCpuLogicalCores();

    /**
     * Get the number of physical CPUs/cores available for processing.
     */
    public DecimalType getCpuPhysicalCores();

    /**
     * Get the average CPU load for all logical processors
     *
     * @return the load as percentage value /0-100/
     */
    public DecimalType getCpuLoad();

    // Memory info
    /**
     * Returns total size of memory
     *
     * @return memory size in MB
     */
    public DecimalType getMemoryTotal();

    /**
     * Returns available size of memory
     *
     * @return memory size in MB
     */
    public DecimalType getMemoryAvailable();

    /**
     * Returns used size of memory
     *
     * @return memory size in MB
     */
    public DecimalType getMemoryUsed();

    /**
     * Percents of available memory on the machine
     *
     * @return percent of available memory
     */
    public DecimalType getMemoryAvailablePercent();

    // Storage info
    /**
     * Returns the total space of the logical storage volume.
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size in MB
     */
    public DecimalType getStorageTotal(int deviceIndex);

    /**
     * Returns the available storage space on the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size in MB
     */
    public DecimalType getStorageAvailable(int deviceIndex);

    /**
     * Gets the used storage space on the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size in MB
     */
    public DecimalType getStorageUsed(int deviceIndex);

    /**
     * Gets the percent of available storage on the logical volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return percent of available storage
     */
    public DecimalType getStorageAvailablePercent(int deviceIndex);

    /**
     * Gets the name of the logical storage volume
     */
    public StringType getStorageName(int deviceIndex);

    /**
     * Gets additional information about the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     */
    public StringType getStorageDescription(int deviceIndex);

    // Network info
    /**
     * Get the Host IP address of the network.
     *
     * @param networkIndex - the index of the network
     * @return 32-bit IPv4 address
     */
    public StringType getNetworkIp(int networkIndex);

    /**
     * Get the name of this network.
     *
     * @param networkIndex - the index of the network
     */
    public StringType getNetworkName(int networkIndex);

    /**
     * Get human readable description of the network device.
     *
     * @param networkIndex- the index of the network
     */
    public StringType getNetworkAdapterName(int networkIndex);

    // Display info
    /**
     * Get information about the display device as product number, manufacturer, serial number, width and height in cm";
     *
     * @param deviceIndex - the index of the display device
     */
    public StringType getDisplayInformation(int deviceIndex);

    // Sensors info
    /**
     * Get the information from the CPU temperature sensors.
     *
     * @return Temperature in degrees Celsius if available, 0 otherwise.
     */
    public DecimalType getSensorsCpuTemperature();

    /**
     * Get the information for the CPU voltage.
     *
     * @return Voltage in Volts if available, 0 otherwise.
     */
    public DecimalType getSensorsCpuVoltage();

    /**
     * Get fan speed
     * 
     * @param deviceIndex
     * @return Speed in rpm or 0 if unable to measure fan speed
     */
    public DecimalType getSensorsFanSpeed(int deviceIndex);

    // Battery info
    /**
     * Get estimated time remaining for the power source.
     *
     * @param deviceIndex
     * @return minutes remaining charge or 999, if the time is estimated as unlimited
     */
    public DecimalType getBatteryRemainingTime(int deviceIndex);

    /**
     * Battery remaining capacity.
     *
     * @param deviceIndex
     * @return percentage value /0-100/
     */
    public DecimalType getBatteryRemainingCapacity(int deviceIndex);

    /**
     * Get battery name
     *
     * @param deviceIndex
     */
    public StringType getBatteryName(int deviceIndex);

}
