/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 *
 */
public class BindingConstants {

    public static final String BINDING_ID = "avmfritz";
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_USER = "user";
    public static final String CONFIG_PASSWORD = "password";
    public static final String BRIDGE_FRITZBOX = "fritzbox";
    public static final String BRIDGE_MODEL_NAME = "FRITZ!Box";
    public static final String PL546E_MODEL_NAME = "FRITZ!Powerline";
    public static final String THING_AIN = "ain";

    // List of main device types
    public static final String DEVICE_DECT301 = "FRITZ_DECT_301";
    public static final String DEVICE_DECT300 = "FRITZ_DECT_300";
    public static final String DEVICE_DECT210 = "FRITZ_DECT_210";
    public static final String DEVICE_DECT200 = "FRITZ_DECT_200";
    public static final String DEVICE_DECT100 = "FRITZ_DECT_Repeater_100";
    public static final String DEVICE_PL546E = "FRITZ_Powerline_546E";
    public static final String DEVICE_PL546E_STANDALONE = "FRITZ_Powerline_546E_Solo";
    public static final String DEVICE_COMETDECT = "Comet_DECT";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_FRITZBOX);
    public static final ThingTypeUID DECT301_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT301);
    public static final ThingTypeUID DECT300_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT300);
    public static final ThingTypeUID DECT210_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT210);
    public static final ThingTypeUID DECT200_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT200);
    public static final ThingTypeUID DECT100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DECT100);
    public static final ThingTypeUID PL546E_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_PL546E);
    public static final ThingTypeUID PL546E_STANDALONE_THING_TYPE = new ThingTypeUID(BINDING_ID,
            DEVICE_PL546E_STANDALONE);
    public static final ThingTypeUID COMETDECT_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_COMETDECT);

    // List of all Channel ids
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_DEVICE_LOCKED = "device_locked";
    public static final String CHANNEL_TEMP = "temperature";
    public static final String CHANNEL_ENERGY = "energy";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_SWITCH = "outlet";
    public static final String CHANNEL_ACTUALTEMP = "actual_temp";
    public static final String CHANNEL_SETTEMP = "set_temp";
    public static final String CHANNEL_ECOTEMP = "eco_temp";
    public static final String CHANNEL_COMFORTTEMP = "comfort_temp";
    public static final String CHANNEL_RADIATOR_MODE = "radiator_mode";
    public static final String CHANNEL_NEXTCHANGE = "next_change";
    public static final String CHANNEL_NEXTTEMP = "next_temp";
    public static final String CHANNEL_BATTERY = "battery_low";

    // List of all Input tags
    public static final String INPUT_PRESENT = "present";
    public static final String INPUT_ACTUALTEMP = "tist";
    public static final String INPUT_SETTEMP = "tsoll";
    public static final String INPUT_ECOTEMP = "absenk";
    public static final String INPUT_COMFORTTEMP = "komfort";
    public static final String INPUT_NEXTCHANGE = "endperiod";
    public static final String INPUT_NEXTTEMP = "tchange";
    public static final String INPUT_BATTERY = "batterylow";

    // List of all Mode types
    public static final String MODE_AUTO = "AUTOMATIC";
    public static final String MODE_MANUAL = "MANUAL";
    public static final String MODE_ON = "ON";
    public static final String MODE_OFF = "OFF";
    public static final String MODE_COMFORT = "COMFORT";
    public static final String MODE_ECO = "ECO";
    public static final String MODE_BOOST = "BOOST";
    public static final String MODE_UNKNOWN = "UNKNOWN";

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Stream
            .of(DECT100_THING_TYPE, DECT200_THING_TYPE, DECT210_THING_TYPE, DECT300_THING_TYPE, DECT301_THING_TYPE,
                    PL546E_THING_TYPE, COMETDECT_THING_TYPE)
            .collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Stream
            .of(BRIDGE_THING_TYPE, PL546E_STANDALONE_THING_TYPE).collect(Collectors.toSet());

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(), SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());
}
