/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WlanThermoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoBindingConstants {

    private static final String BINDING_ID = "wlanthermo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WLANTHERMO_NANO = new ThingTypeUID(BINDING_ID, "nano");
    public static final ThingTypeUID THING_TYPE_WLANTHERMO_MINI = new ThingTypeUID(BINDING_ID, "mini");

    // ThreadPool
    public static final String WLANTHERMO_THREAD_POOL = "wlanthermo";

    // List of all Channel ids
    // System Channels
    public static final String SYSTEM = "system";
    public static final String SYSTEM_SOC = "soc";
    public static final String SYSTEM_CHARGE = "charge";
    public static final String SYSTEM_RSSI = "rssi";
    public static final String SYSTEM_RSSI_SIGNALSTRENGTH = "rssi_signalstrength";
    public static final String SYSTEM_CPU_LOAD = "cpu_load";
    public static final String SYSTEM_CPU_TEMP = "cpu_temp";

    public static final String CHANNEL0 = "channel0";
    public static final String CHANNEL1 = "channel1";
    public static final String CHANNEL2 = "channel2";
    public static final String CHANNEL3 = "channel3";
    public static final String CHANNEL4 = "channel4";
    public static final String CHANNEL5 = "channel5";
    public static final String CHANNEL6 = "channel6";
    public static final String CHANNEL7 = "channel7";
    public static final String CHANNEL8 = "channel8";
    public static final String CHANNEL9 = "channel9";

    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_TYP = "typ";
    public static final String CHANNEL_TEMP = "temp";
    public static final String CHANNEL_MIN = "min";
    public static final String CHANNEL_MAX = "max";
    public static final String CHANNEL_ALARM_DEVICE = "alarm_device";
    public static final String CHANNEL_ALARM_PUSH = "alarm_push";
    public static final String CHANNEL_ALARM_OPENHAB = "alarm_openhab";
    public static final String CHANNEL_ALARM_OPENHAB_LOW = "alarm_openhab_low";
    public static final String CHANNEL_ALARM_OPENHAB_HIGH = "alarm_openhab_high";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_NAME = "color_name";

    public static final String CHANNEL_PITMASTER_ENABLED = "enabled"; // Mini
    public static final String CHANNEL_PITMASTER_CURRENT = "current"; // Mini
    public static final String CHANNEL_PITMASTER_SETPOINT = "setpoint"; // Mini+Nano
    public static final String CHANNEL_PITMASTER_DUTY_CYCLE = "duty_cycle"; // Mini+Nano
    public static final String CHANNEL_PITMASTER_LID_OPEN = "lid_open"; // Mini
    public static final String CHANNEL_PITMASTER_CHANNEL_ID = "channel_id"; // Mini+Nano
    public static final String CHANNEL_PITMASTER_STATE = "state"; // Nano
    public static final String CHANNEL_PITMASTER_PIDPROFILE = "pid_id"; // Nano

    public static final String TRIGGER_ALARM_OFF = "OFF";
    public static final String TRIGGER_ALARM_MIN = "MIN";
    public static final String TRIGGER_ALARM_MAX = "MAX";
}
