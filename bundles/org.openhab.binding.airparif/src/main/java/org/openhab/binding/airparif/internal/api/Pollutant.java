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

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Pollutant} enum lists all pollutants tracked by AirParif
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Pollutant {
    @SerializedName("pm25")
    PM25(Units.MICROGRAM_PER_CUBICMETRE),

    @SerializedName("pm10")
    PM10(Units.MICROGRAM_PER_CUBICMETRE),

    @SerializedName("no2")
    NO2(Units.PARTS_PER_BILLION),

    @SerializedName("o3")
    O3(Units.PARTS_PER_BILLION),

    @SerializedName("indice")
    INDICE(Units.PERCENT),

    UNKNOWN(Units.PERCENT);

    public static final EnumSet<Pollutant> AS_SET = EnumSet.allOf(Pollutant.class);

    public final Unit<?> unit;

    Pollutant(Unit<?> unit) {
        this.unit = unit;
    }

    public static Pollutant safeValueOf(String searched) {
        try {
            return Pollutant.valueOf(searched);
        } catch (IllegalArgumentException e) {
            return Pollutant.UNKNOWN;
        }
    }
}
