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
package org.openhab.binding.systeminfo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SysteminfoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Svilen Valkanov - Initial contribution
 */
@NonNullByDefault
public class SysteminfoBindingConstants {

    public static final String BINDING_ID = "systeminfo";

    public static final ThingTypeUID THING_TYPE_COMPUTER = new ThingTypeUID(BINDING_ID, "computer");

    // Thing properties
    /**
     * Number of CPU logical cores
     */
    public static final String PROPERTY_CPU_LOGICAL_CORES = "CPU Logical Cores";

    /**
     * Number of CPU physical cores
     */
    public static final String PROPERTY_CPU_PHYSICAL_CORES = "CPU Physical Cores";

    /**
     * Contains information about the family /Windows, Linux, OS X etc/ of the operation system
     */
    public static final String PROPERTY_OS_FAMILY = "OS Family";

    /**
     * Name of the manufacturer of the operation system
     */
    public static final String PROPERTY_OS_MANUFACTURER = "OS Manufacturer";

    /**
     * Version of the operation system
     */
    public static final String PROPERTY_OS_VERSION = "OS Version";

    // List of all Channel IDs

    /**
     * Size of the available memory
     */
    public static final String CHANNEL_MEMORY_AVAILABLE = "memory#available";

    /**
     * Size of the used memory
     */
    public static final String CHANNEL_MEMORY_USED = "memory#used";

    /**
     * Total size of the memory
     */
    public static final String CHANNEL_MEMORY_TOTAL = "memory#total";

    /**
     * Percents of the available memory
     */
    public static final String CHANNEL_MEMORY_AVAILABLE_PERCENT = "memory#availablePercent";

    /**
     * Percents of the used memory
     */
    public static final String CHANNEL_MEMORY_USED_PERCENT = "memory#usedPercent";

    /**
     * Percents of the used heap
     */
    public static final String CHANNEL_MEMORY_USED_HEAP_PERCENT = "memory#usedHeapPercent";

    /**
     * Bytes used in the heap
     */
    public static final String CHANNEL_MEMORY_HEAP_AVAILABLE = "memory#availableHeap";

    /**
     * Total size of swap memory
     */
    public static final String CHANNEL_SWAP_TOTAL = "swap#total";

    /**
     * Size of the available swap memory
     */
    public static final String CHANNEL_SWAP_AVAILABLE = "swap#available";

    /**
     * Size of the used swap memory
     */
    public static final String CHANNEL_SWAP_USED = "swap#used";

    /**
     * Percents of the available swap memory
     */
    public static final String CHANNEL_SWAP_AVAILABLE_PERCENT = "swap#availablePercent";

    /**
     * Percents of the used swap memory
     */
    public static final String CHANNEL_SWAP_USED_PERCENT = "swap#usedPercent";

    /**
     * Physical storage drive name
     */
    public static final String CHANNEL_DRIVE_NAME = "drive#name";

    /**
     * Physical storage drive model
     */
    public static final String CHANNEL_DRIVE_MODEL = "drive#model";

    /**
     * Physical storage drive serial number
     */
    public static final String CHANNEL_DRIVE_SERIAL = "drive#serial";

    /**
     * Name of the logical volume storage
     */
    public static final String CHANNEL_STORAGE_NAME = "storage#name";

    /**
     * Logical storage volume type -(e.g. NTFS, FAT32 ..)
     */
    public static final String CHANNEL_STORAGE_TYPE = "storage#type";

    /**
     * Description of the logical volume storage
     */
    public static final String CHANNEL_STORAGE_DESCRIPTION = "storage#description";

    /**
     * Size of the available storage space
     */
    public static final String CHANNEL_STORAGE_AVAILABLE = "storage#available";

    /**
     * Size of the used storage space
     */
    public static final String CHANNEL_STORAGE_USED = "storage#used";

    /**
     * Total storage space
     */
    public static final String CHANNEL_STORAGE_TOTAL = "storage#total";

    /**
     * Percents of the available storage space
     */
    public static final String CHANNEL_STORAGE_AVAILABLE_PERCENT = "storage#availablePercent";

    /**
     * Percents of the used storage space
     */
    public static final String CHANNEL_STORAGE_USED_PERCENT = "storage#usedPercent";

    /**
     * Temperature of the CPU measured from the sensors.
     */
    public static final String CHANNEL_SENSORS_CPU_TEMPERATURE = "sensors#cpuTemp";

