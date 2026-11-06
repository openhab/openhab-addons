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
package org.openhab.binding.atv.internal.client.protocols.companion;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Companion protocol frame type values.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum FrameType {
    Unknown(0),
    NoOp(1),
    PS_Start(3),
    PS_Next(4),
    PV_Start(5),
    PV_Next(6),
    U_OPACK(7),
    E_OPACK(8),
    P_OPACK(9),
    PA_Req(10),
    PA_Rsp(11),
    SessionStartRequest(16),
    SessionStartResponse(17),
    SessionData(18),
    FamilyIdentityRequest(32),
    FamilyIdentityResponse(33),
    FamilyIdentityUpdate(34);

    private final int value;

    FrameType(int value) {
        this.value = value;
    }

    /** Returns the numeric on-wire value. */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching an on-wire value.
     *
     * @param value the frame type byte value
     * @return the matching constant
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static FrameType fromValue(int value) {
        for (FrameType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown FrameType value: " + value);
    }

    /** Whether this is a pairing ({@code PS_*} or {@code PV_*}) frame. */
    public boolean isAuthFrame() {
        return this == PS_Start || this == PS_Next || this == PV_Start || this == PV_Next;
    }

    /** Whether this is an OPACK-carrying data frame. */
    public boolean isOpackFrame() {
        return this == U_OPACK || this == E_OPACK || this == P_OPACK;
    }
}
