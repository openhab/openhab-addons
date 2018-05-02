/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
