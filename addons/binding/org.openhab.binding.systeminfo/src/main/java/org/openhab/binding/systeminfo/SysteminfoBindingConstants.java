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

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_COMPUTER = new ThingTypeUID(BINDING_ID, "computer");

    // List of all Channel Groups Type IDs

    // List of all Channel IDs
    public final static String CHANNEL_OS_FAMILY = "os_family";
    public final static String CHANNEL_OS_MANUFACTURER = "os_manufacturer";
    public final static String CHANNEL_OS_VERSION = "os_version";

    public final static String CHANNEL_MEMORY_AVAILABLE = "memory_available";
    public final static String CHANNEL_MEMORY_USED = "memory_used";
    public final static String CHANNEL_MEMORY_TOTAL = "memory_total";

    public final static String CHANNEL_STORAGE_NAME = "storage_name";
    public final static String CHANNEL_STORAGE_DESCRIPTION = "storage_description";
    public final static String CHANNEL_STORAGE_AVAILABLE = "storage_available";
    public final static String CHANNEL_STORAGE_USED = "storage_used";
    public final static String CHANNEL_STORAGE_TOTAL = "storage_total";

    public final static String CHANNEL_SENSORS_CPU_TEMPERATURE = "sensors_cpu_temperature";
    public final static String CHANNEL_SENOSRS_CPU_VOLTAGE = "sensors_cpu_voltage";
    public final static String CHANNEL_SENSORS_FAN_SPEED = "sensors_fan_speed";

    public final static String CHANNEL_BATTERY_NAME = "battery_name";
    public final static String CHANNEL_BATTERY_REMAINING_CAPACITY = "battery_remaining_capacity";
    public final static String CHANNEL_BATTERY_REMAINING_TIME = "battery_remaining_time";

    public final static String CHANNEL_CPU_DESCRIPTION = "cpu_description";
    public final static String CHANNEL_CPU_LOGICAL_CORES = "cpu_logical_cores";
    public final static String CHANNEL_CPU_PHYSICAL_CORES = "cpu_phisycal_cores";
    public final static String CHANNEL_CPU_LOAD = "cpu_load";
    public final static String CHANNEL_CPU_NAME = "cpu_name";

    public final static String CHANNEL_DISPLAY_INFORMATION = "display_information";

    public final static String CHANNEL_NETWORK_IP = "network_ip";
    public final static String CHANNEL_NETWORK_ADAPTER_NAME = "network_adapter_name";
    public final static String CHANNEL_NETWORK_NAME = "network_name";

    public final static String HIGH_PRIORITY_REFRESH_TIME = "interval_high";
    public final static String MEDIUM_PRIORITY_REFRESH_TIME = "interval_medium";
}
