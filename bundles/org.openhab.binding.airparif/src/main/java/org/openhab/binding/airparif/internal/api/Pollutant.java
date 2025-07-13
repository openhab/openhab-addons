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
package org.openhab.binding.airparif.internal.api;

import java.util.EnumSet;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.AirParifApi.Appreciation;
import org.openhab.core.library.unit.Units;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Pollutant} enum lists all pollutants tracked by AirParif
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum Pollutant {
    // Concentration thresholds per pollutant are available here:
    // https://www.airparif.fr/sites/default/files/pdf/guide_calcul_nouvel_indice_fedeAtmo_14122020.pdf

    @SerializedName("pm25")
    PM25(Units.MICROGRAM_PER_CUBICMETRE, new int[] { 10, 20, 25, 50, 75 }),

    @SerializedName("pm10")
    PM10(Units.MICROGRAM_PER_CUBICMETRE, new int[] { 20, 40, 50, 100, 150 }),

    @SerializedName("no2")
    NO2(Units.MICROGRAM_PER_CUBICMETRE, new int[] { 40, 90, 120, 230, 340 }),

    @SerializedName("o3")
    O3(Units.MICROGRAM_PER_CUBICMETRE, new int[] { 50, 100, 130, 240, 380 }),

    @SerializedName("so2")
    SO2(Units.MICROGRAM_PER_CUBICMETRE, new int[] { 100, 200, 350, 500, 750 }),

    @SerializedName("indice")
    INDICE(null, new int[] {}),

    UNKNOWN(null, new int[] {});

    public static final EnumSet<Pollutant> AS_SET = EnumSet.allOf(Pollutant.class);

    public final @Nullable Unit<?> unit;
    private final int[] thresholds;

    Pollutant(@Nullable Unit<?> unit, int[] thresholds) {
        this.unit = unit;
        this.thresholds = thresholds;
    }

    public static Pollutant safeValueOf(String searched) {
        try {
            return Pollutant.valueOf(searched);
        } catch (IllegalArgumentException e) {
            return Pollutant.UNKNOWN;
        }
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public Appreciation getAppreciation(double concentration) {
        if (thresholds.length == 0) {
            return Appreciation.UNKNOWN;
        }

        for (int i = 0; i < thresholds.length; i++) {
            if (concentration <= thresholds[i]) {
                return Appreciation.values()[i];
            }
        }
        return Appreciation.EXTREMELY_BAD;
    }
}
