/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AstroBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Amit Kumar Mondal - Made non-Instantiable
 */
@NonNullByDefault
public final class AstroBindingConstants {

    /** Constructor */
    private AstroBindingConstants() {
        throw new IllegalAccessError("Non-instantiable");
    }

    public static final String BINDING_ID = "astro";

    private static final String SUN = "sun";
    private static final String MOON = "moon";
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

    // regular channelIds

    // Common to SUN and MOON
    public static final String CHANNEL_ID_POSITION_AZIMUTH = "position#azimuth";
    public static final String CHANNEL_ID_POSITION_ELEVATION = "position#elevation";
    public static final String CHANNEL_ID_PHASE_NAME = "phase#name";
    public static final String CHANNEL_ID_ZODIAC_SIGN = "zodiac#sign";
    public static final String CHANNEL_ID_RISE_START = "rise#start";
    public static final String CHANNEL_ID_RISE_END = "rise#end";
    public static final String CHANNEL_ID_RISE_DURATION = "rise#duration";
    public static final String CHANNEL_ID_SET_START = "set#start";
    public static final String CHANNEL_ID_SET_END = "set#end";
    public static final String CHANNEL_ID_SET_DURATION = "set#duration";
    public static final String CHANNEL_ID_ECLIPSE_TOTAL = "eclipse#total";
    public static final String CHANNEL_ID_ECLIPSE_TOTAL_ELEVATION = "eclipse#totalElevation";
    public static final String CHANNEL_ID_ECLIPSE_PARTIAL = "eclipse#partial";
    public static final String CHANNEL_ID_ECLIPSE_PARTIAL_ELEVATION = "eclipse#partialElevation";

    // Moon Specific channels
    public static final String CHANNEL_ID_MOON_PHASE_FIRST_QUARTER = "phase#firstQuarter";
    public static final String CHANNEL_ID_MOON_PHASE_THIRD_QUARTER = "phase#thirdQuarter";
    public static final String CHANNEL_ID_MOON_PHASE_FULL = "phase#full";
    public static final String CHANNEL_ID_MOON_PHASE_NEW = "phase#new";
    public static final String CHANNEL_ID_MOON_PHASE_AGE = "phase#age";
    public static final String CHANNEL_ID_MOON_PHASE_AGE_DEGREE = "phase#ageDegree";
    public static final String CHANNEL_ID_MOON_PHASE_AGE_PERCENT = "phase#agePercent";
    public static final String CHANNEL_ID_MOON_PHASE_ILLUMINATION = "phase#illumination";
    public static final String CHANNEL_ID_MOON_DISTANCE_DATE = "distance#date";
    public static final String CHANNEL_ID_MOON_DISTANCE_DISTANCE = "distance#distance";
    public static final String CHANNEL_ID_MOON_PERIGEE_DATE = "perigee#date";
    public static final String CHANNEL_ID_MOON_PERIGEE_DISTANCE = "perigee#distance";
    public static final String CHANNEL_ID_MOON_APOGEE_DATE = "apogee#date";
    public static final String CHANNEL_ID_MOON_APOGEE_DISTANCE = "apogee#distance";
    public static final String CHANNEL_ID_MOON_POSITION_SHADE_LENGTH = "position#shadeLength";

