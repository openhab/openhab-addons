/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenDaikinBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Waterhouse <tim@timwaterhouse.com> - Initial contribution
 */
@NonNullByDefault
public class OpenDaikinBindingConstants {

    private static final String BINDING_ID = "opendaikin";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AC_UNIT = new ThingTypeUID(BINDING_ID, "ac_unit");

    // List of all Channel ids
    public static final String CHANNEL_AC_TEMPC = "settempc";
    public static final String CHANNEL_AC_TEMPF = "settempf";
    public static final String CHANNEL_INDOOR_TEMPC = "indoortempc";
    public static final String CHANNEL_INDOOR_TEMPF = "indoortempf";
    public static final String CHANNEL_OUTDOOR_TEMPC = "outdoortempc";
    public static final String CHANNEL_OUTDOOR_TEMPF = "outdoortempf";
    public static final String CHANNEL_AC_POWER = "power";
    public static final String CHANNEL_AC_MODE = "mode";
    public static final String CHANNEL_AC_FAN_SPEED = "fanspeed";
    public static final String CHANNEL_HUMIDITY = "humidity";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_AC_UNIT);
    }

}
