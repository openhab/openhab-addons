/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
    GOOD(HSBType.fromRGB(0, 228, 0)),
    MODERATE(HSBType.fromRGB(255, 255, 0)),
    UNHEALTHY_FSG(HSBType.fromRGB(255, 126, 0)),
    UNHEALTHY(HSBType.fromRGB(255, 0, 0)),
    VERY_UNHEALTHY(HSBType.fromRGB(143, 63, 151)),
    HAZARDOUS(HSBType.fromRGB(126, 0, 35));

    private HSBType color;

    Appreciation(HSBType color) {
        this.color = color;
    }

    public State getColor() {
        return color;
    }
}
