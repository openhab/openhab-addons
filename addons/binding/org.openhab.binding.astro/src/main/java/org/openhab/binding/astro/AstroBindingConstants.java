/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AstroBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class AstroBindingConstants {

    public static final String BINDING_ID = "astro";

    public static final String SUN = "sun";
    public static final String MOON = "moon";
    public static final String LOCAL = "local";

    // things
    public static final ThingTypeUID THING_TYPE_SUN = new ThingTypeUID(BINDING_ID, SUN);
    public static final ThingTypeUID THING_TYPE_MOON = new ThingTypeUID(BINDING_ID, MOON);

    // events
    public static final String EVENT_START = "START";
    public static final String EVENT_END = "END";

    public static final String EVENT_PHASE_FIRST_QUARTER = "FIRST_QUARTER";
    public static final String EVENT_PHASE_THIRD_QUARTER = "THIRD_QUARTER";
    public static final String EVENT_PHASE_FULL = "FULL";
    public static final String EVENT_PHASE_NEW = "NEW";

    public static final String EVENT_ECLIPSE_PARTIAL = "PARTIAL";
    public static final String EVENT_ECLIPSE_TOTAL = "TOTAL";
    public static final String EVENT_ECLIPSE_RING = "RING";

    public static final String EVENT_PERIGEE = "PERIGEE";
    public static final String EVENT_APOGEE = "APOGEE";

    // event channelIds
    public static final String EVENT_CHANNEL_ID_MOON_PHASE = "phase#event";
    public static final String EVENT_CHANNEL_ID_ECLIPSE = "eclipse#event";
    public static final String EVENT_CHANNEL_ID_PERIGEE = "perigee#event";
    public static final String EVENT_CHANNEL_ID_APOGEE = "apogee#event";

    public static final String EVENT_CHANNEL_ID_RISE = "rise#event";
    public static final String EVENT_CHANNEL_ID_SET = "set#event";
    public static final String EVENT_CHANNEL_ID_NOON = "noon#event";
    public static final String EVENT_CHANNEL_ID_NIGHT = "night#event";
    public static final String EVENT_CHANNEL_ID_MORNING_NIGHT = "morningNight#event";
    public static final String EVENT_CHANNEL_ID_ASTRO_DAWN = "astroDawn#event";
    public static final String EVENT_CHANNEL_ID_NAUTIC_DAWN = "nauticDawn#event";
    public static final String EVENT_CHANNEL_ID_CIVIL_DAWN = "civilDawn#event";
    public static final String EVENT_CHANNEL_ID_ASTRO_DUSK = "astroDusk#event";
    public static final String EVENT_CHANNEL_ID_NAUTIC_DUSK = "nauticDusk#event";
    public static final String EVENT_CHANNEL_ID_CIVIL_DUSK = "civilDusk#event";
    public static final String EVENT_CHANNEL_ID_EVENING_NIGHT = "eveningNight#event";
    public static final String EVENT_CHANNEL_ID_DAYLIGHT = "daylight#event";

    public static final String CHANNEL_ID_SUN_PHASE_NAME = "phase#name";
}
