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

/**
 * Shade position kind, as returned by the HD Power View Hub.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
public enum PosKind {
    REGULAR(1),
    INVERTED(2),
    VANE(3),
    ERROR(4);

    public final int key;

    PosKind(int key) {
        this.key = key;
    }

    public static PosKind get(int key) {
        switch (key) {
            case 1:
                return PosKind.REGULAR;
            case 2:
                return PosKind.INVERTED;
            case 3:
                return PosKind.VANE;
        }
        return PosKind.ERROR;
    }
}
