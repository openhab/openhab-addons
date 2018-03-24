/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
    public static final String CHANNEL_AC_TEMP = "settemp";
    public static final String CHANNEL_INDOOR_TEMP = "indoortemp";
    public static final String CHANNEL_OUTDOOR_TEMP = "outdoortemp";
    public static final String CHANNEL_AC_POWER = "power";
    public static final String CHANNEL_AC_MODE = "mode";
    public static final String CHANNEL_AC_FAN_SPEED = "fanspeed";
    public static final String CHANNEL_AC_FAN_DIR = "fandir";
    public static final String CHANNEL_HUMIDITY = "humidity";
    
    // Name for the setting that determines if we use Fahrenheit or Celsius 
    public static final String SETTING_USE_FAHRENHEIT = "useFahrenheit";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_AC_UNIT);
    }

}
