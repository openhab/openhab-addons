/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.am43.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is an enum representing possible motor direction settings
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public enum Direction {
    Forward(0x1),
    Reverse(0x0);

    private byte value;

    private Direction(int value) {
        this.value = (byte) value;
    }

    public byte toByte() {
        return value;
    }

    public static Direction valueOf(boolean bitValue) {
        return bitValue ? Forward : Reverse;
    }
}
