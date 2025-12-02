/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.State;

/**
 * Unit test for {@link TimeweightedAverageStateProfile}.
 *
 * @author Bernd Weymann - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class TimeweightedAverageProfileTest {

    private @NonNullByDefault({}) @Mock ProfileCallback mockCallback;
    private @NonNullByDefault({}) @Mock ProfileContext mockContext;
    private @NonNullByDefault({}) @Mock ScheduledExecutorService mockScheduler;

    private TimeweightedAverageStateProfile initTWAProfile(String duration) {
        Configuration config = new Configuration();
        config.put("duration", duration);

        reset(mockContext);
        reset(mockCallback);
        reset(mockScheduler);

        when(mockContext.getExecutorService()).thenReturn(mockScheduler);
        when(mockContext.getConfiguration()).thenReturn(config);

        return new TimeweightedAverageStateProfile(mockCallback, mockContext);
    }

    public static Stream<Arguments> testTWAProfile() {
        return Stream.of( //
                Arguments.of("10s", 10000), //
                Arguments.of("", 60000), //
                Arguments.of(null, 60000), //
                Arguments.of("500ms", 500), //
                Arguments.of("7s", 7000));
    }

    @ParameterizedTest
    @MethodSource
    public void testTWAProfile(String timeout, long expectedSchedule) {
        TimeweightedAverageStateProfile profile = initTWAProfile(timeout);
        profile.onStateUpdateFromHandler(DecimalType.ZERO);
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(expectedSchedule), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testAverages() {
        TimeweightedAverageStateProfile profile = initTWAProfile("10s");
        TreeMap<Instant, State> testData = new TreeMap<>();
        Instant baseTime = Instant.parse("2024-01-01T00:00:00Z");
        testData.put(baseTime, QuantityType.valueOf("750 W"));
        testData.put(baseTime.plusSeconds(60), QuantityType.valueOf("0 W"));
        assertEquals(750.0, profile.average(testData), 0.1, "One value for 60s average should be 750W");

        testData.clear();
        testData.put(baseTime, QuantityType.valueOf("750 W"));
        testData.put(baseTime.plusSeconds(30), QuantityType.valueOf("1000 W"));
        testData.put(baseTime.plusSeconds(60), QuantityType.valueOf("0 W"));
        assertEquals(875.0, profile.average(testData), 0.1, "One value for 60s average should be 750W");

        testData.clear();
        testData.put(baseTime, QuantityType.valueOf("300 W"));
        testData.put(baseTime.plusSeconds(20), QuantityType.valueOf("900 W"));
        testData.put(baseTime.plusSeconds(40), QuantityType.valueOf("600 W"));
        testData.put(baseTime.plusSeconds(60), QuantityType.valueOf("0 W"));
        assertEquals(600.0, profile.average(testData), 0.1, "One value for 60s average should be 750W");
    }
}
