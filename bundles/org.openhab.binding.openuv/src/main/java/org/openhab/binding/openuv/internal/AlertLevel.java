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
package org.openhab.binding.openuv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link AlertLevel} enum defines alert level in regard of the UV Index
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum AlertLevel {
    GREEN(DecimalType.ZERO, "3a8b2f"),
    YELLOW(new DecimalType(1), "f9a825"),
    ORANGE(new DecimalType(2), "ef6c00"),
    RED(new DecimalType(3), "b71c1c"),
    PURPLE(new DecimalType(4), "6a1b9a"),
    UNKNOWN(UnDefType.NULL, "b3b3b3");

    public final State state;
    public final String color;

    AlertLevel(State state, String color) {
        this.state = state;
        this.color = color;
    }

    public static AlertLevel fromUVIndex(double uv) {
        if (uv >= 11) {
            return PURPLE;
        } else if (uv >= 8) {
            return RED;
        } else if (uv >= 6) {
            return ORANGE;
        } else if (uv >= 3) {
            return YELLOW;
        } else if (uv > 0) {
            return GREEN;
        }
        return UNKNOWN;
    }
}
