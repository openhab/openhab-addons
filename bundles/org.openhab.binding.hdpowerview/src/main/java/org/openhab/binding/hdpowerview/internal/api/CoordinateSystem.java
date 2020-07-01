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
    ZERO_IS_CLOSED(1),
    ZERO_IS_OPEN(2),
    VANE_COORDS(3),
    ERROR_UNKNOWN(4);

    public final int key;

    CoordinateSystem(int key) {
        this.key = key;
    }

    public static CoordinateSystem get(int key) {
        switch (key) {
            case 1:
                return ZERO_IS_CLOSED;
            case 2:
                return ZERO_IS_OPEN;
            case 3:
                return VANE_COORDS;
        }
        return ERROR_UNKNOWN;
    }
}
