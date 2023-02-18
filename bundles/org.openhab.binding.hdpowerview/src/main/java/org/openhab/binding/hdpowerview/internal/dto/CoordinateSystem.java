/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Shade coordinate system (a.k.a. position kind), as returned by the HD PowerView hub.
 *
 * @param NONE a coordinate system that does not refer to any type of physical rail.
 * @param PRIMARY_POSITION primary rail, whose coordinate value 0 means shade is closed.
 * @param SECONDARY_POSITION secondary rail, whose coordinate value 0 means shade is open.
 * @param VANE_TILT_POSITION vane/tilt operator, whose coordinate system is for vanes.
 * @param ERROR_UNKNOWN unsupported coordinate system.
 *
 * @author Andy Lintner - Initial contribution of the original enum called
 *         ShadePositionKind
 *
 * @author Andrew Fiddian-Green - Rewritten as a new enum called
 *         CoordinateSystem to support secondary rail positions and be more
 *         explicit on coordinate directions and ranges
 */
@NonNullByDefault
public enum CoordinateSystem {
    /*-
     * Specifies the coordinate system used for the position of the shade. Top-down
     * shades are in the same coordinate space as bottom-up shades. Shade position
     * values for top-down shades would be reversed for bottom-up shades. For
     * example, since 65535 is the open value for a bottom-up shade, it is the
     * closed value for a top-down shade. The top-down/bottom-up shade is different
     * in that instead of the top and bottom rail operating in one coordinate space
     * like the top-down and the bottom-up, it operates in two where the top
     * (middle) rail closed value is 0 and the bottom (primary) rail closed position
     * is also 0 and fully open for both is 65535
     *
     * The position element can take on multiple states depending on the family of
     * shade under control.
     *
     * The ranges of position integer values are
     *      shades: 0..65535
     *      vanes: 0..32767
     *
     * Shade fully up: (top-down: open, bottom-up: closed)
     *      posKind: 1 {ZERO_IS_CLOSED}
     *      position: 65535
     *
     * Shade and vane fully down: (top-down: closed, bottom-up: open)
     *      posKind: 1 {ZERO_IS_CLOSED}
     *      position1: 0
     *
     * ALTERNATE: Shade and vane fully down: (top-down: closed, bottom-up: open)
     *      posKind: 3 {VANE_COORDS}
     *      position: 0
     *
     * Shade fully down (closed) and vane fully up (open):
     *      posKind: 3 {VANE_COORDS}
     *      position: 32767
     *
     * Dual action, secondary top-down shade fully up (closed):
     *      posKind: 2 {ZERO_IS_OPEN}
     *      position: 0
     *
     * Dual action, secondary top-down shade fully down (open):
     *      posKind: 2 {ZERO_IS_OPEN}
     *      position: 65535
     *
     */
    NONE,
    PRIMARY_POSITION,
    SECONDARY_POSITION,
    VANE_TILT_POSITION,
    ERROR_UNKNOWN;

    public static final int MAX_SHADE = 65535;
    public static final int MAX_VANE = 32767;

    /**
     * Converts an HD PowerView posKind integer value to a CoordinateSystem enum value.
     *
     * @param posKind input integer value.
     * @return corresponding CoordinateSystem enum.
     */
    public static CoordinateSystem fromPosKind(int posKind) {
        try {
            return CoordinateSystem.values()[posKind];
        } catch (ArrayIndexOutOfBoundsException e) {
            return ERROR_UNKNOWN;
        }
    }

    /**
     * Check if the coordinate system matches the given posKind.
     *
     * @param posKind
     * @return true if equal.
     */
    public boolean equals(@Nullable Integer posKind) {
        return (posKind != null) && (posKind.intValue() == ordinal());
    }
}
