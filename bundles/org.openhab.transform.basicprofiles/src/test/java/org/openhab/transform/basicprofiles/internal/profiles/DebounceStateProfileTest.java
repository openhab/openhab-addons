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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;

/**
 * Unit test for {@link DebounceStateProfile}.
 *
 * @author Andreas Berger - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class DebounceStateProfileTest {

    private @Mock @NonNullByDefault({}) ProfileCallback mockCallback;
    private @Mock @NonNullByDefault({}) ProfileContext mockContext;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService mockScheduler;
    private @Mock @NonNullByDefault({}) ScheduledFuture<?> mockFuture;

    private StateProfile initProfile(int onDelay, int offDelay) {
        reset(mockContext, mockCallback, mockScheduler);
        when(mockContext.getExecutorService()).thenReturn(mockScheduler);
        when(mockContext.getConfiguration())
                .thenReturn(new Configuration(Map.of("onDelay", onDelay, "offDelay", offDelay)));
        lenient().doReturn(mockFuture).when(mockScheduler).schedule(any(Runnable.class), anyLong(),
                eq(TimeUnit.MILLISECONDS));
        return new DebounceStateProfile(mockCallback, mockContext);
    }

    private Runnable captureScheduled(int expectedDelay) {
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockScheduler).schedule(captor.capture(), eq((long) expectedDelay), eq(TimeUnit.MILLISECONDS));
        return captor.getValue();
    }

    @Test
    public void testNegativeOnDelayThrows() {
        assertThrows(IllegalArgumentException.class, () -> initProfile(-1, 0));
    }

    @Test
    public void testNegativeOffDelayThrows() {
        assertThrows(IllegalArgumentException.class, () -> initProfile(0, -1));
    }

    @Test
    public void testZeroDelayForwardsImmediately() {
        StateProfile profile = initProfile(0, 0);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        verify(mockCallback, times(1)).sendUpdate(OnOffType.ON);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        verify(mockCallback, times(1)).sendUpdate(OnOffType.OFF);
        verify(mockScheduler, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    public void testOffDelayHoldsThenFires() {
        StateProfile profile = initProfile(0, 5000);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        verify(mockCallback, times(1)).sendUpdate(OnOffType.ON);

        profile.onStateUpdateFromHandler(OnOffType.OFF);
        verify(mockCallback, never()).sendUpdate(OnOffType.OFF);
        Runnable job = captureScheduled(5000);

        job.run();
        verify(mockCallback, times(1)).sendUpdate(OnOffType.OFF);
    }

    @Test
    public void testReturningOnCancelsPendingOff() {
        StateProfile profile = initProfile(0, 5000);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(5000L), eq(TimeUnit.MILLISECONDS));

        profile.onStateUpdateFromHandler(OnOffType.ON);
        verify(mockFuture, times(1)).cancel(false);
        verify(mockCallback, times(1)).sendUpdate(OnOffType.ON);
        verify(mockCallback, never()).sendUpdate(OnOffType.OFF);
    }

    @Test
    public void testStaleCancelledJobDoesNotForward() {
        StateProfile profile = initProfile(0, 5000);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        Runnable staleOffJob = captureScheduled(5000);

        // ON returns and cancels the pending OFF before its timer effectively completes
        profile.onStateUpdateFromHandler(OnOffType.ON);

        // the stale OFF job runs late (it had already started waiting for the lock)
        staleOffJob.run();

        // it must NOT forward the stale OFF
        verify(mockCallback, never()).sendUpdate(OnOffType.OFF);
        verify(mockCallback, times(1)).sendUpdate(OnOffType.ON);
    }

    @Test
    public void testRepeatedOffDoesNotReschedule() {
        StateProfile profile = initProfile(0, 5000);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(5000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testOnDelayHoldsThenFires() {
        StateProfile profile = initProfile(5000, 0);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        verify(mockCallback, never()).sendUpdate(OnOffType.ON);
        Runnable job = captureScheduled(5000);
        job.run();
        verify(mockCallback, times(1)).sendUpdate(OnOffType.ON);
    }

    @Test
    public void testOnDelayCancelledByOff() {
        StateProfile profile = initProfile(5000, 0);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(5000L), eq(TimeUnit.MILLISECONDS));
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        verify(mockFuture, times(1)).cancel(false);
        verify(mockCallback, never()).sendUpdate(OnOffType.ON);
        verify(mockCallback, times(1)).sendUpdate(OnOffType.OFF);
    }

    @Test
    public void testContactMapping() {
        StateProfile profile = initProfile(0, 5000);
        profile.onStateUpdateFromHandler(OpenClosedType.OPEN);
        verify(mockCallback, times(1)).sendUpdate(OpenClosedType.OPEN);

        profile.onStateUpdateFromHandler(OpenClosedType.CLOSED);
        verify(mockCallback, never()).sendUpdate(OpenClosedType.CLOSED);
        Runnable job = captureScheduled(5000);
        job.run();
        verify(mockCallback, times(1)).sendUpdate(OpenClosedType.CLOSED);
    }

    @Test
    public void testCommandsFromHandlerForwardImmediately() {
        StateProfile profile = initProfile(5000, 5000);
        profile.onCommandFromHandler(OnOffType.ON);
        verify(mockCallback, times(1)).sendCommand(OnOffType.ON);
        profile.onCommandFromHandler(OnOffType.OFF);
        verify(mockCallback, times(1)).sendCommand(OnOffType.OFF);
        verify(mockScheduler, never()).schedule(any(Runnable.class), anyLong(), any());
    }

    @Test
    public void testCommandFromHandlerDoesNotAffectPendingStateUpdate() {
        StateProfile profile = initProfile(0, 5000);
        profile.onStateUpdateFromHandler(OnOffType.ON);
        profile.onStateUpdateFromHandler(OnOffType.OFF);
        Runnable job = captureScheduled(5000);

        // a command arriving while an OFF state update is pending must not interfere with the delay
        profile.onCommandFromHandler(OnOffType.ON);
        verify(mockCallback, times(1)).sendCommand(OnOffType.ON);
        verify(mockFuture, never()).cancel(anyBoolean());

        // the pending OFF still fires after its delay
        job.run();
        verify(mockCallback, times(1)).sendUpdate(OnOffType.OFF);
    }

    @Test
    public void testItemEventsPassThrough() {
        StateProfile profile = initProfile(0, 5000);
        profile.onCommandFromItem(OnOffType.ON);
        verify(mockCallback, times(1)).handleCommand(OnOffType.ON);
        profile.onStateUpdateFromItem(OnOffType.ON);
        verify(mockCallback, never()).sendUpdate(any());
    }
}
