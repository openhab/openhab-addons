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
 * A shade type, as returned by the HD Power View Hub.
 *
 * @author Andy Lintner
 */
public enum ShadePositionKind {

    POSITION(1),
    VANE(3);

    private final int key;

    ShadePositionKind(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static ShadePositionKind get(int key) {
        if (key == 1) {
            return ShadePositionKind.POSITION;
        } else if (key == 3) {
            return ShadePositionKind.VANE;
        } else {
            return null;
        }
    }

}
