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
 * A shade type, as returned by the HD Power View Hub.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
public enum ShadePositionKind {

    /*-
     * The types of position are defined as follows: 
     *   0 = None 
     *   1 = Primary rail
     *   2 = Secondary rail
     *   3 = Vane tilt
     *   4 = Error
     */
    PRIMARY(1),
    SECONDARY(2),
    VANE(3);

    private final int key;

    ShadePositionKind(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static ShadePositionKind get(int key) {
        switch (key) {
            case 1:
                return ShadePositionKind.PRIMARY;
            case 2:
                return ShadePositionKind.SECONDARY;
            case 3:
                return ShadePositionKind.VANE;
        }
        return null;
    }
}
