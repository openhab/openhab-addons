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
package org.openhab.binding.enocean.internal.statemachine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.library.types.PercentType;

/**
 * Tests for the watchdog of {@link STMStateMachine}.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class STMStateMachineTest {

    private static final STMWatchdog<BlindAction, BlindState> WATCHDOG = new STMWatchdog<>(
            EnumSet.of(BlindState.MOVEMENT_POSITION_UP, BlindState.MOVEMENT_POSITION_DOWN),
            BlindAction.MOVEMENT_TIMEOUT, 1000);

    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;
    private @Mock @NonNullByDefault({}) ScheduledFuture<?> future;
    private final List<Runnable> scheduledTasks = new ArrayList<>();
    private final List<BlindState> timedOutStates = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            scheduledTasks.add(invocation.getArgument(0));
            return future;
        });
    }

    private STMStateMachine<BlindAction, BlindState> build(STMTransitionConfiguration<BlindAction, BlindState> config,
            BlindState initialState) {
        return STMStateMachine.build(config, initialState, scheduler, null).registerWatchdog(WATCHDOG,
                timedOutStates::add);
    }

    @Test
    void watchdogResetsStateMachineToInvalidWhenFeedbackIsMissing() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);

        stm.apply(BlindAction.POSITION_REQUEST_UP);
        assertThat(scheduledTasks, hasSize(1));

        scheduledTasks.get(0).run();

        assertThat(stm.getState(), is(BlindState.INVALID));
        assertThat(timedOutStates, contains(BlindState.MOVEMENT_POSITION_UP));
    }

    @Test
    void timeoutDiscardsCommandStoredForAbandonedMovement() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);

        stm.apply(BlindAction.POSITION_REQUEST_UP);
        stm.storeCommand("rollershutter", PercentType.valueOf("50"));

        scheduledTasks.get(0).run();

        assertThat(stm.getState(), is(BlindState.INVALID));
        assertThat(stm.getStoredCommand(), is(nullValue()));
        assertThat(stm.getStoredChannel(), is(nullValue()));
    }

    @Test
    void leavingSupervisedStateCancelsWatchdog() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);

        stm.apply(BlindAction.POSITION_REQUEST_DOWN);
        stm.apply(BlindAction.POSITION_DONE);

        verify(future).cancel(false);
        scheduledTasks.get(0).run();
        assertThat(stm.getState(), is(BlindState.POSITION_REACHED));
        assertThat(timedOutStates, is(empty()));
    }

    @Test
    void expiredTimerOfEarlierMovementDoesNotResetLaterMovement() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);

        stm.apply(BlindAction.POSITION_REQUEST_UP);
        stm.apply(BlindAction.POSITION_DONE);
        stm.apply(BlindAction.SLATS_POS_DONE);
        stm.apply(BlindAction.POSITION_REQUEST_UP);
        assertThat(scheduledTasks, hasSize(2));

        scheduledTasks.get(0).run();

        assertThat(stm.getState(), is(BlindState.MOVEMENT_POSITION_UP));
        assertThat(timedOutStates, is(empty()));
    }

    @Test
    void statesWithoutSupervisionAreNotScheduled() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);

        stm.apply(BlindAction.SLATS_POS_REQUEST);

        assertThat(stm.getState(), is(BlindState.MOVEMENT_SLATS));
        assertThat(scheduledTasks, is(empty()));
    }

    @Test
    void disposeCancelsWatchdog() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);

        stm.apply(BlindAction.POSITION_REQUEST_UP);
        stm.dispose();

        verify(future).cancel(false);
        scheduledTasks.get(0).run();
        assertThat(stm.getState(), is(BlindState.MOVEMENT_POSITION_UP));
    }

    @Test
    void timeoutLeadsToInvalidFromAllMovementStates() {
        List<BlindState> blindsStates = List.of(BlindState.MOVEMENT_POSITION_UP, BlindState.MOVEMENT_POSITION_DOWN,
                BlindState.MOVEMENT_SLATS, BlindState.MOVEMENT_CALIBRATION_UP, BlindState.MOVEMENT_CALIBRATION_DOWN,
                BlindState.MOVEMENT_MANUAL_UP, BlindState.MOVEMENT_MANUAL_DOWN);
        for (BlindState initialState : blindsStates) {
            STMStateMachine<BlindAction, BlindState> stm = STMStateMachine
                    .build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, initialState, scheduler, null);
            stm.apply(BlindAction.MOVEMENT_TIMEOUT);
            assertThat("FSB14_BLINDS from " + initialState, stm.getState(), is(BlindState.INVALID));
        }

        List<BlindState> rollershutterStates = List.of(BlindState.MOVEMENT_POSITION_UP,
                BlindState.MOVEMENT_POSITION_DOWN, BlindState.MOVEMENT_CALIBRATION_UP,
                BlindState.MOVEMENT_CALIBRATION_DOWN, BlindState.MOVEMENT_MANUAL_UP, BlindState.MOVEMENT_MANUAL_DOWN);
        for (BlindState initialState : rollershutterStates) {
            STMStateMachine<BlindAction, BlindState> stm = STMStateMachine.build(
                    STMTransitionConfiguration.BlindTransitions.FSB14_ROLLERSHUTTER, initialState, scheduler, null);
            stm.apply(BlindAction.MOVEMENT_TIMEOUT);
            assertThat("FSB14_ROLLERSHUTTER from " + initialState, stm.getState(), is(BlindState.INVALID));
        }
    }

    @Test
    void eachWatchdogUsesItsOwnTimeout() {
        STMStateMachine<BlindAction, BlindState> stm = build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                BlindState.IDLE);
        stm.registerWatchdog(
                new STMWatchdog<>(EnumSet.of(BlindState.MOVEMENT_MANUAL_UP), BlindAction.MOVEMENT_TIMEOUT, 5000),
                timedOutStates::add);

        stm.apply(BlindAction.POSITION_REQUEST_UP);
        verify(scheduler).schedule(any(Runnable.class), eq(1000L), eq(TimeUnit.MILLISECONDS));

        stm.apply(BlindAction.POSITION_INTERRUPTED);
        stm.apply(BlindAction.MANUAL_START_UP);
        verify(scheduler).schedule(any(Runnable.class), eq(5000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void manualMovementIsTrackedFromIdle() {
        for (STMTransitionConfiguration<BlindAction, BlindState> config : List.of(
                STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS,
                STMTransitionConfiguration.BlindTransitions.FSB14_ROLLERSHUTTER)) {
            STMStateMachine<BlindAction, BlindState> stm = STMStateMachine.build(config, BlindState.IDLE, scheduler,
                    null);
            stm.apply(BlindAction.MANUAL_START_UP);
            assertThat(stm.getState(), is(BlindState.MOVEMENT_MANUAL_UP));
            stm.apply(BlindAction.MANUAL_DONE);
            assertThat(stm.getState(), is(BlindState.IDLE));

            stm.apply(BlindAction.MANUAL_START_DOWN);
            assertThat(stm.getState(), is(BlindState.MOVEMENT_MANUAL_DOWN));
            stm.apply(BlindAction.MANUAL_DONE);
            assertThat(stm.getState(), is(BlindState.IDLE));
        }
    }

    @Test
    void manualMovementIsNotStartedWhileBusyOrUncalibrated() {
        for (BlindState busyState : List.of(BlindState.INVALID, BlindState.MOVEMENT_POSITION_UP,
                BlindState.MOVEMENT_SLATS, BlindState.POSITION_REACHED, BlindState.MOVEMENT_CALIBRATION_DOWN)) {
            STMStateMachine<BlindAction, BlindState> stm = STMStateMachine
                    .build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, busyState, scheduler, null);
            stm.apply(BlindAction.MANUAL_START_DOWN);
            assertThat("from " + busyState, stm.getState(), is(busyState));
        }
    }

    @Test
    void interruptedPositionMovementReturnsToIdle() {
        STMStateMachine<BlindAction, BlindState> stm = STMStateMachine.build(
                STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, BlindState.MOVEMENT_POSITION_UP, scheduler,
                null);

        stm.apply(BlindAction.POSITION_INTERRUPTED);

        assertThat(stm.getState(), is(BlindState.IDLE));
    }

    @Test
    void movementContextIsReplacedByNextMovement() {
        STMStateMachine<BlindAction, BlindState> stm = STMStateMachine
                .build(STMTransitionConfiguration.BlindTransitions.FSB14_BLINDS, BlindState.IDLE, scheduler, null);
        assertThat(stm.getExpectedRuntime(), is(0));

        stm.startMovement(81);
        stm.requestStop();
        assertThat(stm.getExpectedRuntime(), is(81));
        assertThat(stm.isStopRequested(), is(true));

        stm.startMovement(270);
        assertThat(stm.getExpectedRuntime(), is(270));
        assertThat(stm.isStopRequested(), is(false));
    }
}
