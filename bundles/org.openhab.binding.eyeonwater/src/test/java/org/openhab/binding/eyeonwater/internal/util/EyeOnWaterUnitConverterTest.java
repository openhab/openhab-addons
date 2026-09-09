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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.eyeonwater.internal.util.EyeOnWaterUnitConverter.NormalizedReading;

/**
 * Tests for {@link EyeOnWaterUnitConverter}.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
class EyeOnWaterUnitConverterTest {

    @Test
    void testNormalizeGallons() {
        NormalizedReading r1 = EyeOnWaterUnitConverter.normalize(1.5, "GAL");
        assertEquals(1.5, r1.getValue());
        assertEquals("gal", r1.getUnit());

        NormalizedReading r2 = EyeOnWaterUnitConverter.normalize(1.5, "10 GAL");
        assertEquals(15.0, r2.getValue());
        assertEquals("gal", r2.getUnit());

        NormalizedReading r3 = EyeOnWaterUnitConverter.normalize(1.5, "100 GAL");
        assertEquals(150.0, r3.getValue());
        assertEquals("gal", r3.getUnit());

        NormalizedReading r4 = EyeOnWaterUnitConverter.normalize(1.5, "KGAL");
        assertEquals(1500.0, r4.getValue());
        assertEquals("gal", r4.getUnit());
    }

    @Test
    void testNormalizeCubicFeet() {
        NormalizedReading r1 = EyeOnWaterUnitConverter.normalize(2.0, "CF");
        assertEquals(2.0, r1.getValue());
        assertEquals("cf", r1.getUnit());

        NormalizedReading r2 = EyeOnWaterUnitConverter.normalize(2.0, "CUBIC_FEET");
        assertEquals(2.0, r2.getValue());
        assertEquals("cf", r2.getUnit());

        NormalizedReading r3 = EyeOnWaterUnitConverter.normalize(2.0, "10 CF");
        assertEquals(20.0, r3.getValue());
        assertEquals("cf", r3.getUnit());

        NormalizedReading r4 = EyeOnWaterUnitConverter.normalize(2.0, "CCF");
        assertEquals(200.0, r4.getValue());
        assertEquals("cf", r4.getUnit());
    }

    @Test
    void testNormalizeCubicMetersAndLiters() {
        NormalizedReading r1 = EyeOnWaterUnitConverter.normalize(3.0, "CM");
        assertEquals(3.0, r1.getValue());
        assertEquals("m³", r1.getUnit());

        NormalizedReading r2 = EyeOnWaterUnitConverter.normalize(3.0, "CUBIC_METER");
        assertEquals(3.0, r2.getValue());
        assertEquals("m³", r2.getUnit());

        NormalizedReading r3 = EyeOnWaterUnitConverter.normalize(1500.0, "LITERS");
        assertEquals(1.5, r3.getValue());
        assertEquals("m³", r3.getUnit());
    }

    @Test
    void testNormalizeNullOrUnknown() {
        NormalizedReading r1 = EyeOnWaterUnitConverter.normalize(4.0, null);
        assertEquals(4.0, r1.getValue());
        assertEquals("gal", r1.getUnit());

        NormalizedReading r2 = EyeOnWaterUnitConverter.normalize(4.0, "UNKNOWN");
        assertEquals(4.0, r2.getValue());
        assertEquals("gal", r2.getUnit());
    }
}
