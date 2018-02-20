/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bigassfan;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link BigAssFanBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class BigAssFanBindingConstants {

    public static final String BINDING_ID = "bigassfan";

    // Fans communicate on this port using both UDP (discovery) and TCP (commands)
    public static final int BAF_PORT = 31415;

    // Commands sent to/from fan are ASCII
    public static final String CHARSET = "US-ASCII";

    // BigAssFan Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FAN = new ThingTypeUID(BINDING_ID, "fan");
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_FAN,
            THING_TYPE_CONTROLLER);

    /*
     * List of Channel Ids
     */
    // Fan control channels
    public static final String CHANNEL_FAN_POWER = "fan-power";
    public static final String CHANNEL_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_FAN_DIRECTION = "fan-direction";
    public static final String CHANNEL_FAN_AUTO = "fan-auto";
    public static final String CHANNEL_FAN_WHOOSH = "fan-whoosh";
    public static final String CHANNEL_FAN_SMARTMODE = "fan-smartmode";
    public static final String CHANNEL_FAN_SPEED_MIN = "fan-speed-min";
    public static final String CHANNEL_FAN_SPEED_MAX = "fan-speed-max";
    public static final String CHANNEL_FAN_LEARN_MINSPEED = "fan-learn-speed-min";
    public static final String CHANNEL_FAN_LEARN_MAXSPEED = "fan-learn-speed-max";
    public static final String CHANNEL_FAN_WINTERMODE = "fan-wintermode";

    // Light control channels
    public static final String CHANNEL_LIGHT_POWER = "light-power";
    public static final String CHANNEL_LIGHT_LEVEL = "light-level";
    public static final String CHANNEL_LIGHT_AUTO = "light-auto";
    public static final String CHANNEL_LIGHT_SMARTER = "light-smarter";
    public static final String CHANNEL_LIGHT_LEVEL_MIN = "light-level-min";
    public static final String CHANNEL_LIGHT_LEVEL_MAX = "light-level-max";
    public static final String CHANNEL_LIGHT_PRESENT = "light-present";

    // Miscellaneous channels
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_TIME = "time";

    /*
     * BigAssFan thing configuration parameters
     */
    // IP network address of the fan
    public static final String THING_PROPERTY_IP = "ipAddress";

    // MAC address of the fan
    public static final String THING_PROPERTY_MAC = "macAddress";

    // Friendly name given to the fan
    public static final String THING_PROPERTY_LABEL = "label";
}
