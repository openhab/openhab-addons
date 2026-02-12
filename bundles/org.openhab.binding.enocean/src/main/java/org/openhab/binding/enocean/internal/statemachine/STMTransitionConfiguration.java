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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transition configuration for state machines.
 * <p>
 * Contains the list of valid transitions for a specific device type.
 *
 * @param <A> the action type (enum)
 * @param <S> the state type (enum)
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class STMTransitionConfiguration<A extends Enum<A>, S extends Enum<S>> {

    private final List<STMTransition<A, S>> transitions;

    public STMTransitionConfiguration(List<STMTransition<A, S>> transitions) {
        this.transitions = new ArrayList<>(transitions);
    }

    public List<STMTransition<A, S>> getTransitions() {
        return transitions;
    }

    /**
     * Predefined configurations for blind/rollershutter devices using FSB14 protocol.
     */
    public static class BlindTransitions {

        public static final STMTransitionConfiguration<BlindAction, BlindState> FSB14_BLINDS = new STMTransitionConfiguration<>(
                List.of(new STMTransition<>(BlindState.IDLE, BlindAction.POSITION_REQUEST_UP,
                        BlindState.MOVEMENT_POSITION_UP),
                        new STMTransition<>(BlindState.IDLE, BlindAction.POSITION_REQUEST_DOWN,
                                BlindState.MOVEMENT_POSITION_DOWN),
                        new STMTransition<>(BlindState.IDLE, BlindAction.SLATS_POS_REQUEST, BlindState.MOVEMENT_SLATS),
                        new STMTransition<>(BlindState.MOVEMENT_POSITION_UP, BlindAction.POSITION_DONE,
                                BlindState.POSITION_REACHED),
                        new STMTransition<>(BlindState.MOVEMENT_POSITION_DOWN, BlindAction.POSITION_DONE,
                                BlindState.POSITION_REACHED),
                        new STMTransition<>(BlindState.POSITION_REACHED, BlindAction.SLATS_POS_REQUEST,
                                BlindState.MOVEMENT_SLATS),
                        new STMTransition<>(BlindState.MOVEMENT_SLATS, BlindAction.SLATS_POS_DONE, BlindState.IDLE),
                        new STMTransition<>(BlindState.POSITION_REACHED, BlindAction.SLATS_POS_DONE, BlindState.IDLE),
                        new STMTransition<>(BlindState.INVALID, BlindAction.CALIBRATION_REQUEST_UP,
                                BlindState.MOVEMENT_CALIBRATION_UP),
                        new STMTransition<>(BlindState.INVALID, BlindAction.CALIBRATION_REQUEST_DOWN,
                                BlindState.MOVEMENT_CALIBRATION_DOWN),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_UP, BlindAction.CALIBRATION_DONE,
                                BlindState.IDLE),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_DOWN, BlindAction.CALIBRATION_DONE,
                                BlindState.IDLE),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_UP, BlindAction.INVALID_REQUEST,
                                BlindState.INVALID),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_DOWN, BlindAction.INVALID_REQUEST,
                                BlindState.INVALID),
                        new STMTransition<>(BlindState.MOVEMENT_SLATS, BlindAction.INVALID_REQUEST,
                                BlindState.INVALID)));

        public static final STMTransitionConfiguration<BlindAction, BlindState> FSB14_ROLLERSHUTTER = new STMTransitionConfiguration<>(
                List.of(new STMTransition<>(BlindState.IDLE, BlindAction.POSITION_REQUEST_UP,
                        BlindState.MOVEMENT_POSITION_UP),
                        new STMTransition<>(BlindState.IDLE, BlindAction.POSITION_REQUEST_DOWN,
                                BlindState.MOVEMENT_POSITION_DOWN),
                        new STMTransition<>(BlindState.MOVEMENT_POSITION_UP, BlindAction.POSITION_DONE,
                                BlindState.IDLE),
                        new STMTransition<>(BlindState.MOVEMENT_POSITION_DOWN, BlindAction.POSITION_DONE,
                                BlindState.IDLE),
                        new STMTransition<>(BlindState.INVALID, BlindAction.CALIBRATION_REQUEST_UP,
                                BlindState.MOVEMENT_CALIBRATION_UP),
                        new STMTransition<>(BlindState.INVALID, BlindAction.CALIBRATION_REQUEST_DOWN,
                                BlindState.MOVEMENT_CALIBRATION_DOWN),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_UP, BlindAction.CALIBRATION_DONE,
                                BlindState.IDLE),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_DOWN, BlindAction.CALIBRATION_DONE,
                                BlindState.IDLE),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_UP, BlindAction.INVALID_REQUEST,
                                BlindState.INVALID),
                        new STMTransition<>(BlindState.MOVEMENT_CALIBRATION_DOWN, BlindAction.INVALID_REQUEST,
                                BlindState.INVALID)));

        private BlindTransitions() {
            // Utility class
        }
    }
}
