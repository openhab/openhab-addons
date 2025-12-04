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
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
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
    private final String testItemName = "testItem";
    private final ChannelUID testChannelUID = new ChannelUID("this:test:channel:uid");
    private ItemChannelLink testLink = new ItemChannelLink(testItemName, testChannelUID);

    private TimeweightedAverageStateProfile initTWAProfile(String duration, double delta) {
        Configuration config = new Configuration();
        config.put("duration", duration);
        if (delta > 0) {
            config.put("delta", delta);
        }

        reset(mockContext);
        reset(mockCallback);
        reset(mockScheduler);

        when(mockContext.getExecutorService()).thenReturn(mockScheduler);
        when(mockContext.getConfiguration()).thenReturn(config);
        testLink = new ItemChannelLink(testItemName, testChannelUID, config);
        when(mockCallback.getItemChannelLink()).thenReturn(testLink);

        return new TimeweightedAverageStateProfile(mockCallback, mockContext);
    }

    public static Stream<Arguments> testTWATimeframe() {
        return Stream.of( //
                Arguments.of("10s", 10 * 1000), //
                Arguments.of("", 60 * 1000), //
                Arguments.of(null, 60 * 1000), //
                Arguments.of("500ms", 500), //
                Arguments.of("7m", 7 * 60 * 1000));
    }

    @ParameterizedTest
    @MethodSource
    public void testTWATimeframe(String timeout, long expectedSchedule) {
        TimeweightedAverageStateProfile profile = initTWAProfile(timeout, 0);
        profile.onStateUpdateFromHandler(DecimalType.ZERO);
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(expectedSchedule), eq(TimeUnit.MILLISECONDS));
    }

    public static Stream<Arguments> testTWAAverages() {
        return Stream.of( //
                Arguments.of(List.of("2024-01-01T00:00:00Z", "2024-01-01T00:01:00Z"), List.of("750 W", "0 W"), 750.0), //
                Arguments.of(List.of("2024-01-01T00:00:00Z", "2024-01-01T00:00:30Z", "2024-01-01T00:01:00Z"),
                        List.of("750 W", "1000 W", "0 W"), 875.0), //
                Arguments.of(List.of("2024-01-01T00:00:00Z", "2024-01-01T00:00:20Z", "2024-01-01T00:00:40Z",
                        "2024-01-01T00:01:00Z"), List.of("300 W", "900 W", "600 W", "0 W"), 600.0) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testTWAAverages(List<String> timeStrings, List<String> stateStrings, double expectedAverage) {
        TimeweightedAverageStateProfile profile = initTWAProfile("10s", 0);
        TreeMap<Instant, State> testData = new TreeMap<>();
        for (int i = 0; i < timeStrings.size(); i++) {
            testData.put(Instant.parse(timeStrings.get(i)), QuantityType.valueOf(stateStrings.get(i)));
        }
        assertEquals(expectedAverage, profile.average(testData));
    }

    public static Stream<Arguments> testTWADelta() {
        return Stream.of( //
                Arguments.of(List.of("750 W", "1000 W", "900 W", "800 W"), 0), //
                Arguments.of(List.of("750 W", "1000 W", "900 W", "800 W", "1300 W"), 2), //
                Arguments.of(List.of("750 W", "1000 W", "900 W", "800 W", "300 W"), 2), //
                Arguments.of(
                        List.of("750 W", "1000 W", "900 W", "800 W", "1500 W", "1400 W", "1300 W", "1100 W", "500 W"),
                        4) //
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testTWADelta(List<String> stateStrings, int expectedCallbacks) {
        TimeweightedAverageStateProfile profile = initTWAProfile("1h", 500);
        for (String stateString : stateStrings) {
            profile.onStateUpdateFromHandler(QuantityType.valueOf(stateString));
        }
        verify(mockCallback, times(expectedCallbacks)).sendUpdate(any());
    }
}
