/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link systeminfoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class SysteminfoBindingConstants {

    public static final String BINDING_ID = "systeminfo";

    public final static ThingTypeUID THING_TYPE_COMPUTER = new ThingTypeUID(BINDING_ID, "computer");

    // List of all Channel IDs
    /**
     * Contains information about the family /Windows, Linux, OS X etc/ of the operation system
     */
    public final static String CHANNEL_OS_FAMILY = "os_family";

    /**
     * Name of the manufacturer of the operation system
     */
    public final static String CHANNEL_OS_MANUFACTURER = "os_manufacturer";

    /**
     * Version of the operation system
     */
    public final static String CHANNEL_OS_VERSION = "os_version";

    /**
     * Size of the available memory
     */
    public final static String CHANNEL_MEMORY_AVAILABLE = "memory_available";

    /**
     * Size of the used memory
     */
    public final static String CHANNEL_MEMORY_USED = "memory_used";

    /**
     * Total size of the memory
     */
    public final static String CHANNEL_MEMORY_TOTAL = "memory_total";

    /**
     * Percent of the available memory
     */
    public final static String CHANNEL_MEMORY_AVAILABLE_PERCENT = "memory_available_percent";
    /**
     * Name of the logical volume storage
     */
    public final static String CHANNEL_STORAGE_NAME = "storage_name";

    /**
     * Description of the logical volume storage
     */
    public final static String CHANNEL_STORAGE_DESCRIPTION = "storage_description";

    /**
     * Size of the available storage space
     */
    public final static String CHANNEL_STORAGE_AVAILABLE = "storage_available";

    /**
     * Size of the used storage space
     */
    public final static String CHANNEL_STORAGE_USED = "storage_used";

    /**
     * Total storage space
     */
    public final static String CHANNEL_STORAGE_TOTAL = "storage_total";

    /**
     * Percent of the available storage space
     */
    public final static String CHANNEL_STORAGE_AVAILABLE_PERCENT = "storage_available_percent";
    /**
     * Temperature of the CPU measured from the sensors.
     */
    public final static String CHANNEL_SENSORS_CPU_TEMPERATURE = "sensors_cpu_temperature";

    /**
     * Voltage of the CPU core.
     */
    public final static String CHANNEL_SENOSRS_CPU_VOLTAGE = "sensors_cpu_voltage";

    /**
     * Fan speed
     */
    public final static String CHANNEL_SENSORS_FAN_SPEED = "sensors_fan_speed";

    /**
     * Name of the battery
     */
    public final static String CHANNEL_BATTERY_NAME = "battery_name";

    /**
     * Remaining capacity of the battery.
     */
    public final static String CHANNEL_BATTERY_REMAINING_CAPACITY = "battery_remaining_capacity";

    /**
     * Estimated remaining time of the battery
     */
    public final static String CHANNEL_BATTERY_REMAINING_TIME = "battery_remaining_time";

    /**
     * Detailed description about the CPU
     */
    public final static String CHANNEL_CPU_DESCRIPTION = "cpu_description";

    /**
     * Number of CPU logical cores
     */
    public final static String CHANNEL_CPU_LOGICAL_CORES = "cpu_logical_cores";

    /**
     * Number of CPU physical cores
     */
    public final static String CHANNEL_CPU_PHYSICAL_CORES = "cpu_phisycal_cores";

    /**
     * Average CPU load
     */
    public final static String CHANNEL_CPU_LOAD = "cpu_load";

    /**
     * CPU name
     */
    public final static String CHANNEL_CPU_NAME = "cpu_name";

    /**
     * Information about the display device
     */
    public final static String CHANNEL_DISPLAY_INFORMATION = "display_information";

    /**
     * Host IP address of the network
     */
    public final static String CHANNEL_NETWORK_IP = "network_ip";

    /**
     * Network adapter name
     */
    public final static String CHANNEL_NETWORK_ADAPTER_NAME = "network_adapter_name";

    /**
     * Network name
     */
    public final static String CHANNEL_NETWORK_NAME = "network_name";

    /**
     * Name of the configuration parameter of the thing used to describe how often High priority channels will be
     * updated
     */
    public final static String HIGH_PRIORITY_REFRESH_TIME = "interval_high";

    /**
     * Name of the configuration parameter of the thing used to describe how often Medium priority channels will be
     * updated
     */
    public final static String MEDIUM_PRIORITY_REFRESH_TIME = "interval_medium";
}
