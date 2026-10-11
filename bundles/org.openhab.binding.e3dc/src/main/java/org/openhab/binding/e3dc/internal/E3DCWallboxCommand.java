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
package org.openhab.binding.e3dc.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The packed command the wallbox expects in {@code WB_REQ_SET_EXTERN}.
 *
 * <p>
 * The wallbox is not configured through individual parameters but through one fixed length byte array
 * that carries a complete command:
 *
 * <pre>
 * index 0  charging mode: 1 selects sun mode, 2 selects normal mode
 * index 1  maximum charge current in ampere, valid range 1 to 32
 * index 2  reserved
 * index 3  reserved
 * index 4  1 requests a change of the suspended state, otherwise 0
 * index 5  reserved
 * index 6  reserved
 * index 7  reserved
 * </pre>
 *
 * <p>
 * Because the array describes the complete state, a command for one field has to carry the current
 * values of the others, otherwise the wallbox would fall back to whatever the remaining bytes mean.
 * {@link #SUB_COMMANDS_PER_FRAME} is the limit the device accepts within one container.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCWallboxCommand(boolean sunMode, int maxCurrent, boolean toggleSuspended) {

    /** Length of the payload the device expects. */
    public static final int PAYLOAD_LENGTH = 8;

    /** The wallbox accepts only this many sub commands inside one sent container. */
    public static final int SUB_COMMANDS_PER_FRAME = 6;

    /** Smallest charging current the wallbox accepts, in ampere. */
    public static final int MIN_CURRENT = 1;

    /** Largest charging current the wallbox accepts, in ampere. */
    public static final int MAX_CURRENT = 32;

    /** Marker for sun mode in byte 0. */
    private static final byte MODE_SUN = 1;

    /** Marker for normal mode in byte 0. */
    private static final byte MODE_NORMAL = 2;

    /** Returns a copy with the given charging current, clamped to the range the device accepts. */
    public E3DCWallboxCommand withMaxCurrent(int current) {
        return new E3DCWallboxCommand(sunMode, clampCurrent(current), toggleSuspended);
    }

    /** Returns a copy with the given charging mode. */
    public E3DCWallboxCommand withSunMode(boolean sun) {
        return new E3DCWallboxCommand(sun, maxCurrent, toggleSuspended);
    }

    /** Returns a copy that requests a change of the suspended state. */
    public E3DCWallboxCommand withToggle() {
        return new E3DCWallboxCommand(sunMode, maxCurrent, true);
    }

    /** Returns a copy with the pending toggle cleared, used once a command has been sent. */
    public E3DCWallboxCommand withToggleCleared() {
        return new E3DCWallboxCommand(sunMode, maxCurrent, false);
    }

    /** Clamps a charging current to the range the device accepts. */
    public static int clampCurrent(int current) {
        if (current < MIN_CURRENT) {
            return MIN_CURRENT;
        }
        if (current > MAX_CURRENT) {
            return MAX_CURRENT;
        }
        return current;
    }

    /** The payload as it is placed in {@code WB_EXTERN_DATA}. */
    public byte[] toPayload() {
        byte[] payload = new byte[PAYLOAD_LENGTH];
        Arrays.fill(payload, (byte) 0);
        payload[0] = sunMode ? MODE_SUN : MODE_NORMAL;
        payload[1] = (byte) clampCurrent(maxCurrent);
        payload[4] = (byte) (toggleSuspended ? 1 : 0);
        return payload;
    }
}
