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
package org.openhab.binding.millheat.internal.model;

/**
 * The {@link ModeType} represents a type of mode the user can set in the app.
 *
 * @author Arne Seime - Initial contribution
 */
public enum ModeType {
    ALWAYSHOME(-1),
    COMFORT(1),
    SLEEP(2),
    AWAY(3),
    VACATION(4),
    OFF(5);

    public static ModeType valueOf(final int modeVal) {
        for (final ModeType mode : ModeType.values()) {
            if (mode.value == modeVal) {
                return mode;
            }
        }
        return null;
    }

    private final int value;

    ModeType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
