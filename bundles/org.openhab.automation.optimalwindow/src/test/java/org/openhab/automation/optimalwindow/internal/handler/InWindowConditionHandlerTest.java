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
package org.openhab.automation.optimalwindow.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.automation.optimalwindow.internal.OptimalWindowConstants;
import org.openhab.automation.optimalwindow.internal.calc.ForecastSource;
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
import org.openhab.automation.optimalwindow.internal.calc.WindowConfiguration;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.config.core.Configuration;

/**
 * Tests the {@link InWindowConditionHandler}.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class InWindowConditionHandlerTest {
    private static final ZoneId ZONE = ZoneId.of("GMT");
    private static final ZonedDateTime DAY = ZonedDateTime.of(2026, 10, 7, 0, 0, 0, 0, ZONE);

    private static final ForecastSource CHEAP_FROM_2_TO_4 = (item, service, begin, end) -> {
        SortedMap<Instant, Double> values = new TreeMap<>();
        for (int hour = 0; hour < 48; hour++) {
            values.put(DAY.plusHours(hour).toInstant(), hour >= 2 && hour < 4 ? 5.0 : 20.0);
        }
        return values;
    };

    private static boolean satisfiedAt(ForecastSource source, ZonedDateTime time) {
        return satisfiedAt(
                new WindowTracker(new WindowCalculator(source),
                        WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price", "length", "2h")))),
                time);
    }

    private static boolean satisfiedAt(WindowTracker tracker, ZonedDateTime time) {
        Condition condition = ModuleBuilder.createCondition().withId("1")
                .withTypeUID(OptimalWindowConstants.CONDITION_TYPE_ID)
                .withConfiguration(new Configuration(Map.of("forecastItem", "Price", "length", "2h"))).build();
        return new InWindowConditionHandler(condition, tracker, () -> ZONE, Clock.fixed(time.toInstant(), ZONE))
                .isSatisfied(Map.of());
    }

    @Test
    void satisfiedWithinWindow() {
        assertTrue(satisfiedAt(CHEAP_FROM_2_TO_4, DAY.plusHours(2)));
        assertTrue(satisfiedAt(CHEAP_FROM_2_TO_4, DAY.plusHours(3).plusMinutes(59)));
    }

    @Test
    void notSatisfiedOutsideWindow() {
        assertFalse(satisfiedAt(CHEAP_FROM_2_TO_4, DAY.plusHours(1)));
        assertFalse(satisfiedAt(CHEAP_FROM_2_TO_4, DAY.plusHours(4)));
    }

    @Test
    void startedWindowIsKeptWhenForecastChanges() {
        SortedMap<Instant, Double> cheapFrom10To12 = new TreeMap<>();
        for (int hour = 0; hour < 48; hour++) {
            cheapFrom10To12.put(DAY.plusHours(hour).toInstant(), hour >= 10 && hour < 12 ? 5.0 : 20.0);
        }
        AtomicBoolean changed = new AtomicBoolean();
        ForecastSource source = (item, service, begin, end) -> changed.get() ? cheapFrom10To12
                : CHEAP_FROM_2_TO_4.getValues(item, service, begin, end);
        WindowTracker tracker = new WindowTracker(new WindowCalculator(source),
                WindowConfiguration.from(new Configuration(Map.of("forecastItem", "Price", "length", "2h"))));

        assertTrue(satisfiedAt(tracker, DAY.plusHours(2)));
        // new forecast values make 10:00 - 12:00 cheaper, but the running window is kept like by the trigger
        changed.set(true);
        tracker.forecastUpdated();
        assertTrue(satisfiedAt(tracker, DAY.plusHours(3)));
    }

    @Test
    void notSatisfiedWithoutForecast() {
        assertFalse(satisfiedAt((item, service, begin, end) -> new TreeMap<>(), DAY.plusHours(2)));
        assertFalse(satisfiedAt((item, service, begin, end) -> {
            throw new IllegalStateException("persistence service missing");
        }, DAY.plusHours(2)));
    }
}
