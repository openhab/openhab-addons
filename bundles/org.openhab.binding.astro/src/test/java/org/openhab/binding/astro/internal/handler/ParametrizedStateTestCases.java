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
package org.openhab.binding.astro.internal.handler;

import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.CommonTestConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * Test cases used in the {@link StateTest}
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valakanov - Added test data from
 *         <a href="http://www.suncalc.net">http://www.suncalc.net</a> and
 *         <a href="http://www.mooncalc.org">http://www.mooncalc.org</a>
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Introduced UoM and migrated tests to pure Java
 */
@NonNullByDefault
public final class ParametrizedStateTestCases {

    public static final Instant LEAP_DAY_2016_SA = Instant.parse("2016-02-29T03:00:00Z");
    public static final ZoneId BAGHDAD_ZONE = ZoneId.of("Asia/Baghdad");
    public static final Location SHAYBAH_LOC = new Location(22.4343, 54.3225);
    public static final Instant MARCH_16_2022_UA = Instant.parse("2022-03-16T08:04:53Z");
    public static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");
    public static final Location MARIUPOL_LOC = new Location(47.096059749317625, 37.548728017918116);

    public record Location(double latitude, double longitude) {

        @Override
        public final String toString() {
            return Double.toString(latitude) + ',' + Double.toString(longitude);
        }
    }

    public record StateTestCase(String thingId, String channelId, Instant testMoment, State expectedState,
            ZoneId timeZone, Location location) {
    }

    public final List<StateTestCase> cases;

    public ParametrizedStateTestCases() {
        cases = List.of(
                new StateTestCase(TEST_SUN_THING_ID, "rise#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T05:46:07+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "rise#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T05:48:26+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "rise#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(2.32, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "set#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T17:24:56+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "set#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T17:27:15+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "set#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(2.32, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "noon#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T11:36:41+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "noon#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T11:37:41+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "noon#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(1, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "night#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T18:41:51+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "night#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-03-01T04:30:44+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "night#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(589, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "morningNight#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T00:00:00+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "morningNight#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T04:31:31+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "morningNight#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(272, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "astroDawn#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T04:31:31+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "astroDawn#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T04:57:31+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "astroDawn#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(26, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "nauticDawn#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T04:57:31+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "nauticDawn#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T05:23:34+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "nauticDawn#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(26.1, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "civilDawn#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T05:23:34+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "civilDawn#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T05:46:07+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "civilDawn#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(22.5, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "astroDusk#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T18:15:52+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "astroDusk#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T18:41:51+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "astroDusk#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(26, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "nauticDusk#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T17:49:48+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "nauticDusk#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T18:15:52+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "nauticDusk#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(26.1, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "civilDusk#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T17:27:15+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "civilDusk#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T17:49:48+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "civilDusk#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(22.5, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "eveningNight#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T18:41:51+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "eveningNight#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-03-01T00:00:00+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "eveningNight#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(318, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "midnight#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T23:36:41+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "midnight#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T23:37:41+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "midnight#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(1, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "daylight#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T05:48:26+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "daylight#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T17:24:56+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, "daylight#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(696, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_MOON_THING_ID, "rise#start", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T23:00:00+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_MOON_THING_ID, "rise#end", LEAP_DAY_2016_SA,
                        new DateTimeType("2016-02-29T23:00:00+03:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_MOON_THING_ID, "rise#duration", LEAP_DAY_2016_SA,
                        new QuantityType<>(0, Units.MINUTE), BAGHDAD_ZONE, SHAYBAH_LOC),
                // Total Solar Eclipse on August 21, 2017
                new StateTestCase(TEST_SUN_THING_ID, "eclipse#total", Instant.parse("2017-01-01T00:00:00Z"),
                        new DateTimeType("2017-08-21T18:27:10+00:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                // Partial Solar Eclipse on August 11, 2018
                new StateTestCase(TEST_SUN_THING_ID, "eclipse#partial", Instant.parse("2018-07-15T00:00:00Z"),
                        new DateTimeType("2018-08-11T11:47:57+02:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                // Ring Solar Eclipse on December 26, 2019
                new StateTestCase(TEST_SUN_THING_ID, "eclipse#ring", Instant.parse("2019-11-01T00:00:00Z"),
                        new DateTimeType("2019-12-26T06:19:23+01:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                // Total Lunar Eclipse on January 21, 2019
                new StateTestCase(TEST_MOON_THING_ID, "eclipse#total", Instant.parse("2018-12-01T00:00:00Z"),
                        new DateTimeType("2019-01-21T05:13:02+00:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                // Partial Lunar Eclipse on July 16, 2019
                new StateTestCase(TEST_MOON_THING_ID, "eclipse#partial", Instant.parse("2019-06-01T00:00:00Z"),
                        new DateTimeType("2019-07-16T23:31:59+02:00"), BAGHDAD_ZONE, SHAYBAH_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS, MARCH_16_2022_UA,
                        new PercentType(93), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE, MARCH_16_2022_UA,
                        new QuantityType<>(5290, Units.KELVIN), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.minus(2, ChronoUnit.HOURS), new PercentType(64), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.minus(2, ChronoUnit.HOURS), new QuantityType<>(4410, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.minus(4, ChronoUnit.HOURS), new PercentType(12), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.minus(4, ChronoUnit.HOURS), new QuantityType<>(2860, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.minus(5, ChronoUnit.HOURS), new PercentType(0), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.minus(5, ChronoUnit.HOURS), new QuantityType<>(2500, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.plus(2, ChronoUnit.HOURS), new PercentType(100), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.plus(2, ChronoUnit.HOURS), new QuantityType<>(5490, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.plus(4, ChronoUnit.HOURS), new PercentType(83), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.plus(4, ChronoUnit.HOURS), new QuantityType<>(5000, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.plus(6, ChronoUnit.HOURS), new PercentType(44), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.plus(6, ChronoUnit.HOURS), new QuantityType<>(3820, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.plus(8, ChronoUnit.HOURS), new PercentType(0), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.plus(8, ChronoUnit.HOURS), new QuantityType<>(2500, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_BRIGHTNESS,
                        MARCH_16_2022_UA.plus(14, ChronoUnit.HOURS), new PercentType(0), KYIV_ZONE, MARIUPOL_LOC),
                new StateTestCase(TEST_SUN_THING_ID, CHANNEL_ID_SUN_CIRCADIAN_TEMPERATURE,
                        MARCH_16_2022_UA.plus(14, ChronoUnit.HOURS), new QuantityType<>(2500, Units.KELVIN), KYIV_ZONE,
                        MARIUPOL_LOC));
    }

    public List<StateTestCase> getCases() {
        return cases;
    }
}
