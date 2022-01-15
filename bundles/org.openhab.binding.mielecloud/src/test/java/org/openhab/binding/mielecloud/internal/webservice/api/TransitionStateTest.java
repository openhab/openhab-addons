/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class TransitionStateTest {
    private final DeviceState historic = mock(DeviceState.class);
    private final DeviceState previous = mock(DeviceState.class);
    private final DeviceState next = mock(DeviceState.class);

    @Test
    public void testHasFinishedChangedReturnsTrueWhenPreviousStateIsNull() {
        // given:
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        TransitionState transitionState = new TransitionState(null, next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertTrue(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsTrueWhenPreviousStateIsUnknown() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.empty());
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertTrue(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsFalseWhenNoStateTransitionOccurred() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertFalse(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsTrueWhenStateChangedFromRunningToEndProgrammed() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertTrue(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsTrueWhenStateChangedFromRunningToProgrammed() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertTrue(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsFalseWhenStateChangedFromRunningToPause() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getStateType()).thenReturn(Optional.of(StateType.PAUSE));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertFalse(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsTrueWhenStateChangedFromProgrammedWaitingToStartToRunning() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertTrue(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsFalseWhenStateRemainsProgrammedWaitingToStart() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));
        when(next.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertFalse(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsFalseWhenStateChangedFromPauseToRunning() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PAUSE));
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertFalse(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsTrueWhenStateChangedFromEndProgrammedToOff() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.OFF));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertTrue(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsFalseWhenStateChangedFromRunningToFailure() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getStateType()).thenReturn(Optional.of(StateType.FAILURE));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertFalse(hasFinishedChanged);
    }

    @Test
    public void testHasFinishedChangedReturnsFalseWhenStateChangedFromPauseToFailure() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PAUSE));
        when(next.getStateType()).thenReturn(Optional.of(StateType.FAILURE));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        boolean hasFinishedChanged = transitionState.hasFinishedChanged();

        // then:
        assertFalse(hasFinishedChanged);
    }

    @Test
    public void testIsFinishedReturnsTrueWhenStateChangedFromRunningToEndProgrammed() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Boolean isFinished = transitionState.isFinished().get();

        // then:
        assertTrue(isFinished);
    }

    @Test
    public void testIsFinishedReturnsTrueWhenStateChangedFromRunningToProgrammed() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Boolean isFinished = transitionState.isFinished().get();

        // then:
        assertTrue(isFinished);
    }

    @Test
    public void testIsFinishedReturnsFalseWhenStateChangedFromProgrammedWaitingToStartToRunning() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Boolean isFinished = transitionState.isFinished().get();

        // then:
        assertFalse(isFinished);
    }

    @Test
    public void testIsFinishedReturnsFalseWhenStateChangedFromRunningToFailure() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.FAILURE));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Boolean isFinished = transitionState.isFinished().get();

        // then:
        assertFalse(isFinished);
    }

    @Test
    public void testIsFinishedReturnsFalseWhenStateChangedFromPauseToFailure() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PAUSE));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.FAILURE));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Boolean isFinished = transitionState.isFinished().get();

        // then:
        assertFalse(isFinished);
    }

    @Test
    public void testIsFinishedReturnsTrueWhenStateChangedFromEndProgrammedToOff() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));
        when(previous.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.OFF));
        when(next.isInState(any())).thenCallRealMethod();

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Boolean isFinished = transitionState.isFinished().get();

        // then:
        assertFalse(isFinished);
    }

    @Test
    public void testIsFinishedReturnsNullWhenPreviousStateIsNull() {
        // given:
        when(next.getStateType()).thenReturn(Optional.of(StateType.IDLE));

        TransitionState transitionState = new TransitionState(null, next);

        // when:
        Optional<Boolean> isFinished = transitionState.isFinished();

        // then:
        assertFalse(isFinished.isPresent());
    }

    @Test
    public void testIsFinishedReturnsNullWhenPreviousStateIsUnknown() {
        // given:
        when(previous.getStateType()).thenReturn(Optional.empty());
        when(next.getStateType()).thenReturn(Optional.of(StateType.IDLE));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Optional<Boolean> isFinished = transitionState.isFinished();

        // then:
        assertFalse(isFinished.isPresent());
    }

    @Test
    public void testProgramStartedWithZeroRemainingTimeShowsNoRemainingTimeAndProgress() {
        // given:
        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(0));
        when(next.getProgress()).thenReturn(Optional.of(100));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        Optional<Integer> remainingTime = transitionState.getRemainingTime();
        Optional<Integer> progress = transitionState.getProgress();

        // then:
        assertFalse(remainingTime.isPresent());
        assertFalse(progress.isPresent());
    }

    @Test
    public void testProgramStartetdWithRemainingTimeShowsRemainingTimeAndProgress() {
        // given:
        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(2));
        when(next.getProgress()).thenReturn(Optional.of(50));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        int remainingTime = transitionState.getRemainingTime().get();
        int progress = transitionState.getProgress().get();

        // then:
        assertEquals(2, remainingTime);
        assertEquals(50, progress);
    }

    @Test
    public void testProgramCountingDownRemainingTimeToZeroShowsRemainingTimeAndProgress() {
        // given:
        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(previous.getRemainingTime()).thenReturn(Optional.of(1));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(0));
        when(next.getProgress()).thenReturn(Optional.of(100));

        TransitionState transitionState = new TransitionState(new TransitionState(null, previous), next);

        // when:
        int remainingTime = transitionState.getRemainingTime().get();
        int progress = transitionState.getProgress().get();

        // then:
        assertEquals(0, remainingTime);
        assertEquals(100, progress);
    }

    @Test
    public void testDevicePairedWhileRunningWithZeroRemainingTimeShowsNoRemainingTimeAndProgress() {
        // given:
        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(0));
        when(next.getProgress()).thenReturn(Optional.of(100));

        TransitionState transitionState = new TransitionState(null, next);

        // when:
        Optional<Integer> remainingTime = transitionState.getRemainingTime();
        Optional<Integer> progress = transitionState.getProgress();

        // then:
        assertFalse(remainingTime.isPresent());
        assertFalse(progress.isPresent());
    }

    @Test
    public void testDevicePairedWhileRunningWithRemainingTimeShowsRemainingTimeAndProgress() {
        // given:
        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(3));
        when(next.getProgress()).thenReturn(Optional.of(80));

        TransitionState transitionState = new TransitionState(null, next);

        // when:
        int remainingTime = transitionState.getRemainingTime().get();
        int progress = transitionState.getProgress().get();

        // then:
        assertEquals(3, remainingTime);
        assertEquals(80, progress);
    }

    @Test
    public void testWhenNoRemainingTimeIsSetWhileProgramIsRunningThenNoRemainingTimeAndProgressIsShown() {
        // given:
        when(historic.isInState(any())).thenCallRealMethod();
        when(historic.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));

        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(previous.getRemainingTime()).thenReturn(Optional.of(0));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(0));
        when(next.getProgress()).thenReturn(Optional.of(100));

        TransitionState transitionState = new TransitionState(
                new TransitionState(new TransitionState(null, historic), previous), next);

        // when:
        Optional<Integer> remainingTime = transitionState.getRemainingTime();
        Optional<Integer> progress = transitionState.getProgress();

        // then:
        assertFalse(remainingTime.isPresent());
        assertFalse(progress.isPresent());
    }

    @Test
    public void testRemainingTimeIsSetWhileRunningShowsRemainingTimeAndProgress() {
        // given:
        when(historic.isInState(any())).thenCallRealMethod();
        when(historic.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));

        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(previous.getRemainingTime()).thenReturn(Optional.of(0));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(100));
        when(next.getProgress()).thenReturn(Optional.of(10));

        TransitionState transitionState = new TransitionState(
                new TransitionState(new TransitionState(null, historic), previous), next);

        // when:
        int remainingTime = transitionState.getRemainingTime().get();
        int progress = transitionState.getProgress().get();

        // then:
        assertEquals(100, remainingTime);
        assertEquals(10, progress);
    }

    @Test
    public void testPreviousProgramDoesNotAffectHandlingOfRemainingTimeAndProgressForNextProgramCase1() {
        // given:
        DeviceState beforeHistoric = mock(DeviceState.class);
        when(beforeHistoric.isInState(any())).thenCallRealMethod();
        when(beforeHistoric.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(beforeHistoric.getRemainingTime()).thenReturn(Optional.of(1));

        when(historic.isInState(any())).thenCallRealMethod();
        when(historic.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));
        when(historic.getRemainingTime()).thenReturn(Optional.of(0));

        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));
        when(previous.getRemainingTime()).thenReturn(Optional.of(0));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(0));
        when(next.getProgress()).thenReturn(Optional.of(100));

        TransitionState transitionState = new TransitionState(
                new TransitionState(new TransitionState(new TransitionState(null, beforeHistoric), historic), previous),
                next);

        // when:
        Optional<Integer> remainingTime = transitionState.getRemainingTime();
        Optional<Integer> progress = transitionState.getProgress();

        // then:
        assertFalse(remainingTime.isPresent());
        assertFalse(progress.isPresent());
    }

    @Test
    public void testPreviousProgramDoesNotAffectHandlingOfRemainingTimeAndProgressForNextProgramCase2() {
        // given:
        DeviceState beforeHistoric = mock(DeviceState.class);
        when(beforeHistoric.isInState(any())).thenCallRealMethod();
        when(beforeHistoric.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(beforeHistoric.getRemainingTime()).thenReturn(Optional.of(1));

        when(historic.isInState(any())).thenCallRealMethod();
        when(historic.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));
        when(historic.getRemainingTime()).thenReturn(Optional.of(0));

        when(previous.isInState(any())).thenCallRealMethod();
        when(previous.getStateType()).thenReturn(Optional.of(StateType.PROGRAMMED_WAITING_TO_START));
        when(previous.getRemainingTime()).thenReturn(Optional.of(0));

        when(next.isInState(any())).thenCallRealMethod();
        when(next.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(next.getRemainingTime()).thenReturn(Optional.of(10));
        when(next.getProgress()).thenReturn(Optional.of(60));

        TransitionState transitionState = new TransitionState(
                new TransitionState(new TransitionState(new TransitionState(null, beforeHistoric), historic), previous),
                next);

        // when:
        int remainingTime = transitionState.getRemainingTime().get();
        int progress = transitionState.getProgress().get();

        // then:
        assertEquals(10, remainingTime);
        assertEquals(60, progress);
    }
}
