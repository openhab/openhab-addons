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
package org.openhab.binding.dirigera.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.mock.TemperatureLightHandlerMock;
import org.openhab.core.library.types.HSBType;

/**
 * {@link TestColorModel} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestColorModel {

    @Test
    void hsbCloseTo() {
        // test with 2% means max distance between 2 values is 360 * 0.02 = 7.2
        HSBType first = new HSBType("50, 100, 100");
        HSBType second = new HSBType("57.1, 100, 100");
        assertTrue(ColorModel.closeTo(first, second, 0.02), "Hue is close");
        assertTrue(ColorModel.closeTo(second, first, 0.02), "Hue is close");
        second = new HSBType("57.3, 100, 100");
        assertFalse(ColorModel.closeTo(first, second, 0.02), "Hue bit over boundary");
        assertFalse(ColorModel.closeTo(second, first, 0.02), "Hue bit over boundary");

        // test boundary at min and max hue values
        first = new HSBType("359, 100, 100");
        second = new HSBType("6.1, 100, 100");
        assertTrue(ColorModel.closeTo(first, second, 0.02), "Hue is close");
        assertTrue(ColorModel.closeTo(second, first, 0.02), "Hue is close");
        second = new HSBType("6.3, 100, 100");
        assertFalse(ColorModel.closeTo(first, second, 0.02), "Hue bit over boundary");
        assertFalse(ColorModel.closeTo(second, first, 0.02), "Hue bit over boundary");

        // test saturation
        first = new HSBType("359, 50, 100");
        second = new HSBType("359, 51.9, 100");
        assertTrue(ColorModel.closeTo(first, second, 0.02), "Saturation is close");
        assertTrue(ColorModel.closeTo(second, first, 0.02), "Saturation is close");
        second = new HSBType("359, 52.1, 100");
        assertFalse(ColorModel.closeTo(first, second, 0.02), "Saturation bit over boundary");
        assertFalse(ColorModel.closeTo(second, first, 0.02), "Saturation bit over boundary");
    }

    @Test
    void testKelvinToHSB() {
        TemperatureLightHandlerMock handler = new TemperatureLightHandlerMock();
        HSBType hsb = ColorModel.kelvin2Hsb(6200);
        long kelvinCalculated = ColorModel.hsb2Kelvin(hsb);
        assertEquals(0, handler.getPercent(kelvinCalculated), "Below boundary");

        hsb = ColorModel.kelvin2Hsb(1000);
        kelvinCalculated = ColorModel.hsb2Kelvin(hsb);
        assertEquals(100, handler.getPercent(kelvinCalculated), "Above boundary");

        hsb = ColorModel.kelvin2Hsb(2200 + 900);
        kelvinCalculated = ColorModel.hsb2Kelvin(hsb);
        assertEquals(50, handler.getPercent(kelvinCalculated), 2, "Middle ~50% temperature");

        for (int kelvinInput = 2000; kelvinInput < 6501; kelvinInput++) {
            hsb = ColorModel.kelvin2Hsb(kelvinInput);
            kelvinCalculated = ColorModel.hsb2Kelvin(hsb);
            // assure all values has max difference of 50
            assertEquals(kelvinInput, kelvinCalculated, 50, "Diff " + (kelvinInput - kelvinCalculated));
        }

        // test if kelvin is matching with IKEA TRADFRI bulb values
        hsb = ColorModel.kelvin2Hsb(2200);
        assertEquals(29.7, hsb.getHue().doubleValue(), 0.1, "Hue for 2200 K");
        assertEquals(84.7, hsb.getSaturation().doubleValue(), 0.1, "Saturation for 2200 K");
    }
}
