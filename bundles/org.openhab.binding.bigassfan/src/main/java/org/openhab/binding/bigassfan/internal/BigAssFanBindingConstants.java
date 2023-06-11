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
package org.openhab.binding.bigassfan.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BigAssFanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class BigAssFanBindingConstants {

    public static final String BINDING_ID = "bigassfan";

    // Fans communicate on this port using both UDP (discovery) and TCP (commands)
    public static final int BAF_PORT = 31415;

    // BigAssFan Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FAN = new ThingTypeUID(BINDING_ID, "fan");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_FAN, THING_TYPE_LIGHT, THING_TYPE_CONTROLLER).collect(Collectors.toSet()));

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
    public static final String CHANNEL_FAN_SLEEP = "fan-sleep";

    // Light control channels
    public static final String CHANNEL_LIGHT_POWER = "light-power";
    public static final String CHANNEL_LIGHT_LEVEL = "light-level";
    public static final String CHANNEL_LIGHT_AUTO = "light-auto";
    public static final String CHANNEL_LIGHT_SMARTER = "light-smarter";
    public static final String CHANNEL_LIGHT_LEVEL_MIN = "light-level-min";
    public static final String CHANNEL_LIGHT_LEVEL_MAX = "light-level-max";
    public static final String CHANNEL_LIGHT_PRESENT = "light-present";

    // Standalone light channels
    public static final String CHANNEL_LIGHT_HUE = "light-hue";
    public static final String CHANNEL_LIGHT_COLOR = "light-color";

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
