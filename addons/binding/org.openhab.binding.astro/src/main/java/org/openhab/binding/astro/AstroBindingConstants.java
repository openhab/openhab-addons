/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro;

import java.util.HashMap;
import java.util.Map;

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
    public static final Map<String, String> MOON_EVENTS = new HashMap<String, String>();
    public static final Map<String, String> SUN_EVENTS = new HashMap<String, String>();

    public static final String EVENT_CHANNEL_ID = "event#event";

    static {
        MOON_EVENTS.put("rise#start", "RISE_START");
        MOON_EVENTS.put("set#end", "SET_END");
        MOON_EVENTS.put("phase#firstQuarter", "FIRST_QUARTER");
        MOON_EVENTS.put("phase#thirdQuarter", "THIRD_QUARTER");
        MOON_EVENTS.put("phase#full", "FULL");
        MOON_EVENTS.put("phase#new", "NEW");
        MOON_EVENTS.put("eclipse#total", "TOTAL_ECLIPSE");
        MOON_EVENTS.put("eclipse#partial", "PARTIAL_ECLIPSE");
        MOON_EVENTS.put("perigee#date", "PERIGEE");
        MOON_EVENTS.put("apogee#date", "APOGEE");

        SUN_EVENTS.put("rise#start", "RISE_START");
        SUN_EVENTS.put("rise#end", "RISE_END");
        SUN_EVENTS.put("set#start", "SET_START");
        SUN_EVENTS.put("set#end", "SET_END");
        SUN_EVENTS.put("noon#start", "NOON_START");
        SUN_EVENTS.put("noon#end", "NOON_END");
        SUN_EVENTS.put("night#start", "NIGHT_START");
        SUN_EVENTS.put("night#end", "NIGHT_END");
        SUN_EVENTS.put("morningNight#start", "MORNING_NIGHT_START");
        SUN_EVENTS.put("morningNight#end", "MORNING_NIGHT_END");
        SUN_EVENTS.put("astroDawn#start", "ASTRO_DAWN_START");
        SUN_EVENTS.put("astroDawn#end", "ASTRO_DAWN_END");
        SUN_EVENTS.put("nauticDawn#start", "NAUTIC_DAWN_START");
        SUN_EVENTS.put("nauticDawn#end", "NAUTIC_DAWN_END");
        SUN_EVENTS.put("civilDawn#start", "CIVIL_DAWN_START");
        SUN_EVENTS.put("civilDawn#end", "CIVIL_DAWN_END");
        SUN_EVENTS.put("astroDusk#start", "ASTRO_DUSK_START");
        SUN_EVENTS.put("astroDusk#end", "ASTRO_DUSK_END");
        SUN_EVENTS.put("nauticDusk#start", "NAUTIC_DUSK_START");
        SUN_EVENTS.put("nauticDusk#end", "NAUTIC_DUSK_END");
        SUN_EVENTS.put("civilDusk#start", "CIVIL_DUSK_START");
        SUN_EVENTS.put("civilDusk#end", "CIVIL_DUSK_END");
        SUN_EVENTS.put("eveningNight#start", "EVENING_NIGHT_START");
        SUN_EVENTS.put("eveningNight#end", "EVENING_NIGHT_END");
        SUN_EVENTS.put("daylight#start", "DAYLIGHT_START");
        SUN_EVENTS.put("daylight#end", "DAYLIGHT_END");
        SUN_EVENTS.put("eclipse#total", "TOTAL_ECLIPSE");
        SUN_EVENTS.put("eclipse#partial", "PARTIAL_ECLIPSE");
        SUN_EVENTS.put("eclipse#ring", "RING_ECLIPSE");
        SUN_EVENTS.put("zodiac#start", "ZODIAC_{}_START");
        SUN_EVENTS.put("zodiac#end", "ZODIAC_{}_END");
        SUN_EVENTS.put("season#spring", "SEASON_SPRING");
        SUN_EVENTS.put("season#summer", "SEASON_SUMMER");
        SUN_EVENTS.put("season#autumn", "SEASON_AUTUMN");
        SUN_EVENTS.put("season#winter", "SEASON_WINTER");
    }
}
