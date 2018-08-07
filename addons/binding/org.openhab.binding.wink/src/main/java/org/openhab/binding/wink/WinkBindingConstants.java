/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WinkBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Sebastien Marchand - Initial contribution
 */
public class WinkBindingConstants {

    public static final String BINDING_ID = "wink";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WINK_HUB_2 = new ThingTypeUID(BINDING_ID, "wink_hub_2");
    public static final ThingTypeUID THING_TYPE_LIGHT_BULB = new ThingTypeUID(BINDING_ID, "light_bulb");
    public static final ThingTypeUID THING_TYPE_REMOTE = new ThingTypeUID(BINDING_ID, "remote");
    public static final ThingTypeUID THING_TYPE_BINARY_SWITCH = new ThingTypeUID(BINDING_ID, "binary_switch");
    public static final ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, "lock");
    public static final ThingTypeUID THING_TYPE_DOORBELL = new ThingTypeUID(BINDING_ID, "door_bell");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids for a light bulb
    public static final String CHANNEL_LIGHTLEVEL = "lightlevel";
    public static final String CHANNEL_LIGHTSTATE = "lightstate";
    public static final String CHANNEL_SWITCHSTATE = "switchstate";
    public static final String CHANNEL_LOCKSTATE = "lockstate";
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_BUTTON = "buttonpress";

    // List of Chanel ids for a thermostat
    public static final String CHANNEL_THERMOSTAT_ONLINE = "thermostat_online";
    public static final String CHANNEL_THERMOSTAT_CURRENTTEMPERATURE = "thermostat_currenttemperature";
    public static final String CHANNEL_THERMOSTAT_CURRENTSETPOINT = "thermostat_currentsetpoint";
    public static final String CHANNEL_THERMOSTAT_HUMIDITY = "thermostat_humidity";
    public static final String CHANNEL_THERMOSTAT_SMARTTEMPERATURE = "thermostat_smarttemperature";
    public static final String CHANNEL_THERMOSTAT_EXTERNALTEMPERATURE = "thermostat_externaltemperature";
    public static final String CHANNEL_THERMOSTAT_HASFAN = "thermostat_hasfan";
    public static final String CHANNEL_THERMOSTAT_ECOMODE = "thermostat_ecomode";
    public static final String CHANNEL_THERMOSTAT_TECHNAME = "thermostat_techname";
    public static final String CHANNEL_THERMOSTAT_TECHPHONE = "thermostat_techphone";
    public static final String CHANNEL_THERMOSTAT_CURRENTMODE = "thermostat_currentmode"; // heat_only, cool_only, auto
    public static final String CHANNEL_THERMOSTAT_RUNNINGMODE = "thermostat_runningmode"; // aux_active, cool_active,
                                                                                            // heat_active, idle
    public static final String CHANNEL_THERMOSTAT_FANACTIVE = "thermostat_fanactive";
    public static final String CHANNEL_THERMOSTAT_LASTERROR = "thermostat_lasterror";
    public static final String CHANNEL_THERMOSTAT_OCCUPIED = "thermostat_occupied";

    // REST URI constants
    public static final String WINK_URI = "https://api.wink.com/";
    public static final String WINK_DEVICES_REQUEST_PATH = "users/me/wink_devices";
    public static final String WINK_ACCESS_TOKEN = "access_token";
    public static final String WINK_REFRESH_TOKEN = "refresh_token";

    public static final String DELEGATED_AUTH_SERVICE = "https://openhab-authservice.herokuapp.com";
}
