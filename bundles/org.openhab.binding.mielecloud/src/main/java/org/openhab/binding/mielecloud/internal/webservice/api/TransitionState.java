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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;

/**
 * This immutable class provides methods to extract the state information related to state transitions in a comfortable
 * way.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class TransitionState {
    private final boolean remainingTimeWasSetInCurrentProgram;
    private final Optional<DeviceState> previousState;
    private final DeviceState nextState;

    /**
     * Creates a new {@link TransitionState}.
     *
     * Note: {@code previousState} <b>must not</b> be saved in a field in this class as this will create a linked list
     * and cause memory issues. The constructor only serves the purpose of unpacking state that must be carried on.
     *
     * @param previousTransitionState The previous transition state if it exists.
     * @param nextState The device state which the device is transitioning to.
     */
    public TransitionState(@Nullable TransitionState previousTransitionState, DeviceState nextState) {
        this.remainingTimeWasSetInCurrentProgram = wasRemainingTimeSetInCurrentProgram(previousTransitionState,
                nextState);
        this.previousState = Optional.ofNullable(previousTransitionState).map(it -> it.nextState);
        this.nextState = nextState;
    }

    /**
     * Gets whether the finish state changed due to the transition form the previous to the current state.
     *
     * @return Whether the finish state changed due to the transition form the previous to the current state.
     */
    public boolean hasFinishedChanged() {
        return previousState.map(this::hasFinishedChangedFromPreviousState).orElse(true);
    }

    private boolean hasFinishedChangedFromPreviousState(DeviceState previous) {
        if (previous.getStateType().equals(nextState.getStateType())) {
            return false;
        }

        if (isInRunningState(previous) && nextState.isInState(StateType.FAILURE)) {
            return false;
        }

        if (isInRunningState(previous) != isInRunningState(nextState)) {
            return true;
        }

        if (nextState.isInState(StateType.OFF)) {
            return true;
        }

        return false;
    }

    /**
     * Gets whether a program finished.
     *
     * @return Whether a program finished.
     */
    public Optional<Boolean> isFinished() {
        return previousState.flatMap(this::hasFinishedFromPreviousState);
    }

    private Optional<Boolean> hasFinishedFromPreviousState(DeviceState prevState) {
        if (!prevState.getStateType().isPresent()) {
            return Optional.empty();
        }

        if (nextState.isInState(StateType.OFF)) {
            return Optional.of(false);
        }

        if (nextState.isInState(StateType.FAILURE)) {
            return Optional.of(false);
        }

        return Optional.of(!isInRunningState(nextState));
    }

    /**
     * Gets the remaining time of the active program.
     *
     * Note: Tracking changes in the remaining time is a workaround for the Miele API not properly distinguishing
     * between "there is no remaining time set" and "the remaining time is zero". If the remaining time is zero when a
     * program is started then we assume that no timer was set / program with remaining time is active. This may be
     * changed later by the user which is detected by the remaining time changing from 0 to some larger value.
     *
     * @return The remaining time in seconds.
     */
    public Optional<Integer> getRemainingTime() {
        if (!remainingTimeWasSetInCurrentProgram && isInRunningState(nextState)) {
            return nextState.getRemainingTime().filter(it -> it != 0);
        } else {
            return nextState.getRemainingTime();
        }
    }

    /**
     * Gets the program progress.
     *
     * @return The progress of the active program in percent.
     */
    public Optional<Integer> getProgress() {
        if (getRemainingTime().isPresent()) {
            return nextState.getProgress();
        } else {
            return Optional.empty();
        }
    }

    private static boolean wasRemainingTimeSetInCurrentProgram(@Nullable TransitionState previousTransitionState,
            DeviceState nextState) {
        if (previousTransitionState != null && isInRunningState(previousTransitionState.nextState)) {
            return previousTransitionState.remainingTimeWasSetInCurrentProgram
                    || previousTransitionState.getRemainingTime().isPresent();
        } else {
            return false;
        }
    }

    private static boolean isInRunningState(DeviceState device) {
        return device.isInState(StateType.RUNNING) || device.isInState(StateType.PAUSE);
    }
}
