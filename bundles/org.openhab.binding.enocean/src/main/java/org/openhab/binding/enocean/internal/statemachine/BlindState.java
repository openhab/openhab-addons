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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * States for the blind/rollershutter state machine.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public enum BlindState {
    IDLE,
    MOVEMENT_POSITION_UP,
    MOVEMENT_POSITION_DOWN,
    MOVEMENT_SLATS,
    POSITION_REACHED,
    MOVEMENT_CALIBRATION_UP,
    MOVEMENT_CALIBRATION_DOWN,
    INVALID
}
