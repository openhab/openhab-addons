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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.State;

/**
 * Unit test for {@link FlatLineProfile}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class FlatLineProfileTest {

    private @NonNullByDefault({}) @Mock ProfileCallback mockCallback;
    private @NonNullByDefault({}) @Mock ProfileContext mockContext;
    private @NonNullByDefault({}) @Mock ScheduledExecutorService mockScheduler;

    private FlatLineProfile initFlatLineProfile(String timeout, @Nullable String inverted) {
        Configuration config = new Configuration();
        config.put("timeout", timeout);
        if (inverted != null) {
            config.put("inverted", inverted);
        }

        reset(mockContext);
        reset(mockCallback);
        reset(mockScheduler);

        when(mockContext.getExecutorService()).thenReturn(mockScheduler);
        when(mockContext.getConfiguration()).thenReturn(config);

        return new FlatLineProfile(mockCallback, mockContext);
    }

    public static Stream<Arguments> testFlatLineProfile() {
        return Stream.of( //
                Arguments.of("500 ms", null, 500, OnOffType.OFF), //
                Arguments.of("500 ms", "false", 500, OnOffType.OFF), //
                Arguments.of("0.5 s", "false", 500, OnOffType.OFF), //
                Arguments.of("1 h", "true", 3600000, OnOffType.ON));
    }

    @ParameterizedTest
    @MethodSource
    public void testFlatLineProfile(String timeout, String inverted, long expectedMilliSeconds, State expectedState) {
        FlatLineProfile profile = initFlatLineProfile(timeout, inverted);

        verify(mockScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(expectedMilliSeconds),
                eq(expectedMilliSeconds), eq(TimeUnit.MILLISECONDS));

        reset(mockScheduler);

        profile.onStateUpdateFromHandler(DecimalType.ZERO);

        verify(mockCallback, times(2)).sendUpdate(expectedState);
        verify(mockScheduler, times(1)).scheduleWithFixedDelay(any(Runnable.class), eq(expectedMilliSeconds),
                eq(expectedMilliSeconds), eq(TimeUnit.MILLISECONDS));

        reset(mockCallback);
        reset(mockScheduler);

        assertDoesNotThrow(() -> profile.close());
        profile.onStateUpdateFromHandler(DecimalType.ZERO);

        verify(mockCallback, times(1)).sendUpdate(expectedState);
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), eq(expectedMilliSeconds),
                eq(expectedMilliSeconds), eq(TimeUnit.MILLISECONDS));
    }
}
