/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SysteminfoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Alexander Falkenstern - Process information
 */
@NonNullByDefault
public class SysteminfoBindingConstants {

    public static final String BINDING_ID = "systeminfo";

    public static final ThingTypeUID THING_TYPE_COMPUTER = new ThingTypeUID(BINDING_ID, "computer");
    public static final ThingTypeUID THING_TYPE_PROCESS = new ThingTypeUID(BINDING_ID, "process");

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

    // List of all Channel group IDs
    /**
     * Battery group ID
     */
    public static final String BATTERY_GROUP_ID = "battery";

    /**
     * CPU group ID
     */
    public static final String CPU_GROUP_ID = "cpu";

    /**
     * Display group ID
     */
    public static final String DISPLAY_GROUP_ID = "display";

    /**
     * Physical storage drive group ID
     */
    public static final String DRIVE_GROUP_ID = "drive";

    /**
     * Physical sensors group ID
     */
    public static final String FANS_GROUP_ID = "fans";

    /**
     * Memory group ID
     */
    public static final String MEMORY_GROUP_ID = "memory";

    /**
     * Network group ID
     */
    public static final String NETWORK_GROUP_ID = "network";

    /**
     * Swap group ID
     */
    public static final String STORAGE_GROUP_ID = "storage";

    /**
     * Swap group ID
     */
    public static final String SWAP_GROUP_ID = "swap";

    // List of all Channel IDs
    /**
     * Name
     */
    public static final String CHANNEL_NAME = "name";

    /**
     * Detailed description
     */
    public static final String CHANNEL_DESCRIPTION = "description";

    /**
     * Running threads count
     */
    public static final String CHANNEL_THREADS = "threads";

    /**
     * Size of the available memory
     */
    public static final String CHANNEL_AVAILABLE = "available";

    /**
     * Total size of the memory
     */
    public static final String CHANNEL_TOTAL = "total";

    /**
     * Size of the used memory
     */
    public static final String CHANNEL_USED = "used";

    /**
     * Percents of the available memory
     */
    public static final String CHANNEL_AVAILABLE_PERCENT = "availablePercent";

    /**
     * Percents of the used memory
     */
    public static final String CHANNEL_USED_PERCENT = "usedPercent";

    /**
     * Physical storage drive model
     */
    public static final String CHANNEL_DRIVE_MODEL = "model";

    /**
     * Physical storage drive serial number
     */
    public static final String CHANNEL_DRIVE_SERIAL = "serial";

    /**
     * Logical storage volume type -(e.g. NTFS, FAT32 ..)
     */
    public static final String CHANNEL_STORAGE_TYPE = "type";

    /**
     * Fan speed
     */
    public static final String CHANNEL_FAN_SPEED = "speed";

    /**
     * Remaining capacity of the battery.
     */
    public static final String CHANNEL_BATTERY_REMAINING_CAPACITY = "remainingCapacity";

    /**
     * Estimated remaining time of the battery
     */
    public static final String CHANNEL_BATTERY_REMAINING_TIME = "remainingTime";

    /**
     * Average CPU load for the last minute
     */
    public static final String CHANNEL_CPU_LOAD = "load";

    /**
     * Average CPU load for the last minute
     */
    public static final String CHANNEL_CPU_LOAD_1 = "load1";

    /**
     * Average CPU load for the last 5 minutes
     */
    public static final String CHANNEL_CPU_LOAD_5 = "load5";

    /**
     * Average CPU load for the last 15 minutes
     */
    public static final String CHANNEL_CPU_LOAD_15 = "load15";

    /**
     * Temperature of the CPU measured from the sensors.
     */
    public static final String CHANNEL_CPU_TEMPERATURE = "temperature";

    /**
     * Voltage of the CPU core.
     */
    public static final String CHANNEL_CPU_VOLTAGE = "voltage";

    /**
     * CPU uptime in minutes
     */
    public static final String CHANNEL_CPU_UPTIME = "uptime";

    /**
     * Information about the display device
     */
    public static final String CHANNEL_DISPLAY_INFORMATION = "information";

    /**
     * Host IP address of the network
     */
    public static final String CHANNEL_NETWORK_IP = "ip";

    /**
     * Network display name
     */
    public static final String CHANNEL_NETWORK_INTERFACE = "interface";

    /**
     * Network data sent
     */
    public static final String CHANNEL_NETWORK_DATA_SENT = "dataSent";

    /**
     * Network data received
     */
    public static final String CHANNEL_NETWORK_DATA_RECEIVED = "dataReceived";

    /**
     * Network packets sent
     */
    public static final String CHANNEL_NETWORK_PACKETS_SENT = "packetsSent";

    /**
     * Network packets received
     */
    public static final String CHANNEL_NETWORK_PACKETS_RECEIVED = "packetsReceived";

    /**
     * Network mac address
     */
    public static final String CHANNEL_NETWORK_MAC = "mac";

    /**
     * CPU load used from a process
     */
    public static final String CHANNEL_PROCESS_LOAD = "load";

    /**
     * Size of memory used from a process in MB
     */
    public static final String CHANNEL_PROCESS_RESIDENT_MEMORY = "resident";

    /**
     * Size of memory used from a process in MB
     */
    public static final String CHANNEL_PROCESS_VIRTUAL_MEMORY = "virtual";

    /**
     * Number of threads, used form the process
     */
    public static final String CHANNEL_PROCESS_THREADS = "threads";

    /**
     * The full path of the process
     */
    public static final String CHANNEL_PROCESS_PATH = "path";

    /**
     * Name of the process
     */
    public static final String CHANNEL_PROCESS_USER = "user";

    // Thing configuraion
    /**
     * Name of the configuration parameter of the thing that defines refresh time for High priority channels
     */
    public static final String HIGH_PRIORITY_REFRESH_TIME = "interval_high";

    /**
     * Name of the configuration parameter of the thing that defines refresh time for Medium priority channels
     */
    public static final String MEDIUM_PRIORITY_REFRESH_TIME = "interval_medium";

    /**
     * Name of the channel configuration parameter pid
     *
     */
    public static final String PROCESS_ID = "pid";

    // Channel configuration
    public static final String LOW_PRIOIRITY = "Low";
    public static final String MEDIUM_PRIOIRITY = "Medium";
    public static final String HIGH_PRIOIRITY = "High";

    /**
     * Name of the channel configuration parameter priority
     */
    public static final String PARAMETER_PRIOIRITY = "priority";

}
