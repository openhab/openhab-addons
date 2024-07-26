/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.daikin.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DaikinBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Waterhouse - Initial contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 */
@NonNullByDefault
public class DaikinBindingConstants {

    public static final String BINDING_ID = "daikin";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AC_UNIT = new ThingTypeUID(BINDING_ID, "ac_unit");
    public static final ThingTypeUID THING_TYPE_AIRBASE_AC_UNIT = new ThingTypeUID(BINDING_ID, "airbase_ac_unit");

    // List of all Channel ids
    public static final String CHANNEL_AC_TEMP = "settemp";
    public static final String CHANNEL_INDOOR_TEMP = "indoortemp";
    public static final String CHANNEL_OUTDOOR_TEMP = "outdoortemp";
    public static final String CHANNEL_AC_POWER = "power";
    public static final String CHANNEL_AC_MODE = "mode";
    public static final String CHANNEL_AC_HOMEKITMODE = "homekitmode";
    public static final String CHANNEL_AC_FAN_SPEED = "fanspeed";
    public static final String CHANNEL_AC_FAN_DIR = "fandir";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_CMP_FREQ = "cmpfrequency";

    // Prefix and channel id format for energy - currentyear
    public static final String CHANNEL_ENERGY_HEATING_CURRENTYEAR = "energyheatingcurrentyear";
    public static final String CHANNEL_ENERGY_COOLING_CURRENTYEAR = "energycoolingcurrentyear";

    public static final String CHANNEL_ENERGY_HEATING_TODAY = "energyheatingtoday";
    public static final String CHANNEL_ENERGY_HEATING_THISWEEK = "energyheatingthisweek";
    public static final String CHANNEL_ENERGY_HEATING_LASTWEEK = "energyheatinglastweek";
    public static final String CHANNEL_ENERGY_COOLING_TODAY = "energycoolingtoday";
    public static final String CHANNEL_ENERGY_COOLING_THISWEEK = "energycoolingthisweek";
    public static final String CHANNEL_ENERGY_COOLING_LASTWEEK = "energycoolinglastweek";

    public static final String CHANNEL_ENERGY_STRING_FORMAT = "%s-%d";

    public static final String CHANNEL_AC_SPECIALMODE = "specialmode";
    public static final String CHANNEL_AC_STREAMER = "streamer";

    public static final String CHANNEL_AC_DEMAND_MODE = "demandcontrolmode";
    public static final String CHANNEL_AC_DEMAND_MAX_POWER = "demandcontrolmaxpower";
    public static final String CHANNEL_AC_DEMAND_SCHEDULE = "demandcontrolschedule";

    // additional channels for Airbase Controller
    public static final String CHANNEL_AIRBASE_AC_FAN_SPEED = "airbasefanspeed";
    public static final String CHANNEL_AIRBASE_AC_ZONE = "zone";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_AC_UNIT, THING_TYPE_AIRBASE_AC_UNIT).collect(Collectors.toSet()));
}
