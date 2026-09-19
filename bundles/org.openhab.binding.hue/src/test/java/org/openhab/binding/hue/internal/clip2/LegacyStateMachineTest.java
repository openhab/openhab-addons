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
import org.openhab.binding.hue.internal.api.dto.clip2.helper.LegacyLightState;

/**
 * JUnit test for {@link LegacyLightState}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class LegacyStateMachineTest {

    @Test
    void testBareConstructor() {
        LegacyLightState test = new LegacyLightState();
        assertEquals(LegacyLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertNull(test.getXY());
    }

    @Test
    void testColorTempModeIsSet() {
        LegacyLightState test = new LegacyLightState();
        double[] xy = { 0.1, 0.1 };
        test.setParameters(new PairXy().setXY(xy), 100L);
        assertEquals(LegacyLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(xy), test.getXY());
    }

    @Test
    void testFullColorModeIsSet() {
        LegacyLightState test = new LegacyLightState();
        double[] xy = { 0.1, 0.1 };
        test.setParameters(new PairXy().setXY(xy), null);
        assertEquals(LegacyLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertEquals(new PairXy().setXY(xy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndShadowIgnored() {
        LegacyLightState test = new LegacyLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        test.setParameters(new PairXy().setXY(ctxy), 100L);
        test.setParameters(new PairXy().setXY(fullxy), null);
        assertEquals(LegacyLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(ctxy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndColorTempXySentAgain() {
        LegacyLightState test = new LegacyLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        test.setParameters(new PairXy().setXY(ctxy), 100L);
        test.setParameters(new PairXy().setXY(fullxy), null);
        test.setParameters(new PairXy().setXY(ctxy), null);
        assertEquals(LegacyLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(ctxy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndFullColorXySentAgain() {
        LegacyLightState test = new LegacyLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        test.setParameters(new PairXy().setXY(ctxy), 100L);
        test.setParameters(new PairXy().setXY(fullxy), null);
        test.setParameters(new PairXy().setXY(fullxy), null);
        assertEquals(LegacyLightState.Mode.COLOR_TEMP, test.getMode());
        assertEquals(100L, test.getMirek());
        assertEquals(new PairXy().setXY(ctxy), test.getXY());
    }

    @Test
    void testColorTempModeSetAndDifferentFullColorXySent() {
        LegacyLightState test = new LegacyLightState();
        double[] ctxy = { 0.1, 0.1 };
        double[] fullxy = { 0.2, 0.2 };
        double[] otherxy = { 0.3, 0.3 };
        test.setParameters(new PairXy().setXY(ctxy), 100L);
        test.setParameters(new PairXy().setXY(fullxy), null);
        test.setParameters(new PairXy().setXY(otherxy), null);
        assertEquals(LegacyLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertEquals(new PairXy().setXY(otherxy), test.getXY());
    }

    @Test
    void testFullColorModeSetAndDifferentFullColorXySent() {
        LegacyLightState test = new LegacyLightState();
        double[] axy = { 0.1, 0.1 };
        double[] bxy = { 0.2, 0.2 };
        test.setParameters(new PairXy().setXY(axy), null);
        assertEquals(LegacyLightState.Mode.FULL_COLOR, test.getMode());
        test.setParameters(new PairXy().setXY(bxy), null);
        assertEquals(LegacyLightState.Mode.FULL_COLOR, test.getMode());
        assertNull(test.getMirek());
        assertEquals(new PairXy().setXY(bxy), test.getXY());
    }
}
