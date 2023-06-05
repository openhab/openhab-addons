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
package org.openhab.binding.systeminfo.internal.model;

import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.dimension.DataAmount;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

/**
 * {@link SysteminfoInterface} defines the methods needed to provide this binding with the required system information.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Wouter Born - Add null annotations
 * @author Mark Herwege - Add dynamic creation of extra channels
 * @author Mark Herwege - Use units of measure
 */
@NonNullByDefault
public interface SysteminfoInterface {

    /**
     * Initialize logic for the Systeminfo implementation
     */
    void initializeSysteminfo();

    // Operating system info
    /**
     * Get the Family of the operating system /e.g. Windows, Unix,.../
     */
    StringType getOsFamily();

    /**
     * Get the manufacturer of the operating system
     */
    StringType getOsManufacturer();

    /**
     * Get the version of the operating system
     *
     * @return
     */
    StringType getOsVersion();

    // CPU info
    /**
     * Get the name of the CPU
     */
    StringType getCpuName();

    /**
     * Get description about the CPU e.g (model, family, vendor, serial number, identifier, architecture(32bit or
     * 64bit))
     */
    StringType getCpuDescription();

    /**
     * Get the number of logical CPUs/cores available for processing.
     */
    DecimalType getCpuLogicalCores();

    /**
     * Get the number of physical CPUs/cores available for processing.
     */
    DecimalType getCpuPhysicalCores();

    /**
     * Returns the system cpu load.
     *
     * @return the system cpu load between 0 and 100% or null, if no information is available
     */
    @Nullable
    PercentType getSystemCpuLoad();

    /**
     * Returns the system load average for the last minute.
     *
     * @return the load as a number of processes or null, if no information is available
     */
    @Nullable
    DecimalType getCpuLoad1();

    /**
     * Returns the system load average for the last 5 minutes.
     *
     * @return the load as number of processes or null, if no information is available
     */
    @Nullable
    DecimalType getCpuLoad5();

    /**
     * Returns the system load average for the last 15 minutes.
     *
     * @return the load as number of processes or null, if no information is available
     */
    @Nullable
    DecimalType getCpuLoad15();

    /**
     * Get the System uptime (time since boot).
     *
     * @return time since boot
     */
    QuantityType<Time> getCpuUptime();

    /**
     * Get the number of threads currently running
     *
     * @return number of threads
     */
    DecimalType getCpuThreads();

    // Memory info
    /**
     * Returns total size of memory
     *
     * @return memory size
     */
    QuantityType<DataAmount> getMemoryTotal();

    /**
     * Returns available size of memory
     *
     * @return memory size
     */
    QuantityType<DataAmount> getMemoryAvailable();

    /**
     * Returns used size of memory
     *
     * @return memory size
     */
    QuantityType<DataAmount> getMemoryUsed();

    /**
     * Percents of available memory on the machine
     *
     * @return percent of available memory or null, if no information is available
     */
    @Nullable
    PercentType getMemoryAvailablePercent();

    /**
     * Percents of used memory on the machine
     *
     * @return percent of used memory or null, if no information is available
     */
    @Nullable
    PercentType getMemoryUsedPercent();

    // Swap memory info
    /**
     * Returns total size of swap memory
     *
     * @return memory size or 0, if there is no swap memory
     */
    QuantityType<DataAmount> getSwapTotal();

    /**
     * Returns available size swap of memory
     *
     * @return memory size or 0, if no there is no swap memory
     */
    QuantityType<DataAmount> getSwapAvailable();

    /**
     * Returns used size of swap memory
     *
     * @return memory size or 0, if no there is no swap memory
     */
    QuantityType<DataAmount> getSwapUsed();

    /**
     * Percents of available swap memory on the machine
     *
     * @return percent of available memory or null, if no there is no swap memory
     */
    @Nullable
    PercentType getSwapAvailablePercent();

    /**
     * Percents of used swap memory on the machine
     *
     * @return percent of used memory or null, if no there is no swap memory
     */
    @Nullable
    PercentType getSwapUsedPercent();

