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
package org.openhab.binding.airquality.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.State;

/**
 * The {@link Appreciation} enum lists all possible appreciation
 * of the AQI Level associated with their standard color.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Appreciation {
    GOOD(0, 228, 0),
    MODERATE(255, 255, 0),
    UNHEALTHY_FSG(255, 126, 0),
    UNHEALTHY(255, 0, 0),
    VERY_UNHEALTHY(143, 63, 151),
    HAZARDOUS(126, 0, 35);

    private HSBType color;

    Appreciation(int r, int g, int b) {
        this.color = HSBType.fromRGB(r, g, b);
    }

    public State getColor() {
        return color;
    }
}
