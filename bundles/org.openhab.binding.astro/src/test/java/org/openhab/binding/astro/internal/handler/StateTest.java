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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.internal.CommonTestConstants.*;
import static org.openhab.binding.astro.internal.handler.ParametrizedStateTestCases.*;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.InstantSource;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Tests for the Astro Channels state
 *
 * @see ParametrizedStateTestCases
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Reworked to plain unit tests
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Migrated tests to pure Java
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class StateTest {

    private static final Locale LOCALE = Locale.of("ar", "iq");

    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProvider;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) CronScheduler scheduler;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;
    private @Mock @NonNullByDefault({}) Channel channel;

    @BeforeEach
    public void init() {
        when(localeProvider.getLocale()).thenReturn(LOCALE);
    }

    public static List<StateTestCase> data() {
        ParametrizedStateTestCases cases = new ParametrizedStateTestCases();
        return cases.getCases();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void stateTest(StateTestCase testCase) throws Exception {
        when(timeZoneProvider.getTimeZone()).thenReturn(testCase.timeZone());
        Configuration c = new Configuration(
                Map.of("geolocation", testCase.location().toString(), INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE));
        when(thing.getConfiguration()).thenReturn(c);

        AstroThingHandler handler = getHandler(testCase.thingId(), testCase.testMoment());
        if (handler == null) {
            throw new IllegalStateException("handler should not be null");
        }

        when(channel.getUID()).thenReturn(new ChannelUID(getThingUID(testCase.thingId()), testCase.channelId()));
        when(channel.getConfiguration()).thenReturn(new Configuration());

        State state = handler.getState(channel);
        if (state instanceof DateTimeType dateTime) {
            // Truncate to second
            state = new DateTimeType(Instant.ofEpochMilli((dateTime.getInstant().toEpochMilli() / 1000L) * 1000L));
        } else if (state instanceof QuantityType quantity) {
            Unit<?> unit = quantity.getUnit();
            state = new QuantityType<>(quantity.toBigDecimal().round(new MathContext(3, RoundingMode.HALF_UP)), unit);
        }

        assertEquals(testCase.expectedState(), state);
        handler.dispose();
    }

    private ThingUID getThingUID(String thingID) {
        switch (thingID) {
            case (TEST_SUN_THING_ID):
                return new ThingUID(THING_TYPE_SUN, thingID);
            case (TEST_MOON_THING_ID):
                return new ThingUID(THING_TYPE_MOON, thingID);
            default:
                return new ThingUID("error");
        }
    }

    private @Nullable AstroThingHandler getHandler(String thingID, Instant testMoment) {
        InstantSource instantSource = InstantSource.fixed(testMoment);
        when(thing.getUID()).thenReturn(getThingUID(thingID));
        switch (thingID) {
            case (TEST_SUN_THING_ID):
                SunHandler sunHandler = new SunHandler(thing, scheduler, timeZoneProvider, localeProvider,
                        instantSource);
                sunHandler.initialize();
                sunHandler.publishDailyInfo();
                return sunHandler;
            case (TEST_MOON_THING_ID):
                MoonHandler moonHandler = new MoonHandler(thing, scheduler, timeZoneProvider, localeProvider,
                        instantSource);
                moonHandler.initialize();
                moonHandler.publishDailyInfo();
                return moonHandler;
            default:
                return null;
        }
    }
}
