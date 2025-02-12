/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TemperatureUnit} defines the available temperature units.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public enum TemperatureUnit {
    FAHRENHEIT(0),
    CELSIUS(1);

    private final int value;

    TemperatureUnit(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TemperatureUnit fromValue(int value) {
        for (TemperatureUnit unit : values()) {
            if (unit.getValue() == value) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid temperature unit value: " + value);
    }
}
