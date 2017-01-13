/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enigma2.internal;

/**
 * Representing the power state of the Enigma2 device
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public enum Enigma2PowerState {
    TOGGLE_STANDBY(0),
    DEEPSTANDBY(1),
    REBOOT(2),
    RESTART(3),
    WAKEUP_FROM_STANDBY(4),
    STANDBY(5);

    private int value;

    Enigma2PowerState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
