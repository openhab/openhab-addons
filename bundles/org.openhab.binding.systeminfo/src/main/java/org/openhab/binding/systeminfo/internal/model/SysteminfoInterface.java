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
package org.openhab.binding.systeminfo.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;

/**
 * {@link SysteminfoInterface} defines the methods needed to provide this binding with the required system information.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
public interface SysteminfoInterface {

    /**
     * Initialize logic for the Systeminfo implementation
     */
    public void initializeSysteminfo();

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
     * Get description about the CPU e.g (model, family, vendor, serial number, identifier, architecture(32bit or
     * 64bit))
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
     * Returns the system cpu load.
     *
     * @return the system cpu load between 0 and 1 or null, if no information is available
     */
    public @Nullable PercentType getSystemCpuLoad();

    /**
     * Returns the system load average for the last minute.
     *
     * @return the load as a number of processes or null, if no information is available
     */
    public @Nullable DecimalType getCpuLoad1();

    /**
     * Returns the system load average for the last 5 minutes.
     *
     * @return the load as number of processes or null, if no information is available
     */
    public @Nullable DecimalType getCpuLoad5();

    /**
     * Returns the system load average for the last 15 minutes.
     *
     * @return the load as number of processes or null, if no information is available
     */
    public @Nullable DecimalType getCpuLoad15();

    /**
     * Get the System uptime (time since boot).
     *
     * @return time in minutes since boot
     */
    public DecimalType getCpuUptime();

    /**
     * Get the number of threads currently running
     *
     * @return number of threads
     */
    public DecimalType getCpuThreads();

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
     * @return percent of available memory or null, if no information is available
     */
    public @Nullable DecimalType getMemoryAvailablePercent();

    /**
     * Percents of used memory on the machine
     *
     * @return percent of used memory or null, if no information is available
     */
    public @Nullable DecimalType getMemoryUsedPercent();

    // Swap memory info
    /**
     * Returns total size of swap memory
     *
     * @return memory size in MB or 0, if no there is no swap memory
     */
    public @Nullable DecimalType getSwapTotal();

    /**
     * Returns available size swap of memory
     *
     * @return memory size in MB or 0, if no there is no swap memory
     */
    public @Nullable DecimalType getSwapAvailable();

    /**
     * Returns used size of swap memory
     *
     * @return memory size in MB or 0, if no there is no swap memory
     */
    public @Nullable DecimalType getSwapUsed();

    /**
     * Percents of available swap memory on the machine
     *
     * @return percent of available memory or null, if no there is no swap memory
     */
    public @Nullable DecimalType getSwapAvailablePercent();

    /**
     * Percents of used swap memory on the machine
     *
     * @return percent of used memory or null, if no there is no swap memory
     */
    public @Nullable DecimalType getSwapUsedPercent();

    // Storage info
    /**
     * Returns the total space of the logical storage volume.
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size in MB
     * @throws DeviceNotFoundException
     */
    public DecimalType getStorageTotal(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Returns the available storage space on the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size in MB
     * @throws DeviceNotFoundException
     */
    public DecimalType getStorageAvailable(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the used storage space on the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size in MB
     * @throws DeviceNotFoundException
     */
    public DecimalType getStorageUsed(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the percent of available storage on the logical volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return percent of available storage or null
     * @throws DeviceNotFoundException
     */
    public @Nullable DecimalType getStorageAvailablePercent(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the percent of used storage on the logical volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return percent of used storage or null
     * @throws DeviceNotFoundException
     */
    public @Nullable DecimalType getStorageUsedPercent(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the name of the logical storage volume
     *
     * @throws DeviceNotFoundException
     */
    public StringType getStorageName(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the type of the logical storage volume (e.g. NTFS, FAT32)
     *
     * @throws DeviceNotFoundException
     */
    public StringType getStorageType(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the description of the logical storage volume
     *
     * @throws DeviceNotFoundException
     */
    public StringType getStorageDescription(int deviceIndex) throws DeviceNotFoundException;

    // Hardware drive info
    /**
     * Gets the name of the physical storage drive
     *
     * @param deviceIndex - index of the storage drive
     * @throws DeviceNotFoundException
     */
    public StringType getDriveName(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the model of the physical storage drive
     *
     * @param deviceIndex - index of the storage drive
     * @throws DeviceNotFoundException
     */
    public StringType getDriveModel(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the serial number of the physical storage drive
     *
     * @param deviceIndex - index of the storage drive
     * @throws DeviceNotFoundException
     */
    public StringType getDriveSerialNumber(int deviceIndex) throws DeviceNotFoundException;

    // Network info
    /**
     * Get the Host IP address of the network.
     *
     * @param networkIndex - the index of the network
     * @return 32-bit IPv4 address
     * @throws DeviceNotFoundException
     */
    public StringType getNetworkIp(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get the name of this network.
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public StringType getNetworkName(int networkIndex) throws DeviceNotFoundException;

    /**
     * The description of the network. On some platforms, this is identical to the name.
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public StringType getNetworkDisplayName(int networkIndex) throws DeviceNotFoundException;

    /**
     * Gets the MAC Address of the network.
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public StringType getNetworkMac(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get number of packets received
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public DecimalType getNetworkPacketsReceived(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get number of packets sent
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public DecimalType getNetworkPacketsSent(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get data sent in MB for this network
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public DecimalType getNetworkDataSent(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get data received in MB for this network
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    public DecimalType getNetworkDataReceived(int networkIndex) throws DeviceNotFoundException;

    // Display info
    /**
     * Get information about the display device as product number, manufacturer, serial number, width and height in cm";
     *
     * @param deviceIndex - the index of the display device
     * @throws DeviceNotFoundException
     */
    public StringType getDisplayInformation(int deviceIndex) throws DeviceNotFoundException;

    // Sensors info
    /**
     * Get the information from the CPU temperature sensors.
     *
     * @return Temperature in degrees Celsius if available, null otherwise.
     */
    public @Nullable DecimalType getSensorsCpuTemperature();

    /**
     * Get the information for the CPU voltage.
     *
     * @return Voltage in Volts if available, null otherwise.
     */
    public @Nullable DecimalType getSensorsCpuVoltage();

    /**
     * Get fan speed
     *
     * @param deviceIndex
     * @return Speed in rpm or null if unable to measure fan speed
     * @throws DeviceNotFoundException
     */
    public @Nullable DecimalType getSensorsFanSpeed(int deviceIndex) throws DeviceNotFoundException;

    // Battery info
    /**
     * Get estimated time remaining for the power source.
     *
     * @param deviceIndex
     * @return minutes remaining charge or null, if the time is estimated as unlimited
     * @throws DeviceNotFoundException
     */
    public @Nullable DecimalType getBatteryRemainingTime(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Battery remaining capacity.
     *
     * @param deviceIndex
     * @return percentage value /0-100/
     * @throws DeviceNotFoundException
     */
    public DecimalType getBatteryRemainingCapacity(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Get battery name
     *
     * @param deviceIndex
     * @throws DeviceNotFoundException
     */
    public StringType getBatteryName(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Returns the name of the process
     *
     * @param pid - the PID of the process
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    public @Nullable StringType getProcessName(int pid) throws DeviceNotFoundException;

    /**
     * Returns the CPU usage of the process
     *
     * @param pid - the PID of the process
     * @return - percentage value /0-100/
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    public @Nullable PercentType getProcessCpuUsage(int pid) throws DeviceNotFoundException;

    /**
     * Returns the size of RAM memory only usage of the process
     *
     * @param pid - the PID of the process
     * @return memory size in MB
     * @throws DeviceNotFoundException- thrown if process with this PID can not be found
     */
    public @Nullable DecimalType getProcessMemoryUsage(int pid) throws DeviceNotFoundException;

    /**
     * Returns the full path of the executing process.
     *
     * @param pid - the PID of the process
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    public @Nullable StringType getProcessPath(int pid) throws DeviceNotFoundException;

    /**
     * Returns the number of threads in this process.
     *
     * @param pid - the PID of the process
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    public @Nullable DecimalType getProcessThreads(int pid) throws DeviceNotFoundException;
}
