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
 *
 * @author Sven Schad - Initial contribution
 * 
 */
@NonNullByDefault
public enum STMTransitionConfiguration {

    BLINDS(new ArrayList<>(List.of(
            new STMTransition(STMState.IDLE, STMAction.POSITION_REQUEST_UP, STMState.MOVEMENT_POSITION_UP),
            new STMTransition(STMState.IDLE, STMAction.POSITION_REQUEST_DOWN, STMState.MOVEMENT_POSITION_DOWN),
            new STMTransition(STMState.IDLE, STMAction.SLATS_POS_REQUEST, STMState.MOVEMENT_SLATS),
            new STMTransition(STMState.MOVEMENT_POSITION_UP, STMAction.POSITION_DONE, STMState.POSITION_REACHED),
            new STMTransition(STMState.MOVEMENT_POSITION_DOWN, STMAction.POSITION_DONE, STMState.POSITION_REACHED),
            new STMTransition(STMState.POSITION_REACHED, STMAction.SLATS_POS_REQUEST, STMState.MOVEMENT_SLATS),
            new STMTransition(STMState.MOVEMENT_SLATS, STMAction.SLATS_POS_DONE, STMState.IDLE),
            new STMTransition(STMState.POSITION_REACHED, STMAction.SLATS_POS_DONE, STMState.IDLE),
            new STMTransition(STMState.INVALID, STMAction.CALIBRATION_REQUEST_UP, STMState.MOVEMENT_CALIBRATION_UP),
            new STMTransition(STMState.INVALID, STMAction.CALIBRATION_REQUEST_DOWN, STMState.MOVEMENT_CALIBRATION_DOWN),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_UP, STMAction.CALIBRATION_DONE, STMState.IDLE),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_DOWN, STMAction.CALIBRATION_DONE, STMState.IDLE),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_UP, STMAction.INVALID_REQUEST, STMState.INVALID),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_DOWN, STMAction.INVALID_REQUEST, STMState.INVALID),
            new STMTransition(STMState.MOVEMENT_SLATS, STMAction.INVALID_REQUEST, STMState.INVALID)))),
    ROLLERSHUTTER(new ArrayList<>(List.of(
            new STMTransition(STMState.IDLE, STMAction.POSITION_REQUEST_UP, STMState.MOVEMENT_POSITION_UP),
            new STMTransition(STMState.IDLE, STMAction.POSITION_REQUEST_DOWN, STMState.MOVEMENT_POSITION_DOWN),
            new STMTransition(STMState.MOVEMENT_POSITION_UP, STMAction.POSITION_DONE, STMState.IDLE),
            new STMTransition(STMState.MOVEMENT_POSITION_DOWN, STMAction.POSITION_DONE, STMState.IDLE),
            new STMTransition(STMState.INVALID, STMAction.CALIBRATION_REQUEST_UP, STMState.MOVEMENT_CALIBRATION_UP),
            new STMTransition(STMState.INVALID, STMAction.CALIBRATION_REQUEST_DOWN, STMState.MOVEMENT_CALIBRATION_DOWN),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_UP, STMAction.CALIBRATION_DONE, STMState.IDLE),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_DOWN, STMAction.CALIBRATION_DONE, STMState.IDLE),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_UP, STMAction.INVALID_REQUEST, STMState.INVALID),
            new STMTransition(STMState.MOVEMENT_CALIBRATION_DOWN, STMAction.INVALID_REQUEST, STMState.INVALID))));

    private List<STMTransition> transitions;

    STMTransitionConfiguration(ArrayList<STMTransition> transitions) {
        this.transitions = transitions;
    }

    public List<STMTransition> getTransitions() {
        return transitions;
    }
}
