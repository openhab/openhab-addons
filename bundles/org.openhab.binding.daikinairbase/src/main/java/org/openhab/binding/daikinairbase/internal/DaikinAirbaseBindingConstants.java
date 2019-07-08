/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.daikinairbase.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DaikinAirbaseBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 * @author Paul Smedley <paul@smedley.id.au> - mods for Daikin Airbase
 */
@NonNullByDefault
public class DaikinAirbaseBindingConstants {

    private static final String BINDING_ID = "daikinairbase";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AC_UNIT = new ThingTypeUID(BINDING_ID, "ac_unit");

    // List of all Channel ids
    public static final String CHANNEL_AC_TEMP = "settemp";
    public static final String CHANNEL_INDOOR_TEMP = "indoortemp";
    public static final String CHANNEL_OUTDOOR_TEMP = "outdoortemp";
    public static final String CHANNEL_AC_POWER = "power";
    public static final String CHANNEL_AC_MODE = "mode";
    public static final String CHANNEL_AC_FAN_SPEED = "fanspeed";
    public static final String CHANNEL_AC_ZONE1 = "zone1";
    public static final String CHANNEL_AC_ZONE2 = "zone2";
    public static final String CHANNEL_AC_ZONE3 = "zone3";
    public static final String CHANNEL_AC_ZONE4 = "zone4";
    public static final String CHANNEL_AC_ZONE5 = "zone5";
    public static final String CHANNEL_AC_ZONE6 = "zone6";
    public static final String CHANNEL_AC_ZONE7 = "zone7";
    public static final String CHANNEL_AC_ZONE8 = "zone8";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_AC_UNIT);
}
