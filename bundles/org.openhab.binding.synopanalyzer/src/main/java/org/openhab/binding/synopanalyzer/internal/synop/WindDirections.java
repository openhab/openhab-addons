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
package org.openhab.binding.synopanalyzer.internal.synop;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WindDirections} enum possible wind directions
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum WindDirections {
    N,
    NNE,
    NE,
    ENE,
    E,
    ESE,
    SE,
    SSE,
    S,
    SSW,
    SW,
    WSW,
    W,
    WNW,
    NW,
    NNW;

    private static final double STEP = 360.0 / values().length;

    /**
     * Returns the wind direction based on degree.
     */
    public static WindDirections getWindDirection(int degree) {

        double b = Math.floor((degree + (STEP / 2.0)) / STEP);
        return values()[(int) (b % values().length)];
    }
}
