/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NestBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
public class NestBindingConstants {

    public static final String BINDING_ID = "nest";

    /** The url to use to connect to nest with. */
    public final static String NEST_URL = "https://developer-api.nest.com/";

    /** The url to get the access token when talking to nest. */
    public final static String NEST_ACCESS_TOKEN_URL = "https://developer-api.nest.com/oauth2/access_token";

    /** The url to get set values on the thermostat when talking to nest. */
    public final static String NEST_THERMOSTAT_UPDATE_URL = "https://developer-api.nest.com/devices/thermostats/";

    /** The url to get set values on the camera when talking to nest. */
    public final static String NEST_CAMERA_UPDATE_URL = "https://developer-api.nest.com/devices/camera/";

    /** The url to get set values on the camera when talking to nest. */
    public final static String NEST_SMOKE_ALARM_UPDATE_URL = "https://developer-api.nest.com/devices/smoke_co_alarms/";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public final static ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");
    public final static ThingTypeUID THING_TYPE_SMOKE_DETECTOR = new ThingTypeUID(BINDING_ID, "smoke_detector");
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Channel ids
    // read/write channels (thermostat)
    public final static String CHANNEL_MODE = "mode";
    public final static String CHANNEL_LOCKED = "locked";
    public final static String CHANNEL_MAX_SET_POINT = "max_set_point";
    public final static String CHANNEL_MIN_SET_POINT = "min_set_point";
    public final static String CHANNEL_LOCKED_MAX_SET_POINT = "locked_max_set_point";
    public final static String CHANNEL_LOCKED_MIN_SET_POINT = "locked_min_set_point";

    // read only channels (thermostat)
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_PREVIOUS_MODE = "previous_mode";
    public final static String CHANNEL_CAN_HEAT = "can_heat";
    public final static String CHANNEL_CAN_COOL = "can_cool";
    public final static String CHANNEL_HAS_FAN = "has_fan";
    public final static String CHANNEL_HAS_LEAF = "has_leaf";
    public final static String CHANNEL_SUNLIGHT_CORRECTION_ENABLED = "sunlight_correction_enabled";
    public final static String CHANNEL_SUNLIGHT_CORRECTION_ACTIVE = "sunlight_correction_active";
    public final static String CHANNEL_TIME_TO_TARGET_MINS = "time_to_target_mins";

    // read/write channels (camera)

    // read/write channels (smoke detector)

    // Properties on things that are fixed.
    public final static String PROPERTY_ID = "deviceId";
}
