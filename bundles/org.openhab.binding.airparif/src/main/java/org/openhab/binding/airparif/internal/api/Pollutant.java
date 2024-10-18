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
package org.openhab.binding.airparif.internal.api;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Pollutant} enum lists all pollutants tracked by AirParif
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Pollutant {
    PM25,
    PM10,
    NO2,
    O3,
    UNKNOWN;

    public static final EnumSet<Pollutant> AS_SET = EnumSet.allOf(Pollutant.class);

    public static Pollutant safeValueOf(String searched) {
        try {
            return Pollutant.valueOf(searched);
        } catch (IllegalArgumentException e) {
            return Pollutant.UNKNOWN;
        }
    }
}
