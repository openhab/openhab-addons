/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

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
    // TODO add descriptions
    public final static String CHANNEL_GROUP_OS = "os";
    public final static String CHANNEL_GROUP_NETWORK = "network";
    public final static String CHANNEL_GROUP_DISPLAY = "display";
    public final static String CHANNEL_GROUP_SENSORS = "sensors";
    public final static String CHANNEL_GROUP_CPU = "cpu";
    public final static String CHANNEL_GROUP_STORAGE = "storage";
    public final static String CHANNEL_GROUP_MEMORY = "memory";
    public final static String CHANNEL_GROUP_BATTERY = "battery";

    // List of all Channel IDs
    public final static String CHANNEL_FAMILY = "family";
    public final static String CHANNEL_MANUFACTURER = "manufacturer";
    public final static String CHANNEL_VERSION = "version";

    public final static String CHANNEL_AVAILABLE = "available";
    public final static String CHANNEL_USED = "used";
    public final static String CHANNEL_TOTAL = "total";

    public final static String CHANNEL_CPU_TEMPERATURE = "cpuTemp";
    public final static String CHANNEL_CPU_VOLTAGE = "cpuVoltage";
    public final static String CHANNEL_FAN_SPEED = "fanSpeed";

    public final static String CHANNEL_NAME = "name";
    public final static String CHANNEL_REMAINING_CAPACITY = "remainingCapacity";
    public final static String CHANNEL_REMAINING_TIME = "remainingTime";

    public final static String CHANNEL_DESCRIPTION = "description";
    public final static String CHANNEL_CPU_LOGICAL_CORES = "logicalProcCount";
    public final static String CHANNEL_CPU_PHYSICAL_CORES = "physicalProcCount";
    public final static String CHANNEL_CPU_LOAD = "load";

    public final static String CHANNEL_EDID = "edid";

    public final static String CHANNEL_IP = "ip";
    public final static String CHANNEL_ADAPTER_NAME = "adapterName";
    // List of supported channels for every channel group type
    public final static Set<String> SUPPORTED_CHANNELS_FOR_GROUP_OS = ImmutableSet.of(CHANNEL_FAMILY,
            CHANNEL_MANUFACTURER, CHANNEL_VERSION);
    public final static Set<String> SUPPORTED_CHANNELS_FOR_GROUP_MEMORY = ImmutableSet.of(CHANNEL_AVAILABLE,
            CHANNEL_USED, CHANNEL_TOTAL);

    // List of all Channel Type IDs

}
