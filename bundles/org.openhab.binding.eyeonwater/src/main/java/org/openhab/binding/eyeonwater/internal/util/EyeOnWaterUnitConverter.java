/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.eyeonwater.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link EyeOnWaterUnitConverter} is a utility class that centralizes the normalization
 * of different EyeOnWater API raw units into standard openHAB units.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterUnitConverter {

    /**
     * DTO containing the normalized numeric value and the associated openHAB unit string.
     */
    public static class NormalizedReading {
        private final double value;
        private final String unit;

        public NormalizedReading(double value, String unit) {
            this.value = value;
            this.unit = unit;
        }

        public double getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }
    }

    /**
     * Normalizes raw reading values and units from the EyeOnWater API into standard units.
     *
     * @param value the raw numeric reading value
     * @param rawUnit the raw unit of measurement string (e.g. GAL, CF, CM, CCF, etc.)
     * @return the NormalizedReading containing the normalized value and openHAB unit string
     */
    public static NormalizedReading normalize(double value, @Nullable String rawUnit) {
        if (rawUnit == null) {
            return new NormalizedReading(value, "gal");
        }

        return switch (rawUnit.toUpperCase()) {
            case "GAL" -> new NormalizedReading(value, "gal");
            case "10 GAL" -> new NormalizedReading(value * 10, "gal");
            case "100 GAL" -> new NormalizedReading(value * 100, "gal");
            case "KGAL" -> new NormalizedReading(value * 1000, "gal");
            case "CF", "CUBIC_FEET" -> new NormalizedReading(value, "cf");
            case "10 CF" -> new NormalizedReading(value * 10, "cf");
            case "CCF" -> new NormalizedReading(value * 100, "cf");
            case "CM", "CUBIC_METER" -> new NormalizedReading(value, "m³");
            case "LITER", "LITERS" -> new NormalizedReading(value / 1000.0, "m³");
            default -> new NormalizedReading(value, "gal");
        };
    }
}
