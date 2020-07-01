/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Shade coordinate system, as returned by the HD Power View Hub
 * 
 * {@code ZERO_IS_CLOSED } coordinate value 0 means shade is closed
 * {@code ZERO_IS_OPEN } coordinate value 0 means shade is open
 * {@code VANE_COORDS } coordinate system for vanes
 * {@code ERROR_UNKNOWN } unsupported coordinate system
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public enum CoordinateSystem {
    /*-
     * Specifies the position of the shade. Top-down shades are in the same
     * coordinate space as bottom-up shades. Shade position values for top-down
     * shades would be reversed for bottom-up shades. For example, since 65535 is
     * the open value for a bottom-up shade, it is the closed value for a top-down
     * shade. The top-down/bottom-up shade is different in that instead of the top
     * and bottom rail operating in one coordinate space like the top-down and the
     * bottom-up, it operates in two where the top (middle) rail closed value is 0
     * and the bottom (primary) rail closed position is also 0 and fully open for
     * both is 65535
     * 
     * The position element can take on multiple states depending on the family of
     * shade under control.
     *
     * The position1 element will only ever show one type of posKind1: either 1 or
     * 3; this is because the shade cannot physically exist with both shade and vane
     * open (to any degree).
     *
     * Therefore one can make the assumption that if there is a non-zero position1
     * while posKind1 == 1, then position1 == 0 for the implied posKind1 == 3.
     * 
     * The ranges of position integer values are
     *      shades: 0..65535
     *      vanes: 0..32767  
     * 
     * Shade fully up: (top-down: open, bottom-up: closed)
     *    posKind1: 1
     *    position1: 65535
     *
     * Shade and vane fully down: (top-down: closed, bottom-up: open) 
     *    posKind1: 1
     *    position1: 0
     *    
     * ALTERNATE: Shade and vane fully down: (top-down: closed, bottom-up: open)
     *    posKind1: 3
     *    position1: 0
     *
     * Shade fully down (closed) and vane fully up (open):
     *    posKind1: 3
     *    position1: 32767
     */
    ZERO_IS_CLOSED,
    ZERO_IS_OPEN,
    VANE_COORDS,
    ERROR_UNKNOWN;

    public static final int MAX_SHADE = 65535;
    public static final int MAX_VANE = 32767;

    public static CoordinateSystem fromPosKind(int posKind) {
        switch (posKind) {
            case 1:
                return ZERO_IS_CLOSED;
            case 2:
                return ZERO_IS_OPEN;
            case 3:
                return VANE_COORDS;
        }
        return ERROR_UNKNOWN;
    }

    public int toPosKind() {
        return ordinal() + 1;
    }
}
