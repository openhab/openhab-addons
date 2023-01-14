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
package org.openhab.binding.gree.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GreeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeBindingConstants {

    public static final String BINDING_ID = "gree";

    public static final ThingTypeUID THING_TYPE_GREEAIRCON = new ThingTypeUID(BINDING_ID, "airconditioner");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GREEAIRCON);

    // List of all Thing Type UIDs
    public static final ThingTypeUID GREE_THING_TYPE = new ThingTypeUID(BINDING_ID, "airconditioner");

    // Thing configuration items
    public static final String PROPERTY_IP = "ipAddress";
    public static final String PROPERTY_BROADCAST = "broadcastAddress";

    // List of all Channel ids
    public static final String POWER_CHANNEL = "power";
    public static final String MODE_CHANNEL = "mode";
    public static final String TURBO_CHANNEL = "turbo";
    public static final String LIGHT_CHANNEL = "light";
    public static final String TARGET_TEMP_CHANNEL = "temperature";
    public static final String CURRENT_TEMP_CHANNEL = "currentTemperature";
    public static final String SWINGUD_CHANNEL = "swingUpDown";
    public static final String SWINGLR_CHANNEL = "swingLeftRight";
    public static final String WINDSPEED_CHANNEL = "windspeed";
    public static final String QUIET_CHANNEL = "quiet";
    public static final String AIR_CHANNEL = "air";
    public static final String DRY_CHANNEL = "dry";
    public static final String HEALTH_CHANNEL = "health";
    public static final String PWRSAV_CHANNEL = "powersave";

    // Mode channel
    public static final String MODE_AUTO = "auto";
    public static final String MODE_COOL = "cool";
    public static final String MODE_DRY = "dry";
    public static final String MODE_FAN = "fan";
    public static final String MODE_FAN2 = "fan-only";
    public static final String MODE_HEAT = "heat";
    public static final String MODE_ECO = "eco";
    public static final String MODE_ON = "on";
    public static final String MODE_OFF = "off";
    public static final int GREE_MODE_AUTO = 0;
    public static final int GREE_MODE_COOL = 1;
    public static final int GREE_MODE_DRY = 2;
    public static final int GREE_MODE_FAN = 3;
    public static final int GREE_MODE_HEAT = 4;

    // Quiet channel
    public static final String QUIET_OFF = "off";
    public static final String QUIET_AUTO = "auto";
    public static final String QUIET_QUIET = "quiet";
    public static final int GREE_QUIET_OFF = 0;
    public static final int GREE_QUIET_AUTO = 1;
    public static final int GREE_QUIET_QUIET = 2;

    // UDPPort used to communicate using UDP with GREE Airconditioners. .
    public static final String VENDOR_GREE = "gree";
    public static final int GREE_PORT = 7000;

    public static final String GREE_CID = "app";
    public static final String GREE_CMDT_BIND = "bind";
    public static final String GREE_CMDT_SCAN = "scan";
    public static final String GREE_CMDT_STATUS = "status";
    public static final String GREE_CMDT_CMD = "cmd";
    public static final String GREE_CMDT_PACK = "pack";

    public static final String GREE_CMD_OPT_NAME = "name"; // unit name
    public static final String GREE_CMD_OPT_HOST = "host"; // remote host (cloud)

    /*
     * Note : Values can be:
     * "Pow": Power (0 or 1)
     * "Mod": Mode: Auto: 0, Cool: 1, Dry: 2, Fan: 3, Heat: 4
     * "SetTem": Requested Temperature
     * "WdSpd": Fan Speed : Low:1, Medium Low:2, Medium :3, Medium High :4, High :5
     * "Air": Air Mode Enabled
     * "Blo": Dry
     * "Health": Health
     * "SwhSlp": Sleep
     * "SlpMod": ???
     * "Lig": Light On
     * "SwingLfRig": Swing Left Right
     * "SwUpDn": Swing Up Down: // Ceiling:0, Upwards : 10, Downwards : 11, Full range : 1
     * "Quiet": Quiet mode
     * "Tur": Turbo
     * "StHt": 0,
     * "TemUn": Temperature unit, 0 for Celsius, 1 for Fahrenheit
     * "HeatCoolType"
     * "TemRec": (0 or 1), Send with SetTem, when TemUn==1, distinguishes between upper and lower integer Fahrenheit
     * temp
     * "SvSt": Power Saving
     */
    public static final String GREE_PROP_POWER = "Pow";
    public static final String GREE_PROP_MODE = "Mod";
    public static final String GREE_PROP_SWINGUPDOWN = "SwUpDn";
    public static final String GREE_PROP_SWINGLEFTRIGHT = "SwingLfRig";
    public static final String GREE_PROP_WINDSPEED = "WdSpd";
    public static final String GREE_PROP_AIR = "Air";
    public static final String GREE_PROP_DRY = "Blo";
    public static final String GREE_PROP_TURBO = "Tur";
    public static final String GREE_PROP_QUIET = "Quiet";
    public static final String GREE_PROP_NOISE = "NoiseSet";
    public static final String GREE_PROP_LIGHT = "Lig";
    public static final String GREE_PROP_HEALTH = "Health";
    public static final String GREE_PROP_SLEEP = "SwhSlp";
    public static final String GREE_PROP_SLEEPMODE = "SlpMod";
    public static final String GREE_PROP_PWR_SAVING = "SvSt";
    public static final String GREE_PROP_SETTEMP = "SetTem";
    public static final String GREE_PROP_TEMPUNIT = "TemUn";
    public static final String GREE_PROP_TEMPREC = "TemRec";
    public static final String GREE_PROP_HEAT = "StHt";
    public static final String GREE_PROP_HEATCOOL = "HeatCoolType";
    public static final String GREE_PROP_NOISESET = "NoiseSet";
    public static final String GREE_PROP_CURRENT_TEMP_SENSOR = "TemSen";

    // Temperatur types and min/max ranges
    public static final int TEMP_UNIT_CELSIUS = 0;
    public static final int TEMP_UNIT_FAHRENHEIT = 1;
    public static final int TEMP_MIN_C = 5;
    public static final int TEMP_MAX_C = 30;
    public static final int TEMP_MIN_F = 41;
    public static final int TEMP_MAX_F = 86;
    public static final int TEMP_HALFSTEP_NO = 0;
    public static final int TEMP_HALFSTEP_YES = 1;

    /*
     * The timeout for the Datagram socket used to communicate with Gree Airconditioners.
     * This is particularly important when scanning for devices because this will effectively
     * be the amount of time spent scanning.
     */
    public static final int DATAGRAM_SOCKET_TIMEOUT = 5000; // regular read timeout
    public static final int DISCOVERY_TIMEOUT_MS = 7000; // do not change!!
    public static final int MAX_SCAN_CYCLES = 3;
    public static final int REFRESH_INTERVAL_SEC = 5;
    public static final int MAX_API_RETRIES = 3;

    public static final int DIGITS_TEMP = 1;

    /**
     * The internal offset for the temperature sensor which is set to a constant of -40 degrees Celsius. GREE
     * airconditioners usually return a value from the temperature sensor which is offset by +40 degrees Celsius. The
     * temperature value shown on the device LCD display should match the value shown by this binding when the config
     * parameter currentTemperatureOffset is set to 0.
     *
     * @See https://github.com/tomikaa87/gree-remote#getting-the-current-temperature-reading-from-the-internal-sensor
     *      for more details.
     */
    public static final double INTERNAL_TEMP_SENSOR_OFFSET = -40.0;
}
