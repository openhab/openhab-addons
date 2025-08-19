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
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.State;

/**
 * Unit test for {@link InactivityProfile}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class InactivityProfileTest {

    private @NonNullByDefault({}) @Mock ProfileCallback mockCallback;
    private @NonNullByDefault({}) @Mock ProfileContext mockContext;
    private @NonNullByDefault({}) @Mock ScheduledExecutorService mockScheduler;
    private @NonNullByDefault({}) @Mock ItemChannelLinkRegistry mockLinkRegistry;

    private final String testItemName = "testItem";
    private final ChannelUID testChannelUID = new ChannelUID("this:test:channel:uid");
    private ItemChannelLink testLink = new ItemChannelLink(testItemName, testChannelUID);

    private InactivityProfile initInactivityProfile(String timeout, @Nullable Boolean inverted) {
        Configuration config = new Configuration();
        config.put("timeout", timeout);
        if (inverted != null) {
            config.put("inverted", inverted);
        }
        testLink = new ItemChannelLink(testItemName, testChannelUID, config);

        reset(mockContext);
        reset(mockCallback);
        reset(mockScheduler);
        reset(mockLinkRegistry);

        when(mockCallback.getItemChannelLink()).thenReturn(testLink);
        when(mockContext.getExecutorService()).thenReturn(mockScheduler);
        when(mockContext.getConfiguration()).thenReturn(config);

        return new InactivityProfile(mockCallback, mockContext, mockLinkRegistry);
    }

    public static Stream<Arguments> testInactivityProfile() {
        return Stream.of( //
                Arguments.of(null, null, 3600000, OnOffType.OFF), //
                Arguments.of("", null, 3600000, OnOffType.OFF), //
                Arguments.of("5", null, 5000, OnOffType.OFF), //
                Arguments.of("500ms", null, 500, OnOffType.OFF), //
                Arguments.of("500ms", false, 500, OnOffType.OFF), //
                Arguments.of("5s", false, 5000, OnOffType.OFF), //
                Arguments.of("1h", true, 3600000, OnOffType.ON));
    }

    @ParameterizedTest
    @MethodSource
    public void testInactivityProfile(String timeout, Boolean inverted, long expectedMilliSeconds,
            State expectedState) {
        InactivityProfile profile = initInactivityProfile(timeout, inverted);

        /*
         * test that if item and channel are linked the initial task is scheduled
         */
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(expectedMilliSeconds),
                eq(TimeUnit.MILLISECONDS));

        /*
         * test that if item and channel are linked the update is received and the task is re-scheduled
         */
        reset(mockCallback);
        reset(mockScheduler);

        profile.onStateUpdateFromHandler(DecimalType.ZERO);

        verify(mockCallback, times(1)).sendUpdate(expectedState);
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(expectedMilliSeconds),
                eq(TimeUnit.MILLISECONDS));

        /*
         * test that if item and channel are not linked the update is not received and the task is not re-scheduled
         */
        reset(mockCallback);
        reset(mockScheduler);

        profile.removed(testLink);
        profile.onStateUpdateFromHandler(DecimalType.ZERO);

        verify(mockCallback, never()).sendUpdate(any(State.class));
        verify(mockScheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }
}