    /**
     * Voltage of the CPU core.
     */
    public static final String CHANNEL_SENOSRS_CPU_VOLTAGE = "sensors#cpuVoltage";

    /**
     * Fan speed
     */
    public static final String CHANNEL_SENSORS_FAN_SPEED = "sensors#fanSpeed";

    /**
     * Name of the battery
     */
    public static final String CHANNEL_BATTERY_NAME = "battery#name";

    /**
     * Remaining capacity of the battery.
     */
    public static final String CHANNEL_BATTERY_REMAINING_CAPACITY = "battery#remainingCapacity";

    /**
     * Estimated remaining time of the battery
     */
    public static final String CHANNEL_BATTERY_REMAINING_TIME = "battery#remainingTime";

    /**
     * Detailed description about the CPU
     */
    public static final String CHANNEL_CPU_DESCRIPTION = "cpu#description";

    /**
     * Average recent CPU load
     */
    public static final String CHANNEL_CPU_LOAD = "cpu#load";

    /**
     * Average CPU load for the last minute
     */
    public static final String CHANNEL_CPU_LOAD_1 = "cpu#load1";

    /**
     * Average CPU load for the last 5 minutes
     */
    public static final String CHANNEL_CPU_LOAD_5 = "cpu#load5";

    /**
     * Average CPU load for the last 15 minutes
     */
    public static final String CHANNEL_CPU_LOAD_15 = "cpu#load15";

    /**
     * CPU name
     */
    public static final String CHANNEL_CPU_NAME = "cpu#name";

    /**
     * CPU uptime in minutes
     */
    public static final String CHANNEL_CPU_UPTIME = "cpu#uptime";

    /**
     * CPU running threads count
     */
    public static final String CHANNEL_CPU_THREADS = "cpu#threads";

    /**
     * Information about the display device
     */
    public static final String CHANNEL_DISPLAY_INFORMATION = "display#information";

    /**
     * Host IP address of the network
     */
    public static final String CHANNEL_NETWORK_IP = "network#ip";

    /**
     * Network display name
     */
    public static final String CHANNEL_NETWORK_ADAPTER_NAME = "network#networkName";

    /**
     * Network data sent
     */
    public static final String CHANNEL_NETWORK_DATA_SENT = "network#dataSent";

    /**
     * Network data received
     */
    public static final String CHANNEL_NETWORK_DATA_RECEIVED = "network#dataReceived";

    /**
     * Network packets sent
     */
    public static final String CHANNEL_NETWORK_PACKETS_SENT = "network#packetsSent";

    /**
     * Network packets received
     */
    public static final String CHANNEL_NETWORK_PACKETS_RECEIVED = "network#packetsReceived";

    /**
     * Network name
     */
    public static final String CHANNEL_NETWORK_NAME = "network#networkDisplayName";

    /**
     * Network mac address
     */
    public static final String CHANNEL_NETWORK_MAC = "network#mac";

    /**
     * Name of the channel group for process information
     */
    public static final String CHANNEL_GROUP_PROCESS = "process";

    /**
     * CPU load used from a process
     */

    public static final String CHANNEL_PROCESS_LOAD = "process#load";

    /**
     * Size of memory used from a process in MB
     */
    public static final String CHANNEL_PROCESS_MEMORY = "process#used";

    /**
     * Name of the process
     */
    public static final String CHANNEL_PROCESS_NAME = "process#name";

    /**
     * Number of threads, used form the process
     */
    public static final String CHANNEL_PROCESS_THREADS = "process#threads";

    /**
     * The full path of the process
     */
    public static final String CHANNEL_PROCESS_PATH = "process#path";

    // Thing configuraion
    /**
     * Name of the configuration parameter of the thing that defines refresh time for High priority channels
     */
    public static final String HIGH_PRIORITY_REFRESH_TIME = "interval_high";

    /**
     * Name of the configuration parameter of the thing that defines refresh time for Medium priority channels
     */
    public static final String MEDIUM_PRIORITY_REFRESH_TIME = "interval_medium";

    // Channel configuration

    /**
     * Name of the channel configuration parameter priority
     */
    public static final String PRIOIRITY_PARAM = "priority";

    /**
     * Name of the channel configuration parameter pid
     *
     */
    public static final String PID_PARAM = "pid";
}
