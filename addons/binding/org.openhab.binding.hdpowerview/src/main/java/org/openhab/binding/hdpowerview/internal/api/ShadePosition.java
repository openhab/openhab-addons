/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * The position of a shade, as returned by the HD Power View HUB.
 *
 * @author Andy Lintner - Initial contribution
 */
public class ShadePosition {

    int posKind1;
    public int position1;

    public static ShadePosition forPosition(int position) {
        return new ShadePosition(position, null);
    }

    public static ShadePosition forVane(int vane) {
        return new ShadePosition(null, vane);
    }

    ShadePosition(Integer position, Integer vane) {
        if (position != null) {
            posKind1 = ShadePositionKind.POSITION.getKey();
            position1 = position;
        } else if (vane != null) {
            posKind1 = ShadePositionKind.VANE.getKey();
            position1 = vane;
        }
    }

    ShadePosition() {
    }

    public int getPosition() {
        if (ShadePositionKind.POSITION.getKey() == posKind1) {
            return position1;
        } else {
            return 0;
        }
    }

    public int getVane() {
        if (ShadePositionKind.VANE.getKey() == posKind1) {
            return position1;
        } else {
            return 0;
        }
    }
}
