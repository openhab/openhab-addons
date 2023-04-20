/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

public enum AlertLevel {
    GREEN(DecimalType.ZERO, HSBType.fromRGB(85, 139, 47)),
    YELLOW(new DecimalType(1), HSBType.fromRGB(249, 168, 37)),
    ORANGE(new DecimalType(2), HSBType.fromRGB(239, 108, 0)),
    RED(new DecimalType(3), HSBType.fromRGB(183, 28, 28)),
    PURPLE(new DecimalType(4), HSBType.fromRGB(106, 27, 154)),
    UNKNOWN(UnDefType.NULL, HSBType.fromRGB(179, 179, 179));

    public final State state;
    public final HSBType color;

    AlertLevel(State state, HSBType color) {
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
