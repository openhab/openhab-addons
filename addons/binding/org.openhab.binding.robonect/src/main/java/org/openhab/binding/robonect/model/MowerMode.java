/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

/**
 * @author Marco Meyer - Initial contribution
 */
public enum MowerMode {

    AUTO(0),
    MANUAL(1),
    HOME(2),
    DEMO(3),
    UNKNOWN(99);

    private int code;

    MowerMode(int code) {
        this.code = code;
    }

    public static MowerMode fromMode(int mode) {
        for (MowerMode mowerMode : MowerMode.values()) {
            if (mowerMode.code == mode) {
                return mowerMode;
            }
        }
        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }
    
    /*
    0: Auto
    1: Manuell
    2: Home
    3: Demo
     */
}
