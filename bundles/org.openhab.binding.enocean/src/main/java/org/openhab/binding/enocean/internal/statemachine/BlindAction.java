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
 * Actions that trigger state transitions in the blind/rollershutter state machine.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public enum BlindAction {
    POSITION_REQUEST_UP,
    POSITION_REQUEST_DOWN,
    SLATS_POS_REQUEST,
    POSITION_DONE,
    SLATS_POS_DONE,
    CALIBRATION_REQUEST_UP,
    CALIBRATION_REQUEST_DOWN,
    CALIBRATION_DONE,
    INVALID_REQUEST
}
