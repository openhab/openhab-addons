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
package org.openhab.binding.wlanthermo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
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
    public static final ThingTypeUID THING_TYPE_WLANTHERMO_NANO_V1 = new ThingTypeUID(BINDING_ID, "nano");
    public static final ThingTypeUID THING_TYPE_WLANTHERMO_MINI = new ThingTypeUID(BINDING_ID, "mini");
    public static final ThingTypeUID THING_TYPE_WLANTHERMO_ESP32 = new ThingTypeUID(BINDING_ID, "esp32");

    // Properties
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_SERIAL = "serial";
    public static final String PROPERTY_ESP32_BT_ENABLED = "esp32_bt_enabled";
    public static final String PROPERTY_ESP32_PM_ENABLED = "esp32_pm_enabled";
    public static final String PROPERTY_ESP32_TEMP_CHANNELS = "esp32_temp_channels";
    public static final String PROPERTY_ESP32_PM_CHANNELS = "esp32_pm_channels";

    // List of all Channel ids
    // System Channels
    public static final String SYSTEM = "system";
    public static final String SYSTEM_SOC = "soc";
    public static final String SYSTEM_CHARGE = "charge";
    public static final String SYSTEM_RSSI = "rssi";
    public static final String SYSTEM_RSSI_SIGNALSTRENGTH = "rssi_signalstrength";
    public static final String SYSTEM_CPU_LOAD = "cpu_load";
    public static final String SYSTEM_CPU_TEMP = "cpu_temp";

    public static final String CHANNEL_PREFIX = "channel";

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

    public static final String CHANNEL_PITMASTER_PREFIX = "pit";
    public static final String CHANNEL_PITMASTER_1 = "pit1";
    public static final String CHANNEL_PITMASTER_2 = "pit2";
    public static final String CHANNEL_PITMASTER_ENABLED = "enabled"; // Mini
    public static final String CHANNEL_PITMASTER_CURRENT = "current"; // Mini
    public static final String CHANNEL_PITMASTER_SETPOINT = "setpoint"; // Mini+Nano
    public static final String CHANNEL_PITMASTER_DUTY_CYCLE = "duty_cycle"; // Mini+Nano
    public static final String CHANNEL_PITMASTER_LID_OPEN = "lid_open"; // Mini
    public static final String CHANNEL_PITMASTER_CHANNEL_ID = "channel_id"; // Mini+Nano
    public static final String CHANNEL_PITMASTER_STATE = "state"; // Nano
    public static final String CHANNEL_PITMASTER_PIDPROFILE = "pid_id"; // Nano

    public static final String TRIGGER_NONE = "";
    public static final String TRIGGER_ALARM_MIN = "MIN";
    public static final String TRIGGER_ALARM_MAX = "MAX";

    public static final DecimalType SIGNAL_STRENGTH_4 = new DecimalType(4);
    public static final DecimalType SIGNAL_STRENGTH_3 = new DecimalType(3);
    public static final DecimalType SIGNAL_STRENGTH_2 = new DecimalType(2);
    public static final DecimalType SIGNAL_STRENGTH_1 = new DecimalType(1);
}