    // Sun specific channels
    public static final String CHANNEL_ID_SUN_NOON_START = "noon#start";
    public static final String CHANNEL_ID_SUN_NOON_END = "noon#end";
    public static final String CHANNEL_ID_SUN_NOON_DURATION = "noon#duration";
    public static final String CHANNEL_ID_SUN_NIGHT_START = "night#start";
    public static final String CHANNEL_ID_SUN_NIGHT_END = "night#end";
    public static final String CHANNEL_ID_SUN_NIGHT_DURATION = "night#duration";
    public static final String CHANNEL_ID_SUN_MIDNIGHT_START = "midnight#start";
    public static final String CHANNEL_ID_SUN_MIDNIGHT_END = "midnight#end";
    public static final String CHANNEL_ID_SUN_MIDNIGHT_DURATION = "midnight#duration";
    public static final String CHANNEL_ID_SUN_MORNING_NIGHT_START = "morningNight#start";
    public static final String CHANNEL_ID_SUN_MORNING_NIGHT_END = "morningNight#end";
    public static final String CHANNEL_ID_SUN_MORNING_NIGHT_DURATION = "morningNight#duration";
    public static final String CHANNEL_ID_SUN_ASTRO_DAWN_START = "astroDawn#start";
    public static final String CHANNEL_ID_SUN_ASTRO_DAWN_END = "astroDawn#end";
    public static final String CHANNEL_ID_SUN_ASTRO_DAWN_DURATION = "astroDawn#duration";
    public static final String CHANNEL_ID_SUN_NAUTIC_DAWN_START = "nauticDawn#start";
    public static final String CHANNEL_ID_SUN_NAUTIC_DAWN_END = "nauticDawn#end";
    public static final String CHANNEL_ID_SUN_NAUTIC_DAWN_DURATION = "nauticDawn#duration";
    public static final String CHANNEL_ID_SUN_CIVIL_DAWN_START = "civilDawn#start";
    public static final String CHANNEL_ID_SUN_CIVIL_DAWN_END = "civilDawn#end";
    public static final String CHANNEL_ID_SUN_CIVIL_DAWN_DURATION = "civilDawn#duration";
    public static final String CHANNEL_ID_SUN_ASTRO_DUSK_START = "astroDusk#start";
    public static final String CHANNEL_ID_SUN_ASTRO_DUSK_END = "astroDusk#end";
    public static final String CHANNEL_ID_SUN_ASTRO_DUSK_DURATION = "astroDusk#duration";
    public static final String CHANNEL_ID_SUN_NAUTIC_DUSK_START = "nauticDusk#start";
    public static final String CHANNEL_ID_SUN_NAUTIC_DUSK_END = "nauticDusk#end";
    public static final String CHANNEL_ID_SUN_NAUTIC_DUSK_DURATION = "nauticDusk#duration";
    public static final String CHANNEL_ID_SUN_CIVIL_DUSK_START = "civilDusk#start";
    public static final String CHANNEL_ID_SUN_CIVIL_DUSK_END = "civilDusk#end";
    public static final String CHANNEL_ID_SUN_CIVIL_DUSK_DURATION = "civilDusk#duration";
    public static final String CHANNEL_ID_SUN_EVENING_NIGHT_START = "eveningNight#start";
    public static final String CHANNEL_ID_SUN_EVENING_NIGHT_END = "eveningNight#end";
    public static final String CHANNEL_ID_SUN_EVENING_NIGHT_DURATION = "eveningNight#duration";
    public static final String CHANNEL_ID_SUN_DAYLIGHT_START = "daylight#start";
    public static final String CHANNEL_ID_SUN_DAYLIGHT_END = "daylight#end";
    public static final String CHANNEL_ID_SUN_DAYLIGHT_DURATION = "daylight#duration";
    public static final String CHANNEL_ID_SUN_POSITION_SHADE_LENGTH = "position#shadeLength";
    public static final String CHANNEL_ID_SUN_RADIATION_DIRECT = "radiation#direct";
    public static final String CHANNEL_ID_SUN_RADIATION_DIFFUSE = "radiation#diffuse";
    public static final String CHANNEL_ID_SUN_RADIATION_TOTAL = "radiation#total";
    public static final String CHANNEL_ID_SUN_ZODIAC_START = "zodiac#start";
    public static final String CHANNEL_ID_SUN_ZODIAC_END = "zodiac#end";
    public static final String CHANNEL_ID_SUN_SEASON_NAME = "season#name";
    public static final String CHANNEL_ID_SUN_SEASON_SPRING = "season#spring";
    public static final String CHANNEL_ID_SUN_SEASON_SUMMER = "season#summer";
    public static final String CHANNEL_ID_SUN_SEASON_AUTUMN = "season#autumn";
    public static final String CHANNEL_ID_SUN_SEASON_WINTER = "season#winter";
    public static final String CHANNEL_ID_SUN_SEASON_NEXT_NAME = "season#nextName";
    public static final String CHANNEL_ID_SUN_SEASON_TIME_LEFT = "season#timeLeft";
    public static final String CHANNEL_ID_SUN_ECLIPSE_RING = "eclipse#ring";
    public static final String CHANNEL_ID_SUN_ECLIPSE_RING_ELEVATION = "eclipse#ringElevation";
    public static final String CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS = "circadian#brightness";
    public static final String CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE = "circadian#temperature";

    // event channelIds
    public static final String EVENT_CHANNEL_ID_MOON_PHASE = "phase#event";
    public static final String EVENT_CHANNEL_ID_ECLIPSE = "eclipse#event";

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
    public static final String EVENT_CHANNEL_ID_MIDNIGHT = "midnight#event";
    public static final String EVENT_CHANNEL_ID_DAYLIGHT = "daylight#event";

    // job identifiers

    public static final String DAILY_JOB = "daily";
    public static final String POSITIONAL_JOB = "positional";
    public static final String PUBLISH_ZODIAC_JOB = "publishZodiac";
    public static final String PUBLISH_SEASON_JOB = "publishSeason";
}