    // Storage info
    /**
     * Returns the total space of the logical storage volume.
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size
     * @throws DeviceNotFoundException
     */
    QuantityType<DataAmount> getStorageTotal(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Returns the available storage space on the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size
     * @throws DeviceNotFoundException
     */
    QuantityType<DataAmount> getStorageAvailable(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the used storage space on the logical storage volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return storage size
     * @throws DeviceNotFoundException
     */
    QuantityType<DataAmount> getStorageUsed(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the percent of available storage on the logical volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return percent of available storage or null
     * @throws DeviceNotFoundException
     */
    @Nullable
    PercentType getStorageAvailablePercent(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the percent of used storage on the logical volume
     *
     * @param deviceIndex - the index of the logical volume
     * @return percent of used storage or null
     * @throws DeviceNotFoundException
     */
    @Nullable
    PercentType getStorageUsedPercent(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the name of the logical storage volume
     *
     * @throws DeviceNotFoundException
     */
    StringType getStorageName(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the type of the logical storage volume (e.g. NTFS, FAT32)
     *
     * @throws DeviceNotFoundException
     */
    StringType getStorageType(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the description of the logical storage volume
     *
     * @throws DeviceNotFoundException
     */
    StringType getStorageDescription(int deviceIndex) throws DeviceNotFoundException;

    // Hardware drive info
    /**
     * Gets the name of the physical storage drive
     *
     * @param deviceIndex - index of the storage drive
     * @throws DeviceNotFoundException
     */
    StringType getDriveName(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the model of the physical storage drive
     *
     * @param deviceIndex - index of the storage drive
     * @throws DeviceNotFoundException
     */
    StringType getDriveModel(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Gets the serial number of the physical storage drive
     *
     * @param deviceIndex - index of the storage drive
     * @throws DeviceNotFoundException
     */
    StringType getDriveSerialNumber(int deviceIndex) throws DeviceNotFoundException;

    // Network info
    /**
     * Get the Host IP address of the network.
     *
     * @param networkIndex - the index of the network
     * @return 32-bit IPv4 address
     * @throws DeviceNotFoundException
     */
    StringType getNetworkIp(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get the name of this network.
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    StringType getNetworkName(int networkIndex) throws DeviceNotFoundException;

    /**
     * The description of the network. On some platforms, this is identical to the name.
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    StringType getNetworkDisplayName(int networkIndex) throws DeviceNotFoundException;

    /**
     * Gets the MAC Address of the network.
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    StringType getNetworkMac(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get number of packets received
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    DecimalType getNetworkPacketsReceived(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get number of packets sent
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    DecimalType getNetworkPacketsSent(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get data sent for this network
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    QuantityType<DataAmount> getNetworkDataSent(int networkIndex) throws DeviceNotFoundException;

    /**
     * Get data received for this network
     *
     * @param networkIndex - the index of the network
     * @throws DeviceNotFoundException
     */
    QuantityType<DataAmount> getNetworkDataReceived(int networkIndex) throws DeviceNotFoundException;

    // Display info
    /**
     * Get information about the display device as product number, manufacturer, serial number, width and height in cm";
     *
     * @param deviceIndex - the index of the display device
     * @throws DeviceNotFoundException
     */
    StringType getDisplayInformation(int deviceIndex) throws DeviceNotFoundException;

    // Sensors info
    /**
     * Get the information from the CPU temperature sensors.
     *
     * @return Temperature if available, null otherwise.
     */
    @Nullable
    QuantityType<Temperature> getSensorsCpuTemperature();

    /**
     * Get the information for the CPU voltage.
     *
     * @return Voltage if available, null otherwise.
     */
    @Nullable
    QuantityType<ElectricPotential> getSensorsCpuVoltage();

    /**
     * Get fan speed
     *
     * @param deviceIndex
     * @return Speed in rpm or null if unable to measure fan speed
     * @throws DeviceNotFoundException
     */
    @Nullable
    DecimalType getSensorsFanSpeed(int deviceIndex) throws DeviceNotFoundException;

    // Battery info
    /**
     * Get estimated time remaining for the power source.
     *
     * @param deviceIndex
     * @return duration remaining charge or null, if the time is estimated as unlimited
     * @throws DeviceNotFoundException
     */
    @Nullable
    QuantityType<Time> getBatteryRemainingTime(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Battery remaining capacity.
     *
     * @param deviceIndex
     * @return percentage value
     * @throws DeviceNotFoundException
     */
    PercentType getBatteryRemainingCapacity(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Get battery name
     *
     * @param deviceIndex
     * @throws DeviceNotFoundException
     */
    StringType getBatteryName(int deviceIndex) throws DeviceNotFoundException;

    /**
     * Get PID of process executing this code
     *
     * @return current process ID
     */
    int getCurrentProcessID();

    /**
     * Returns the name of the process
     *
     * @param pid - the PID of the process
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    @Nullable
    StringType getProcessName(int pid) throws DeviceNotFoundException;

    /**
     * Returns the CPU usage of the process
     *
     * @param pid - the PID of the process
     * @return - percentage value, can be above 100% if process uses multiple cores
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    @Nullable
    DecimalType getProcessCpuUsage(int pid) throws DeviceNotFoundException;

    /**
     * Returns the size of RAM memory only usage of the process
     *
     * @param pid - the PID of the process
     * @return memory size
     * @throws DeviceNotFoundException- thrown if process with this PID can not be found
     */
    @Nullable
    QuantityType<DataAmount> getProcessMemoryUsage(int pid) throws DeviceNotFoundException;

    /**
     * Returns the full path of the executing process.
     *
     * @param pid - the PID of the process
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    @Nullable
    StringType getProcessPath(int pid) throws DeviceNotFoundException;

    /**
     * Returns the number of threads in this process.
     *
     * @param pid - the PID of the process
     * @throws DeviceNotFoundException - thrown if process with this PID can not be found
     */
    @Nullable
    DecimalType getProcessThreads(int pid) throws DeviceNotFoundException;

    /**
     * Returns the number of network interfaces.
     *
     * @return network interface count
     */
    int getNetworkIFCount();

    /**
     * Returns the number of displays.
     *
     * @return display count
     */
    int getDisplayCount();

    /**
     * Returns the number of storages.
     *
     * @return storage count
     */
    int getFileOSStoreCount();

    /**
     * Returns the number of power sources/batteries.
     *
     * @return power source count
     */
    int getPowerSourceCount();

    /**
     * Returns the number of drives.
     *
     * @return drive count
     */
    int getDriveCount();

    /**
     * Returns the number of fans.
     *
     * @return fan count
     */
    int getFanCount();
}
