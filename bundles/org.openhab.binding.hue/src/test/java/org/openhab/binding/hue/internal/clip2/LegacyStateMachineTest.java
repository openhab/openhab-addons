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
package org.openhab.binding.hue.internal.clip2;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.PairXy;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.ColorModeWorkAroundLightState;

/**
 * JUnit test for {@link ColorModeWorkAroundLightState}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class LegacyStateMachineTest {

    @Test
    void testBareConstructor() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        assertEquals(ColorModeWorkAroundLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertNull(test.getXY());
    }

    @Test
    void testThrows() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        assertThrows(IllegalArgumentException.class, () -> test.setValues(null, null));
    }

    @Test
    void testDoesNotThrow() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] xy = { 0.1, 0.1 };
        assertDoesNotThrow(() -> test.setValues(100L, null));
        assertDoesNotThrow(() -> test.setValues(null, new PairXy().setXY(xy)));
    }

    @Test
    void testColorTempModeIsSet() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        test.setValues(100L, null);
        assertEquals(ColorModeWorkAroundLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertNull(test.getXY());
    }

    void testFullColorModeIsSet() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] xy = { 0.1, 0.1 };
        test.setValues(null, new PairXy().setXY(xy));
        assertEquals(ColorModeWorkAroundLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertEquals(xy, test.getXY());
    }

    @Test
    void testColorTempModeSetAndShadowIgnored() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        test.setValues(100L, new PairXy().setXY(ctxy));
        test.setValues(null, new PairXy().setXY(fullxy));
        assertEquals(ColorModeWorkAroundLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(ctxy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndColorTempXySentAgain() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        test.setValues(100L, new PairXy().setXY(ctxy));
        test.setValues(null, new PairXy().setXY(fullxy));
        test.setValues(null, new PairXy().setXY(ctxy));
        assertEquals(ColorModeWorkAroundLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(ctxy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndFullColorXySentAgain() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        test.setValues(100L, new PairXy().setXY(ctxy));
        test.setValues(null, new PairXy().setXY(fullxy));
        test.setValues(null, new PairXy().setXY(fullxy));
        assertEquals(ColorModeWorkAroundLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(ctxy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndDifferentFullColorXySent() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        double[] otherxy = { 0.3, 0.3 };
        test.setValues(100L, new PairXy().setXY(ctxy));
        test.setValues(null, new PairXy().setXY(fullxy));
        test.setValues(null, new PairXy().setXY(otherxy));
        assertEquals(ColorModeWorkAroundLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertEquals(new PairXy().setXY(otherxy), test.getXY());
    }

    @Test
    void testFullColorModeSetAndDifferentFullColorXySent() {
        ColorModeWorkAroundLightState test = new ColorModeWorkAroundLightState();
        double[] axy = { 0.1, 0.1 };
        double[] bxy = { 0.2, 0.2 };
        test.setValues(null, new PairXy().setXY(axy));
        assertEquals(ColorModeWorkAroundLightState.Mode.FULL_COLOR, test.getMode());
        test.setValues(null, new PairXy().setXY(bxy));
        assertEquals(ColorModeWorkAroundLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertEquals(new PairXy().setXY(bxy), test.getXY());
    }
}
